package kaamverse.bl.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.AuditLogEntry;
import kaamverse.bl.model.Hire;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.Review;
import kaamverse.bl.model.TrustBadge;
import kaamverse.common.DatabaseException;
import kaamverse.common.MaidStatus;
import kaamverse.common.ReviewStatus;
import kaamverse.common.StringsB;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.DaoFactoryB;
import kaamverse.db.dao.HireDao;
import kaamverse.db.dao.HiringWomanDao;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.ReviewDao;
import kaamverse.db.dao.TransactionManager;
import kaamverse.db.dao.TrustBadgeDao;

/**
 * UC5 data access and saving. High Cohesion: moderation and badges only.
 * User-facing problems are thrown as IllegalStateException with a ready-to-show message.
 */
public class TrustBadgeService {

    /** A pending review with the maid, the employer and the hire it is about. */
    public record ReviewCase(Review review, Maid maid, String employerName, Hire hire) {
    }

    /** A maid's approved-review figures and whether she meets the badge rule. */
    public record MaidStats(Maid maid, int approvedCount, double average, double positivePercentage,
                            boolean eligible, boolean hasBadge) {
    }

    private final ReviewDao reviewDao = DaoFactoryB.reviewDao();
    private final HireDao hireDao = DaoFactoryB.hireDao();
    private final TrustBadgeDao badgeDao = DaoFactoryB.trustBadgeDao();
    private final MaidDao maidDao = DaoFactory.maidDao();
    private final HiringWomanDao womanDao = DaoFactory.hiringWomanDao();
    // GRASP Protected Variations: the rule sits behind an interface, so it can change
    private final BadgeEligibilityPolicy policy = new DefaultBadgeEligibilityPolicy();
    private final NotificationService notifications = new NotificationService();

    public List<ReviewCase> pendingReviews() {
        List<ReviewCase> cases = new ArrayList<>();
        for (Review review : reviewDao.findPendingFlagged()) {
            toCase(review).ifPresent(cases::add);
        }
        return cases;
    }

    /** Approves a pending review, recalculates the maid's average and logs it. Returns the new average. */
    public double approve(Officer officer, int reviewId) {
        return TransactionManager.call(() -> {
            ReviewCase item = loadPending(reviewId);
            if (!reviewDao.decide(reviewId, ReviewStatus.Approved)) {
                throw new IllegalStateException(StringsB.UC5_ERR_DECIDED);
            }
            Maid maid = item.maid();
            // GRASP Information Expert: the maid averages her own approved reviews
            maid.updateAverageRating(reviewDao.findApprovedByMaid(maid.getMaidId()));
            reviewDao.saveMaidAverage(maid.getMaidId(), maid.getAverageRating());
            AuditLogEntry.log(officer, StringsB.UC5_AUDIT_APPROVED.formatted(
                    reviewId, maid.getName(), item.review().getOverallRating()));
            return maid.getAverageRating();
        });
    }

    public void reject(Officer officer, int reviewId) {
        TransactionManager.run(() -> {
            ReviewCase item = loadPending(reviewId);
            if (!reviewDao.decide(reviewId, ReviewStatus.Rejected)) {
                throw new IllegalStateException(StringsB.UC5_ERR_DECIDED);
            }
            AuditLogEntry.log(officer, StringsB.UC5_AUDIT_REJECTED.formatted(
                    reviewId, item.maid().getName(), item.review().getOverallRating()));
        });
    }

    /** Every maid with at least one approved review, best average first. */
    public List<MaidStats> maidStats() {
        List<MaidStats> list = new ArrayList<>();
        for (int maidId : reviewDao.findMaidIdsWithApprovedReviews()) {
            statsFor(maidId).ifPresent(list::add);
        }
        list.sort(Comparator.comparingDouble(MaidStats::average).reversed());
        return list;
    }

    public Optional<MaidStats> statsFor(int maidId) {
        Optional<Maid> found = maidDao.findById(maidId);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Maid maid = found.get();
        List<Review> approved = reviewDao.findApprovedByMaid(maidId);
        maid.updateAverageRating(approved);
        long positive = approved.stream().filter(review -> review.getOverallRating() >= 4).count();
        double positivePercentage = approved.isEmpty() ? 0 : positive * 100.0 / approved.size();
        boolean eligible = policy.isEligible(approved.size(), maid.getAverageRating(), positivePercentage);
        return Optional.of(new MaidStats(maid, approved.size(), maid.getAverageRating(),
                positivePercentage, eligible, maidDao.hasTrustBadge(maidId)));
    }

    /** Re-checks the rule at the moment of awarding, then saves the badge and the audit entry together. */
    public void awardBadge(Officer officer, int maidId) {
        MaidStats stats = TransactionManager.call(() -> {
            MaidStats current = statsFor(maidId)
                    .orElseThrow(() -> new IllegalStateException(StringsB.UC5_ERR_MAID));
            if (current.maid().getStatus() != MaidStatus.Verified) {
                throw new IllegalStateException(StringsB.UC5_ERR_MAID);
            }
            if (current.hasBadge()) {
                throw new IllegalStateException(StringsB.UC5_ERR_HAS_BADGE);
            }
            if (!current.eligible()) {
                throw new IllegalStateException(StringsB.UC5_ERR_NOT_ELIGIBLE);
            }
            TrustBadge badge = new TrustBadge();
            badge.setMaidId(maidId);
            badge.setAwardedDate(LocalDate.now());
            badge.setReason(StringsB.UC5_BADGE_REASON.formatted(
                    current.approvedCount(), current.average(), current.positivePercentage()));
            badge.setAwardedByOfficerId(officer.getOfficerId());
            badgeDao.insert(badge);
            AuditLogEntry.log(officer, StringsB.UC5_AUDIT_AWARDED.formatted(current.maid().getName()));
            return current;
        });
        try {
            notifications.send(stats.maid(), StringsB.UC5_NOTIFY_BADGE);
        } catch (DatabaseException e) {
            // the badge is already saved; a lost notification must not undo it
            System.err.println("Could not notify the maid: " + e.getMessage());
        }
    }

    private ReviewCase loadPending(int reviewId) {
        Review review = reviewDao.findById(reviewId)
                .filter(found -> found.getStatus() == ReviewStatus.Pending)
                .orElseThrow(() -> new IllegalStateException(StringsB.UC5_ERR_DECIDED));
        return toCase(review).orElseThrow(() -> new IllegalStateException(StringsB.UC5_ERR_DECIDED));
    }

    private Optional<ReviewCase> toCase(Review review) {
        Optional<Hire> hire = hireDao.findById(review.getHireId());
        if (hire.isEmpty()) {
            return Optional.empty();
        }
        Optional<Maid> maid = maidDao.findById(hire.get().getMaidId());
        if (maid.isEmpty()) {
            return Optional.empty();
        }
        String employer = womanDao.findById(hire.get().getHiringWomanId())
                .map(HiringWoman::getName).orElse("");
        return Optional.of(new ReviewCase(review, maid.get(), employer, hire.get()));
    }
}