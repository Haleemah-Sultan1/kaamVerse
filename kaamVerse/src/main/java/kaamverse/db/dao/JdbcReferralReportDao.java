package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import kaamverse.bl.model.ReferralReport;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;

public class JdbcReferralReportDao implements ReferralReportDao {

    private static final String INSERT = "INSERT INTO referral_report (complaint_id, authority, reason, "
            + "generated_date) VALUES (?, ?, ?, ?)";
    private static final String BY_COMPLAINT = "SELECT referral_id, complaint_id, authority, reason, "
            + "generated_date FROM referral_report WHERE complaint_id = ?";

    @Override
    public int insert(ReferralReport report) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, report.getComplaintId());
            statement.setString(2, report.getAuthority());
            statement.setString(3, report.getReason());
            statement.setObject(4, report.getGeneratedDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the referral: no ID was returned");
                }
                report.setReferralId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateRecordException("This complaint already has a referral report", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the referral report", e);
        }
    }

    @Override
    public Optional<ReferralReport> findByComplaint(int complaintId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(BY_COMPLAINT)) {
            statement.setInt(1, complaintId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                ReferralReport report = new ReferralReport();
                report.setReferralId(rows.getInt("referral_id"));
                report.setComplaintId(rows.getInt("complaint_id"));
                report.setAuthority(rows.getString("authority"));
                report.setReason(rows.getString("reason"));
                report.setGeneratedDate(rows.getObject("generated_date", LocalDate.class));
                return Optional.of(report);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the referral report", e);
        }
    }
}