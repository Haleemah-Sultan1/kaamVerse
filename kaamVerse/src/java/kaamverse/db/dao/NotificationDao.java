package kaamverse.db.dao;

import java.util.List;
import kaamverse.bl.model.Notification;
import kaamverse.common.UserRole;

/** Saves and reads notifications. */
public interface NotificationDao {

    /** Saves a notification, sets its ID and returns it. */
    int insert(Notification notification);

    /** Unread notifications for one user, newest first. */
    List<Notification> findUnread(UserRole recipientType, int recipientId);

    void markRead(int notificationId);
}
