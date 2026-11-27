package kaamverse.bl.model;

import java.time.LocalDateTime;

import kaamverse.common.UserRole;

/** A message for a user. Saving it is the job of the notification service. */
public class Notification {

    private int notificationId;
    private UserRole recipientType;
    private int recipientId;
    private String text;
    private boolean isRead;
    private LocalDateTime createdAt;

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public UserRole getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(UserRole recipientType) {
        this.recipientType = recipientType;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** GRASP Creator: builds an unread notification for any kind of user. */
    public static Notification forUser(User user, String text) {
        Notification notification = new Notification();
        notification.setRecipientType(user.getUserRole());
        notification.setRecipientId(user.getId());
        notification.setText(text);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
