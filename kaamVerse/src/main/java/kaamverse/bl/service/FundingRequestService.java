package kaamverse.bl.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import kaamverse.bl.model.FundingRequest;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.EventType;
import kaamverse.common.Strings;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.FundingRecipientDao;
import kaamverse.db.dao.FundingRequestDao;
import kaamverse.db.dao.HiringWomanDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.TransactionManager;

/** UC9 data access and saving. GRASP High Cohesion: raising funding requests only. */
public class FundingRequestService {

    private static final Logger LOG = Logger.getLogger(FundingRequestService.class.getName());

    /** Tracking numbers look like FR-20260001: this prefix, the year, then a 4-digit running number. */
    private static final String TRACKING_PREFIX = "FR-";
    /** Two requests saved at the same moment can pick the same number; the loser tries again. */
    private static final int MAX_SAVE_ATTEMPTS = 5;

    private final MaidDao maidDao = DaoFactory.maidDao();
    private final HiringWomanDao hiringWomanDao = DaoFactory.hiringWomanDao();
    private final FundingRequestDao requestDao = DaoFactory.fundingRequestDao();
    private final FundingRecipientDao recipientDao = DaoFactory.fundingRecipientDao();
    private final NotificationService notificationService = new NotificationService();

    /**
     * The maid as she is in the database now, with her current employers
     * loaded, or empty if she no longer exists.
     */
    public Optional<Maid> findMaidWithEmployers(int maidId) {
        Optional<Maid> found = maidDao.findById(maidId);
        found.ifPresent(maid -> maid.setCurrentEmployers(hiringWomanDao.findActiveEmployersOfMaid(maidId)));
        return found;
    }

    /**
     * Saves the request (status Pending, nothing received yet) and one
     * recipient per employer in one transaction, then notifies each employer.
     * GRASP Creator: the Maid creates the FundingRequest, and the request
     * creates its recipients (sendTo).
     *
     * @return the saved request, with its ID and tracking number
     */
    public FundingRequest submit(Maid maid, EventType eventType, double amount, LocalDate eventDate,
                                 String reason, List<HiringWoman> employers) {
        FundingRequest saved = saveWithNewTrackingNo(maid, eventType, amount, eventDate, reason, employers);
        notifyEmployers(maid, saved, employers);
        return saved;
    }

    /** "25000" for whole amounts, "25,000.50" otherwise; always ASCII digits. */
    public static String formatAmount(double amount) {
        boolean whole = amount == Math.rint(amount);
        return String.format(Locale.ROOT, whole ? "%,.0f" : "%,.2f", amount);
    }

    private FundingRequest saveWithNewTrackingNo(Maid maid, EventType eventType, double amount,
                                                 LocalDate eventDate, String reason,
                                                 List<HiringWoman> employers) {
        DuplicateRecordException lastClash = null;
        for (int attempt = 0; attempt < MAX_SAVE_ATTEMPTS; attempt++) {
            try {
                return TransactionManager.call(() -> {
                    FundingRequest request =
                            maid.raiseRequest(eventType.getLabel(), amount, eventDate, reason);
                    request.setTrackingNo(nextTrackingNo(request.getCreatedDate().getYear()));
                    requestDao.insert(request);
                    request.sendTo(employers); // after the insert, so each recipient has the request ID
                    recipientDao.insertAll(request.getRecipients());
                    return request;
                });
            } catch (DuplicateRecordException e) {
                lastClash = e; // the whole attempt was rolled back; pick the next number
            }
        }
        throw new DatabaseException("Could not find a free tracking number", lastClash);
    }

    private String nextTrackingNo(int year) {
        String prefix = TRACKING_PREFIX + year;
        int next = 1;
        Optional<String> latest = requestDao.findLatestTrackingNo(prefix);
        if (latest.isPresent()) {
            try {
                next = Integer.parseInt(latest.get().substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                throw new DatabaseException("Unexpected tracking number format: " + latest.get(), e);
            }
        }
        return prefix + String.format(Locale.ROOT, "%04d", next);
    }

    /**
     * After the commit. The request is already saved, so one failed message
     * must not make the maid think her request was lost: it is logged only.
     */
    private void notifyEmployers(Maid maid, FundingRequest request, List<HiringWoman> employers) {
        String text = Strings.UC9_NOTIFY_EMPLOYER.formatted(maid.getName(), request.getEventType(),
                formatAmount(request.getAmountNeeded()), request.getEventDate(), request.getTrackingNo());
        for (HiringWoman employer : employers) {
            try {
                notificationService.send(employer, text);
            } catch (DatabaseException e) {
                LOG.log(Level.WARNING, "Could not notify employer " + employer.getHiringWomanId()
                        + " about " + request.getTrackingNo(), e);
            }
        }
    }
}
