package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Optional;

import kaamverse.bl.model.Complaint;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;

public class JdbcComplaintDao implements ComplaintDao {

    private static final String INSERT = "INSERT INTO complaint (reference_no, maid_id, hire_id, problem_type, "
            + "urgency, transcript, audio_path, evidence_path, incident_date, status, filed_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String LATEST = "SELECT reference_no FROM complaint WHERE reference_no LIKE ? "
            + "ORDER BY reference_no DESC LIMIT 1";

    @Override
    public int insert(Complaint complaint) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, complaint.getReferenceNo());
            statement.setInt(2, complaint.getMaidId());
            statement.setInt(3, complaint.getHireId());
            statement.setString(4, complaint.getProblemType());
            statement.setString(5, complaint.getUrgency());
            statement.setString(6, complaint.getTranscript());
            statement.setString(7, complaint.getAudioPath());
            statement.setString(8, complaint.getEvidencePath());
            statement.setObject(9, complaint.getIncidentDate());
            statement.setString(10, complaint.getStatus().name());
            statement.setObject(11, complaint.getFiledDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the complaint: no ID was returned");
                }
                complaint.setComplaintId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // reference_no is unique; the service tries the next number
            throw new DuplicateRecordException("This complaint reference number is already used", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the complaint", e);
        }
    }

    @Override
    public Optional<String> findLatestReferenceNo(String prefix) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(LATEST)) {
            statement.setString(1, prefix + "%");
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(rows.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the latest reference number", e);
        }
    }
}