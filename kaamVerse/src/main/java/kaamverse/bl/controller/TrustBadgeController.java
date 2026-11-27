package kaamverse.bl.controller;

import java.util.List;

import kaamverse.bl.model.Officer;
import kaamverse.bl.service.Session;
import kaamverse.bl.service.TrustBadgeService;
import kaamverse.bl.service.TrustBadgeService.MaidStats;
import kaamverse.bl.service.TrustBadgeService.ReviewCase;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.Permission;
import kaamverse.common.StringsB;

/**
 * GRASP Controller for UC5. The screen talks only to this class. It checks the
 * officer's permission on every call, because the menu only hides the button.
 */
public class TrustBadgeController {

    private final TrustBadgeService service = new TrustBadgeService();

    public List<ReviewCase> getPendingReviews() {
        require(Permission.MODERATE_REVIEWS);
        return service.pendingReviews();
    }

    /** Approves the review and returns the maid's new average rating. */
    public double approveReview(int reviewId) {
        Officer officer = require(Permission.MODERATE_REVIEWS);
        try {
            return service.approve(officer, reviewId);
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public void rejectReview(int reviewId) {
        Officer officer = require(Permission.MODERATE_REVIEWS);
        try {
            service.reject(officer, reviewId);
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public List<MaidStats> getMaidStats() {
        require(Permission.AWARD_BADGES);
        return service.maidStats();
    }

    public void awardBadge(int maidId) {
        Officer officer = require(Permission.AWARD_BADGES);
        try {
            service.awardBadge(officer, maidId);
        } catch (IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        } catch (DuplicateRecordException e) {
            throw new ValidationException(StringsB.UC5_ERR_HAS_BADGE);
        }
    }

    private Officer require(Permission permission) {
        if (Session.isLoggedIn()
                && Session.getUser() instanceof Officer officer
                && officer.getPermissions().contains(permission)) {
            return officer;
        }
        throw new ValidationException(StringsB.UC5_ERR_NOT_ALLOWED);
    }
}