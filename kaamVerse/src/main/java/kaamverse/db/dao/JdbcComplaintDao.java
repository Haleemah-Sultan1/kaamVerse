package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Complaint;
import kaamverse.common.ComplaintStatus;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;

public class JdbcComplaintDao implements ComplaintDao {

    private static final String INSERT = "INSERT INTO complaint (reference_no, maid_id, hire_id, problem_type, "
            + "urgency, transcript, audio_path, evidence_path, incident_date, status, filed_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String LATEST = "SELECT reference_no FROM complaint WHERE reference_no LIKE ? "
            + "ORDER BY reference_no DESC LIMIT 1";
    private static final String SELECT = "SELECT complaint_id, reference_no, maid_id, hire_id, problem_type, "
            + "urgency, transcript, audio_path, evidence_path, incident_date, status, outcome, "
            + "clarification_message, filed_date FROM complaint ";

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

    @Override
    public Optional<Complaint> findById(int complaintId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE complaint_id = ?")) {
            statement.setInt(1, complaintId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the complaint", e);
        }
    }

    @Override
    public List<Complaint> findByStatuses(List<ComplaintStatus> statuses) {
        List<Complaint> complaints = new ArrayList<>();
        if (statuses.isEmpty()) {
            return complaints;
        }
        String sql = SELECT + "WHERE status IN (" + marks(statuses.size()) + ") "
                + "ORDER BY urgency = 'High' DESC, filed_date, complaint_id";
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < statuses.size(); i++) {
                statement.setString(i + 1, statuses.get(i).name());
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    complaints.add(map(rows));
                }
            }
            return complaints;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the complaints", e);
        }
    }

    @Override
    public boolean updateDecision(Complaint complaint, List<ComplaintStatus> allowedFrom) {
        String sql = "UPDATE complaint SET status = ?, outcome = ?, clarification_message = ? "
                + "WHERE complaint_id = ? AND status IN (" + marks(allowedFrom.size()) + ")";
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, complaint.getStatus().name());
            statement.setString(2, complaint.getOutcome());
            statement.setString(3, complaint.getClarificationMessage());
            statement.setInt(4, complaint.getComplaintId());
            for (int i = 0; i < allowedFrom.size(); i++) {
                statement.setString(5 + i, allowedFrom.get(i).name());
            }
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the complaint", e);
        }
    }

    private static String marks(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    private Complaint map(ResultSet rows) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setComplaintId(rows.getInt("complaint_id"));
        complaint.setReferenceNo(rows.getString("reference_no"));
        complaint.setMaidId(rows.getInt("maid_id"));
        complaint.setHireId(rows.getInt("hire_id"));
        complaint.setProblemType(rows.getString("problem_type"));
        complaint.setUrgency(rows.getString("urgency"));
        complaint.setTranscript(rows.getString("transcript"));
        complaint.setAudioPath(rows.getString("audio_path"));
        complaint.setEvidencePath(rows.getString("evidence_path"));
        complaint.setIncidentDate(rows.getObject("incident_date", LocalDate.class));
        complaint.setStatus(ComplaintStatus.valueOf(rows.getString("status")));
        complaint.setOutcome(rows.getString("outcome"));
        complaint.setClarificationMessage(rows.getString("clarification_message"));
        complaint.setFiledDate(rows.getObject("filed_date", LocalDate.class));
        return complaint;
    }
}