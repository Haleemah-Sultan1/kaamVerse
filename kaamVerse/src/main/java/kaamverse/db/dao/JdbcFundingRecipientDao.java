package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import kaamverse.bl.model.FundingRecipient;
import kaamverse.common.DatabaseException;

/** JDBC implementation of FundingRecipientDao. */
public class JdbcFundingRecipientDao implements FundingRecipientDao {

    private static final String INSERT =
            "INSERT INTO funding_recipient (request_id, hiring_woman_id) VALUES (?, ?)";

    private static final String EXISTS =
            "SELECT 1 FROM funding_recipient WHERE request_id = ? AND hiring_woman_id = ?";

    @Override
    public void insertAll(List<FundingRecipient> recipients) {
        if (recipients.isEmpty()) {
            return;
        }
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            for (FundingRecipient recipient : recipients) {
                statement.setInt(1, recipient.getRequestId());
                statement.setInt(2, recipient.getHiringWomanId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the funding request recipients", e);
        }
    }

    @Override
    public boolean isRecipient(int requestId, int hiringWomanId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(EXISTS)) {
            statement.setInt(1, requestId);
            statement.setInt(2, hiringWomanId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the funding request recipient", e);
        }
    }
}
