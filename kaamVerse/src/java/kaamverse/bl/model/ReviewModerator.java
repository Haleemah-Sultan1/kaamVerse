package kaamverse.bl.model;

import java.util.EnumSet;
import java.util.Set;

import kaamverse.common.OfficerRole;
import kaamverse.common.Permission;

/** Moderates reviews and awards badges. */
public class ReviewModerator extends Officer {

    public ReviewModerator() {
        super(OfficerRole.REVIEW_MODERATOR);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(Permission.MODERATE_REVIEWS, Permission.AWARD_BADGES);
    }
}
