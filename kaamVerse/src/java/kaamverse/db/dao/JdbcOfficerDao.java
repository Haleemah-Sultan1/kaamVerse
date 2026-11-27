package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import kaamverse.bl.model.ComplaintReviewOfficer;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.ReviewModerator;
import kaamverse.bl.model.VerificationOfficer;
import kaamverse.bl.model.WageStandardsOfficer;
import kaamverse.bl.model.WelfareCaseOfficer;
import kaamverse.common.DatabaseException;
import kaamverse.common.OfficerRole;

/** JDBC implementation of OfficerDao. */
public class JdbcOfficerDao implements OfficerDao {

    private static final String SELECT =
            "SELECT officer_id, name, username, password_hash, role FROM officer ";

    @Override
    public Optional<Officer> findById(int officerId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE officer_id = ?")) {
            statement.setInt(1, officerId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the officer", e);
        }
    }

    @Override
    public Optional<Officer> findByUsername(String username) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the officer", e);
        }
    }

    private Officer map(ResultSet rows) throws SQLException {
        Officer officer = create(OfficerRole.valueOf(rows.getString("role")));
        officer.setOfficerId(rows.getInt("officer_id"));
        officer.setName(rows.getString("name"));
        officer.setUsername(rows.getString("username"));
        officer.setPasswordHash(rows.getString("password_hash"));
        return officer;
    }

    /** GoF Factory: the stored role decides which subclass is created. */
    private static Officer create(OfficerRole role) {
        return switch (role) {
            case WAGE_STANDARDS -> new WageStandardsOfficer();
            case VERIFICATION -> new VerificationOfficer();
            case WELFARE_CASE -> new WelfareCaseOfficer();
            case REVIEW_MODERATOR -> new ReviewModerator();
            case COMPLAINT_REVIEW -> new ComplaintReviewOfficer();
        };
    }
}
