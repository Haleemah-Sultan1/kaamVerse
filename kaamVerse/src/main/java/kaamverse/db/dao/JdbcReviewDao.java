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

import kaamverse.bl.model.Review;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.ReviewStatus;

public class JdbcReviewDao implements ReviewDao {

    private static final String INSERT = "INSERT INTO review (hire_id, overall_rating, punctuality, honesty, "
            + "cleanliness, behaviour, would_rehire, comment, is_flagged, status, created_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT = "SELECT r.review_id, r.hire_id, r.overall_rating, r.punctuality, "
            + "r.honesty, r.cleanliness, r.behaviour, r.would_rehire, r.comment, r.is_flagged, r.status, "
            + "r.created_date FROM review r ";

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

    @Override
    public List<Review> findPendingFlagged() {
        return query(SELECT + "WHERE r.status = 'Pending' AND r.is_flagged = TRUE "
                + "ORDER BY r.created_date, r.review_id", null);
    }

    @Override
    public Optional<Review> findById(int reviewId) {
        List<Review> found = query(SELECT + "WHERE r.review_id = ?", reviewId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    @Override
    public boolean decide(int reviewId, ReviewStatus newStatus) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE review SET status = ? WHERE review_id = ? AND status = 'Pending'")) {
            statement.setString(1, newStatus.name());
            statement.setInt(2, reviewId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the decision on the review", e);
        }
    }

    @Override
    public List<Review> findApprovedByMaid(int maidId) {
        return query(SELECT + "JOIN hire h ON h.hire_id = r.hire_id WHERE h.maid_id = ? "
                + "AND r.status = 'Approved' ORDER BY r.created_date DESC, r.review_id DESC", maidId);
    }

    @Override
    public List<Integer> findMaidIdsWithApprovedReviews() {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DISTINCT h.maid_id FROM review r JOIN hire h ON h.hire_id = r.hire_id "
                             + "WHERE r.status = 'Approved' ORDER BY h.maid_id");
             ResultSet rows = statement.executeQuery()) {
            List<Integer> ids = new ArrayList<>();
            while (rows.next()) {
                ids.add(rows.getInt(1));
            }
            return ids;
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maids with reviews", e);
        }
    }

    @Override
    public void saveMaidAverage(int maidId, double average) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE maid SET average_rating = ? WHERE maid_id = ?")) {
            statement.setDouble(1, average);
            statement.setInt(2, maidId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the maid's average rating", e);
        }
    }

    private List<Review> query(String sql, Integer parameter) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setInt(1, parameter);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (rows.next()) {
                    reviews.add(map(rows));
                }
                return reviews;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the reviews", e);
        }
    }

    private Review map(ResultSet rows) throws SQLException {
        Review review = new Review();
        review.setReviewId(rows.getInt("review_id"));
        review.setHireId(rows.getInt("hire_id"));
        review.setOverallRating(rows.getInt("overall_rating"));
        review.setPunctuality(rows.getInt("punctuality"));
        review.setHonesty(rows.getInt("honesty"));
        review.setCleanliness(rows.getInt("cleanliness"));
        review.setBehaviour(rows.getInt("behaviour"));
        review.setWouldRehire(rows.getBoolean("would_rehire"));
        review.setComment(rows.getString("comment"));
        review.setFlagged(rows.getBoolean("is_flagged"));
        review.setStatus(ReviewStatus.valueOf(rows.getString("status")));
        review.setCreatedDate(rows.getObject("created_date", LocalDate.class));
        return review;
    }
}