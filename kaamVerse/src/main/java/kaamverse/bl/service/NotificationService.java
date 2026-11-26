package kaamverse.bl.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kaamverse.bl.model.Notification;
import kaamverse.bl.model.User;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.NotificationDao;

/**
 * Saves and reads notifications. GoF Observer: this is the subject; open
 * screens register to hear about new notifications.
 */
public class NotificationService {

    /** Shared, so a message sent by any instance reaches every observer. */
    private static final List<NotificationObserver> OBSERVERS = new CopyOnWriteArrayList<>();

    private final NotificationDao notificationDao = DaoFactory.notificationDao();

    /** Saves a notification for the user, then tells the observers. */
    public void send(User user, String text) {
        Notification notification = Notification.forUser(user, text);
        notificationDao.insert(notification);
        for (NotificationObserver observer : OBSERVERS) {
            observer.onNotification(notification);
        }
    }

    public List<Notification> unreadFor(User user) {
        return notificationDao.findUnread(user.getUserRole(), user.getId());
    }

    public void markRead(Notification notification) {
        notificationDao.markRead(notification.getNotificationId());
        notification.setRead(true);
    }

    public void addObserver(NotificationObserver observer) {
        OBSERVERS.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        OBSERVERS.remove(observer);
    }
}
