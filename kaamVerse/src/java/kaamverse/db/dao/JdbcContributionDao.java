package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Optional;

import kaamverse.bl.model.Contribution;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;

/** JDBC implementation of ContributionDao. */
public class JdbcContributionDao implements ContributionDao {

    /** MySQL error code for a duplicate value in a unique column. */
    private static final int DUPLICATE_ENTRY = 1062;

    private static final String INSERT = "INSERT INTO contribution (request_id, hiring_woman_id, amount, "
            + "note, contribution_date, acknowledgement_no) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String LATEST_ACK = "SELECT acknowledgement_no FROM contribution "
            + "WHERE acknowledgement_no LIKE ? ORDER BY CHAR_LENGTH(acknowledgement_no) DESC, "
            + "acknowledgement_no DESC LIMIT 1";

    @Override
    public int insert(Contribution contribution) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, contribution.getRequestId());
            statement.setInt(2, contribution.getHiringWomanId());
            statement.setDouble(3, contribution.getAmount());
            statement.setString(4, contribution.getNote());
            statement.setObject(5, contribution.getContributionDate());
            statement.setString(6, contribution.getAcknowledgementNo());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the contribution: no ID was returned");
                }
                contribution.setContributionId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) {
                throw new DuplicateRecordException("This acknowledgement number is already used", e);
            }
            throw new DatabaseException("Could not save the contribution", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the contribution", e);
        }
    }

    @Override
    public Optional<String> findLatestAcknowledgementNo(String prefix) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(LATEST_ACK)) {
            statement.setString(1, prefix + "%");
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(rows.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the latest acknowledgement number", e);
        }
    }
}
