package kaamverse.bl.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import kaamverse.bl.model.Contribution;
import kaamverse.bl.model.ContributionReceipt;
import kaamverse.bl.model.FundingCase;
import kaamverse.bl.model.FundingRequest;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.common.ContributionRejectedException;
import kaamverse.common.ContributionRejectedException.Reason;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.FundingStatus;
import kaamverse.common.Strings;
import kaamverse.db.dao.ContributionDao;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.FundingRecipientDao;
import kaamverse.db.dao.FundingRequestDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.TransactionManager;

/** UC10 data access and saving. GRASP High Cohesion: contributing to funding requests only. */
public class ContributionService {

    private static final Logger LOG = Logger.getLogger(ContributionService.class.getName());

    /** Acknowledgement numbers look like AK-20260001: this prefix, the year, then a 4-digit running number. */
    private static final String ACK_PREFIX = "AK-";
    /** Two contributions saved at the same moment can pick the same number; the loser tries again. */
    private static final int MAX_SAVE_ATTEMPTS = 5;

    private final MaidDao maidDao = DaoFactory.maidDao();
    private final FundingRequestDao requestDao = DaoFactory.fundingRequestDao();
    private final FundingRecipientDao recipientDao = DaoFactory.fundingRecipientDao();
    private final ContributionDao contributionDao = DaoFactory.contributionDao();
    private final NotificationService notificationService = new NotificationService();

    /** Open requests sent to this hiring woman (and only hers), each with its maid. */
    public List<FundingCase> findOpenCases(HiringWoman woman) {
        List<FundingCase> cases = new ArrayList<>();
        for (FundingRequest request : requestDao.findOpenForRecipient(woman.getHiringWomanId())) {
            loadMaid(request.getMaidId()).ifPresent(maid -> cases.add(new FundingCase(request, maid)));
        }
        return cases;
    }

    /** One open request that was sent to this woman, as it is now; empty if it is not hers or is closed. */
    public Optional<FundingCase> findOpenCase(HiringWoman woman, int requestId) {
        Optional<FundingRequest> found = requestDao.findById(requestId);
        if (found.isEmpty() || found.get().getStatus() == FundingStatus.Funded
                || !recipientDao.isRecipient(requestId, woman.getHiringWomanId())) {
            return Optional.empty();
        }
        return loadMaid(found.get().getMaidId()).map(maid -> new FundingCase(found.get(), maid));
    }

    /**
     * Saves the contribution, the new amount received and the new status in
     * one transaction, then notifies the maid. The request row is locked and
     * read again first, because another employer may have contributed since
     * the screen was filled.
     *
     * @throws ContributionRejectedException if the request is closed or the amount is now too large
     */
    public ContributionReceipt contribute(HiringWoman woman, int requestId, BigDecimal amount, String note) {
        ContributionReceipt receipt = saveWithNewAcknowledgementNo(woman, requestId, amount, note);
        notifyMaid(woman, receipt);
        return receipt;
    }

    private ContributionReceipt saveWithNewAcknowledgementNo(HiringWoman woman, int requestId,
                                                             BigDecimal amount, String note) {
        DuplicateRecordException lastClash = null;
        for (int attempt = 0; attempt < MAX_SAVE_ATTEMPTS; attempt++) {
            try {
                return TransactionManager.call(() -> {
                    FundingRequest request = requestDao.findByIdForUpdate(requestId)
                            .orElseThrow(() -> new ContributionRejectedException(Reason.NOT_AVAILABLE, 0));
                    if (request.getStatus() == FundingStatus.Funded
                            || !recipientDao.isRecipient(requestId, woman.getHiringWomanId())) {
                        throw new ContributionRejectedException(Reason.NOT_AVAILABLE, 0);
                    }
                    if (amount.compareTo(BigDecimal.valueOf(request.getRemainingAmount())) > 0) {
                        throw new ContributionRejectedException(Reason.TOO_MUCH, request.getRemainingAmount());
                    }
                    Contribution contribution = request.addContribution(woman, amount.doubleValue(), note);
                    contribution.setAcknowledgementNo(nextAcknowledgementNo(contribution.getContributionDate().getYear()));
                    contributionDao.insert(contribution);
                    requestDao.updateProgress(request);
                    return new ContributionReceipt(contribution, request);
                });
            } catch (DuplicateRecordException e) {
                lastClash = e; // the whole attempt was rolled back; pick the next number
            }
        }
        throw new DatabaseException("Could not find a free acknowledgement number", lastClash);
    }

    private String nextAcknowledgementNo(int year) {
        String prefix = ACK_PREFIX + year;
        int next = 1;
        Optional<String> latest = contributionDao.findLatestAcknowledgementNo(prefix);
        if (latest.isPresent()) {
            try {
                next = Integer.parseInt(latest.get().substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                throw new DatabaseException("Unexpected acknowledgement number format: " + latest.get(), e);
            }
        }
        return prefix + String.format(Locale.ROOT, "%04d", next);
    }

    /** The maid with her badge status; empty if she no longer exists. */
    private Optional<Maid> loadMaid(int maidId) {
        Optional<Maid> found = maidDao.findById(maidId);
        found.ifPresent(maid -> maid.setHasBadge(maidDao.hasTrustBadge(maidId)));
        return found;
    }

    /**
     * After the commit. The contribution is already saved, so one failed
     * message must not make the contributor think it was lost: it is logged only.
     */
    private void notifyMaid(HiringWoman woman, ContributionReceipt receipt) {
        FundingRequest request = receipt.request();
        try {
            Optional<Maid> maid = maidDao.findById(request.getMaidId());
            if (maid.isPresent()) {
                notificationService.send(maid.get(), Strings.UC10_NOTIFY_MAID.formatted(woman.getName(),
                        FundingRequestService.formatAmount(receipt.contribution().getAmount()),
                        request.getEventType(), request.getTrackingNo(),
                        FundingRequestService.formatAmount(request.getAmountReceived()),
                        FundingRequestService.formatAmount(request.getAmountNeeded()),
                        Strings.uc10StatusName(request.getStatus())));
            }
        } catch (DatabaseException e) {
            LOG.log(Level.WARNING, "Could not notify the maid about " + request.getTrackingNo(), e);
        }
    }
}
