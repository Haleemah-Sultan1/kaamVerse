package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.Notification;
import kaamverse.common.DatabaseException;
import kaamverse.common.UserRole;

/** JDBC implementation of NotificationDao. */
public class JdbcNotificationDao implements NotificationDao {

    private static final String INSERT = "INSERT INTO notification "
            + "(recipient_type, recipient_id, message_text, is_read, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_UNREAD = "SELECT notification_id, recipient_type, recipient_id, "
            + "message_text, is_read, created_at FROM notification "
            + "WHERE recipient_type = ? AND recipient_id = ? AND is_read = FALSE "
            + "ORDER BY created_at DESC, notification_id DESC";
    private static final String MARK_READ = "UPDATE notification SET is_read = TRUE WHERE notification_id = ?";

    @Override
    public int insert(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, notification);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the notification: no ID was returned");
                }
                notification.setNotificationId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the notification", e);
        }
    }

    @Override
    public List<Notification> findUnread(UserRole recipientType, int recipientId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT_UNREAD)) {
            statement.setString(1, recipientType.name());
            statement.setInt(2, recipientId);
            try (ResultSet rows = statement.executeQuery()) {
                List<Notification> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load notifications", e);
        }
    }

    @Override
    public void markRead(int notificationId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(MARK_READ)) {
            statement.setInt(1, notificationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the notification", e);
        }
    }

    private void bind(PreparedStatement statement, Notification notification) throws SQLException {
        statement.setString(1, notification.getRecipientType().name());
        statement.setInt(2, notification.getRecipientId());
        statement.setString(3, notification.getText());
        statement.setBoolean(4, notification.isRead());
        statement.setObject(5, notification.getCreatedAt());
    }

    private Notification map(ResultSet rows) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rows.getInt("notification_id"));
        notification.setRecipientType(UserRole.valueOf(rows.getString("recipient_type")));
        notification.setRecipientId(rows.getInt("recipient_id"));
        notification.setText(rows.getString("message_text"));
        notification.setRead(rows.getBoolean("is_read"));
        notification.setCreatedAt(rows.getObject("created_at", LocalDateTime.class));
        return notification;
    }
}
