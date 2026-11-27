package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import kaamverse.bl.model.TrustBadge;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;

/** JDBC implementation of TrustBadgeDao. */
public class JdbcTrustBadgeDao implements TrustBadgeDao {

    private static final String INSERT = "INSERT INTO trust_badge "
            + "(maid_id, awarded_date, reason, awarded_by_officer_id) VALUES (?, ?, ?, ?)";

    @Override
    public int insert(TrustBadge badge) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, badge.getMaidId());
            statement.setObject(2, badge.getAwardedDate());
            statement.setString(3, badge.getReason());
            statement.setInt(4, badge.getAwardedByOfficerId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the badge: no ID was returned");
                }
                badge.setBadgeId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // maid_id is unique: one badge per maid
            throw new DuplicateRecordException("This maid already has a badge", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the badge", e);
        }
    }
}