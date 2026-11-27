package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.AuditLogEntry;
import kaamverse.common.DatabaseException;

/** JDBC implementation of AuditLogDao. */
public class JdbcAuditLogDao implements AuditLogDao {

    private static final String INSERT =
            "INSERT INTO audit_log_entry (officer_id, action, logged_at) VALUES (?, ?, ?)";
    private static final String SELECT_BY_OFFICER = "SELECT log_id, officer_id, action, logged_at "
            + "FROM audit_log_entry WHERE officer_id = ? ORDER BY logged_at DESC, log_id DESC";

    @Override
    public int insert(AuditLogEntry entry) {
        if (entry.getTimestamp() == null) {
            entry.setTimestamp(LocalDateTime.now());
        }
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, entry);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the audit log entry: no ID was returned");
                }
                entry.setLogId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the audit log entry", e);
        }
    }

    @Override
    public List<AuditLogEntry> findByOfficer(int officerId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_OFFICER)) {
            statement.setInt(1, officerId);
            try (ResultSet rows = statement.executeQuery()) {
                List<AuditLogEntry> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the audit log", e);
        }
    }

    private void bind(PreparedStatement statement, AuditLogEntry entry) throws SQLException {
        statement.setInt(1, entry.getOfficerId());
        statement.setString(2, entry.getAction());
        statement.setObject(3, entry.getTimestamp());
    }

    private AuditLogEntry map(ResultSet rows) throws SQLException {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setLogId(rows.getInt("log_id"));
        entry.setOfficerId(rows.getInt("officer_id"));
        entry.setAction(rows.getString("action"));
        entry.setTimestamp(rows.getObject("logged_at", LocalDateTime.class));
        return entry;
    }
}
