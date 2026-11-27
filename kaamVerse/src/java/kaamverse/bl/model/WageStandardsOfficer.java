package kaamverse.bl.model;

import java.util.EnumSet;
import java.util.Set;

import kaamverse.common.OfficerRole;
import kaamverse.common.Permission;

/** Sets standard wage bands. */
public class WageStandardsOfficer extends Officer {

    public WageStandardsOfficer() {
        super(OfficerRole.WAGE_STANDARDS);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(Permission.MANAGE_WAGE_BANDS);
    }
}
