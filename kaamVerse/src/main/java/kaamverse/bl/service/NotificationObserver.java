package kaamverse.bl.service;

import kaamverse.bl.model.Notification;

/** GoF Observer: told about each notification as it is sent. */
public interface NotificationObserver {

    void onNotification(Notification notification);
}
