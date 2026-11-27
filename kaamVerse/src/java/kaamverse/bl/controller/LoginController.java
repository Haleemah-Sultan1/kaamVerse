package kaamverse.bl.controller;

import java.util.Optional;

import kaamverse.bl.model.User;
import kaamverse.bl.service.AuthService;
import kaamverse.bl.service.Session;
import kaamverse.common.UserRole;

/** GRASP Controller for logging in and out. */
public class LoginController {

    private final AuthService authService = new AuthService();

    /** Starts a session and returns true if the credentials are correct. */
    public boolean login(UserRole role, String identifier, String secret) {
        Optional<User> user = authService.authenticate(role, identifier.trim(), secret);
        user.ifPresent(Session::start);
        return user.isPresent();
    }

    public boolean isLoggedIn() {
        return Session.isLoggedIn();
    }

    public void logout() {
        Session.end();
    }
}
