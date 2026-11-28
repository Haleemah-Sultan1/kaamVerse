package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.InvestigationNote;
import kaamverse.common.DatabaseException;

public class JdbcInvestigationNoteDao implements InvestigationNoteDao {

    private static final String INSERT = "INSERT INTO investigation_note (complaint_id, officer_id, note_text, "
            + "note_date) VALUES (?, ?, ?, ?)";
    private static final String BY_COMPLAINT = "SELECT note_id, complaint_id, officer_id, note_text, note_date "
            + "FROM investigation_note WHERE complaint_id = ? ORDER BY note_date DESC, note_id DESC";

    @Override
    public int insert(InvestigationNote note) {
        if (note.getNoteDate() == null) {
            note.setNoteDate(LocalDateTime.now());
        }
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, note.getComplaintId());
            statement.setInt(2, note.getOfficerId());
            statement.setString(3, note.getText());
            statement.setObject(4, note.getNoteDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the note: no ID was returned");
                }
                note.setNoteId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the investigation note", e);
        }
    }

    @Override
    public List<InvestigationNote> findByComplaint(int complaintId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(BY_COMPLAINT)) {
            statement.setInt(1, complaintId);
            try (ResultSet rows = statement.executeQuery()) {
                List<InvestigationNote> notes = new ArrayList<>();
                while (rows.next()) {
                    InvestigationNote note = new InvestigationNote();
                    note.setNoteId(rows.getInt("note_id"));
                    note.setComplaintId(rows.getInt("complaint_id"));
                    note.setOfficerId(rows.getInt("officer_id"));
                    note.setText(rows.getString("note_text"));
                    note.setNoteDate(rows.getObject("note_date", LocalDateTime.class));
                    notes.add(note);
                }
                return notes;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the investigation notes", e);
        }
    }
}