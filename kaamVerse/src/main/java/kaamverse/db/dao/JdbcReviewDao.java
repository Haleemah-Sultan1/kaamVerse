package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import kaamverse.bl.model.Review;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;

public class JdbcReviewDao implements ReviewDao {

    private static final String INSERT = "INSERT INTO review (hire_id, overall_rating, punctuality, honesty, "
            + "cleanliness, behaviour, would_rehire, comment, is_flagged, status, created_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    public int insert(Review review) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, review.getHireId());
            statement.setInt(2, review.getOverallRating());
            statement.setInt(3, review.getPunctuality());
            statement.setInt(4, review.getHonesty());
            statement.setInt(5, review.getCleanliness());
            statement.setInt(6, review.getBehaviour());
            statement.setBoolean(7, review.isWouldRehire());
            statement.setString(8, review.getComment());
            statement.setBoolean(9, review.isFlagged());
            statement.setString(10, review.getStatus().name());
            statement.setObject(11, review.getCreatedDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the review: no ID was returned");
                }
                review.setReviewId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // hire_id is unique: one review per hire
            throw new DuplicateRecordException("This hire already has a review", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the review", e);
        }
    }

    @Override
    public boolean existsForHire(int hireId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM review WHERE hire_id = ?")) {
            statement.setInt(1, hireId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the review", e);
        }
    }
}