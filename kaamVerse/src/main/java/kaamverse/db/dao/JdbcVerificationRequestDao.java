package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.VerificationRequest;
import kaamverse.common.DatabaseException;
import kaamverse.common.VerificationStatus;

/** JDBC implementation of VerificationRequestDao. */
public class JdbcVerificationRequestDao implements VerificationRequestDao {

    private static final String INSERT =
            "INSERT INTO verification_request (maid_id, status, submitted_date) VALUES (?, ?, ?)";
    private static final String SELECT = "SELECT request_id, maid_id, status, rejection_reason, "
            + "verified_by_officer_id, verified_date_time, submitted_date FROM verification_request ";
    private static final String SAVE_DECISION = "UPDATE verification_request SET status = ?, "
            + "rejection_reason = ?, verified_by_officer_id = ?, verified_date_time = ? "
            + "WHERE request_id = ? AND status = ?";

    @Override
    public int insert(VerificationRequest request) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, request.getMaidId());
            statement.setString(2, request.getStatus().name());
            statement.setObject(3, request.getSubmittedDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the verification request: no ID was returned");
                }
                request.setRequestId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the verification request", e);
        }
    }

    @Override
    public List<VerificationRequest> findPending() {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT + "WHERE status = ? ORDER BY submitted_date, request_id")) {
            statement.setString(1, VerificationStatus.Pending.name());
            try (ResultSet rows = statement.executeQuery()) {
                List<VerificationRequest> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the pending verification requests", e);
        }
    }

    @Override
    public Optional<VerificationRequest> findById(int requestId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE request_id = ?")) {
            statement.setInt(1, requestId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the verification request", e);
        }
    }

    @Override
    public boolean saveDecision(VerificationRequest request) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SAVE_DECISION)) {
            statement.setString(1, request.getStatus().name());
            statement.setString(2, request.getRejectionReason());
            statement.setObject(3, request.getVerifiedByOfficerId());
            statement.setObject(4, request.getVerifiedDateTime());
            statement.setInt(5, request.getRequestId());
            statement.setString(6, VerificationStatus.Pending.name());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the verification decision", e);
        }
    }

    private VerificationRequest map(ResultSet rows) throws SQLException {
        VerificationRequest request = new VerificationRequest();
        request.setRequestId(rows.getInt("request_id"));
        request.setMaidId(rows.getInt("maid_id"));
        request.setStatus(VerificationStatus.valueOf(rows.getString("status")));
        request.setRejectionReason(rows.getString("rejection_reason"));
        request.setVerifiedByOfficerId(rows.getObject("verified_by_officer_id", Integer.class));
        request.setVerifiedDateTime(rows.getObject("verified_date_time", LocalDateTime.class));
        request.setSubmittedDate(rows.getObject("submitted_date", LocalDate.class));
        return request;
    }
}
