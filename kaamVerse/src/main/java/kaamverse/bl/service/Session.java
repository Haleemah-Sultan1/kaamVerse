package kaamverse.bl.service;

import kaamverse.bl.model.User;
import kaamverse.common.UserRole;

/**
 * The logged-in user. The app is a single-user desktop program, so there is
 * one session at a time.
 */
public final class Session {

    private static User user;

    private Session() {
    }

    public static void start(User loggedIn) {
        user = loggedIn;
    }

    public static void end() {
        user = null;
    }

    public static boolean isLoggedIn() {
        return user != null;
    }

    public static User getUser() {
        if (user == null) {
            throw new IllegalStateException("No user is logged in");
        }
        return user;
    }

    public static UserRole getRole() {
        return getUser().getUserRole();
    }
}
