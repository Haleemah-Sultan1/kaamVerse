package kaamverse.bl.model;

import java.util.EnumSet;
import java.util.Set;

import kaamverse.common.OfficerRole;
import kaamverse.common.Permission;

/** Reviews and escalates complaints. */
public class ComplaintReviewOfficer extends Officer {

    public ComplaintReviewOfficer() {
        super(OfficerRole.COMPLAINT_REVIEW);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(Permission.REVIEW_COMPLAINTS, Permission.ESCALATE_COMPLAINTS);
    }
}
