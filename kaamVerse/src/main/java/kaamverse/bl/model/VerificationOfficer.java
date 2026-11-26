package kaamverse.bl.model;

import java.util.EnumSet;
import java.util.Set;

import kaamverse.common.OfficerRole;
import kaamverse.common.Permission;

/** Verifies maid identity. */
public class VerificationOfficer extends Officer {

    public VerificationOfficer() {
        super(OfficerRole.VERIFICATION);
    }

    @Override
    public Set<Permission> getPermissions() {
        return EnumSet.of(Permission.VERIFY_MAIDS);
    }
}
