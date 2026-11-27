package kaamverse.bl.controller;

import java.util.List;
import java.util.Set;

import kaamverse.bl.model.Notification;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.User;
import kaamverse.bl.service.NotificationObserver;
import kaamverse.bl.service.NotificationService;
import kaamverse.bl.service.Session;
import kaamverse.common.Permission;
import kaamverse.common.UserRole;

/** GRASP Controller for the main menu and its notification list. */
public class MainMenuController {

    private final NotificationService notificationService = new NotificationService();

    public String userName() {
        return Session.getUser().getName();
    }

    public UserRole role() {
        return Session.getRole();
    }

    /** Officers have permissions; other users have none. */
    public Set<Permission> permissions() {
        User user = Session.getUser();
        return user instanceof Officer officer ? officer.getPermissions() : Set.of();
    }

    public List<Notification> unreadNotifications() {
        return notificationService.unreadFor(Session.getUser());
    }

    public void markRead(Notification notification) {
        notificationService.markRead(notification);
    }

    public boolean isForCurrentUser(Notification notification) {
        return Session.isLoggedIn()
                && notification.getRecipientType() == Session.getRole()
                && notification.getRecipientId() == Session.getUser().getId();
    }

    public void addObserver(NotificationObserver observer) {
        notificationService.addObserver(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        notificationService.removeObserver(observer);
    }
}
