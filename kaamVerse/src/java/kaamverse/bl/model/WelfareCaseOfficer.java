package kaamverse.bl.model;

import java.util.EnumSet;
import java.util.Set;

import kaamverse.common.OfficerRole;
import kaamverse.common.Permission;

/** Maintains family records. */
public class WelfareCaseOfficer extends Officer {

    public WelfareCaseOfficer() {
        super(OfficerRole.WELFARE_CASE);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(Permission.MANAGE_FAMILY_RECORDS);
    }
}
