package kaamverse.bl.model;

import java.util.Set;

import kaamverse.common.OfficerRole;
import kaamverse.common.Permission;
import kaamverse.common.UserRole;

/** Base class for officers. GRASP Polymorphism: each subclass supplies its own permissions. */
public abstract class Officer implements User {

    private int officerId;
    private String name;
    private String username;
    private String passwordHash;
    private OfficerRole role;

    protected Officer(OfficerRole role) {
        this.role = role;
    }

    public int getOfficerId() {
        return officerId;
    }

    public void setOfficerId(int officerId) {
        this.officerId = officerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public OfficerRole getRole() {
        return role;
    }

    public void setRole(OfficerRole role) {
        this.role = role;
    }

    @Override
    public int getId() {
        return officerId;
    }

    @Override
    public UserRole getUserRole() {
        return UserRole.OFFICER;
    }

    /** Overridden by every subclass; the main menu is built from this. */
    public abstract Set<Permission> getPermissions();
}
