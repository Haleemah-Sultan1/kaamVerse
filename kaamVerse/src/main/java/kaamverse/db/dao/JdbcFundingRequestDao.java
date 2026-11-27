package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.FundingRequest;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.FundingStatus;

/** JDBC implementation of FundingRequestDao. */
public class JdbcFundingRequestDao implements FundingRequestDao {

    /** MySQL error code for a duplicate value in a unique column. */
    private static final int DUPLICATE_ENTRY = 1062;

    private static final String INSERT = "INSERT INTO funding_request (maid_id, tracking_no, event_type, "
            + "amount_needed, amount_received, event_date, reason, status, created_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String LATEST_TRACKING_NO = "SELECT tracking_no FROM funding_request "
            + "WHERE tracking_no LIKE ? ORDER BY CHAR_LENGTH(tracking_no) DESC, tracking_no DESC LIMIT 1";

    private static final String COLUMNS = "fr.request_id, fr.maid_id, fr.tracking_no, fr.event_type, "
            + "fr.amount_needed, fr.amount_received, fr.event_date, fr.reason, fr.status, fr.created_date";
    private static final String SELECT_BY_ID =
            "SELECT " + COLUMNS + " FROM funding_request fr WHERE fr.request_id = ?";
    private static final String SELECT_OPEN_FOR_RECIPIENT = "SELECT " + COLUMNS + " FROM funding_request fr "
            + "JOIN funding_recipient rc ON rc.request_id = fr.request_id "
            + "WHERE rc.hiring_woman_id = ? AND fr.status IN ('Pending', 'PartiallyFunded') "
            + "ORDER BY fr.created_date DESC, fr.request_id DESC";
    private static final String UPDATE_PROGRESS =
            "UPDATE funding_request SET amount_received = ?, status = ? WHERE request_id = ?";

    @Override
    public int insert(FundingRequest request) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, request.getMaidId());
            statement.setString(2, request.getTrackingNo());
            statement.setString(3, request.getEventType());
            statement.setDouble(4, request.getAmountNeeded());
            statement.setDouble(5, request.getAmountReceived());
            statement.setObject(6, request.getEventDate());
            statement.setString(7, request.getReason());
            statement.setString(8, request.getStatus().name());
            statement.setObject(9, request.getCreatedDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the funding request: no ID was returned");
                }
                request.setRequestId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == DUPLICATE_ENTRY) {
                throw new DuplicateRecordException("This tracking number is already used", e);
            }
            throw new DatabaseException("Could not save the funding request", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the funding request", e);
        }
    }

    @Override
    public Optional<String> findLatestTrackingNo(String prefix) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(LATEST_TRACKING_NO)) {
            statement.setString(1, prefix + "%");
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(rows.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the latest tracking number", e);
        }
    }

    @Override
    public Optional<FundingRequest> findById(int requestId) {
        return selectOne(SELECT_BY_ID, requestId);
    }

    @Override
    public Optional<FundingRequest> findByIdForUpdate(int requestId) {
        return selectOne(SELECT_BY_ID + " FOR UPDATE", requestId);
    }

    @Override
    public List<FundingRequest> findOpenForRecipient(int hiringWomanId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT_OPEN_FOR_RECIPIENT)) {
            statement.setInt(1, hiringWomanId);
            try (ResultSet rows = statement.executeQuery()) {
                List<FundingRequest> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the funding requests", e);
        }
    }

    @Override
    public void updateProgress(FundingRequest request) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROGRESS)) {
            statement.setDouble(1, request.getAmountReceived());
            statement.setString(2, request.getStatus().name());
            statement.setInt(3, request.getRequestId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the funding request", e);
        }
    }

    private Optional<FundingRequest> selectOne(String sql, int requestId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the funding request", e);
        }
    }

    private FundingRequest map(ResultSet rows) throws SQLException {
        FundingRequest request = new FundingRequest();
        request.setRequestId(rows.getInt("request_id"));
        request.setMaidId(rows.getInt("maid_id"));
        request.setTrackingNo(rows.getString("tracking_no"));
        request.setEventType(rows.getString("event_type"));
        request.setAmountNeeded(rows.getDouble("amount_needed"));
        request.setAmountReceived(rows.getDouble("amount_received"));
        request.setEventDate(rows.getObject("event_date", LocalDate.class));
        request.setReason(rows.getString("reason"));
        request.setStatus(FundingStatus.valueOf(rows.getString("status")));
        request.setCreatedDate(rows.getObject("created_date", LocalDate.class));
        return request;
    }
}
