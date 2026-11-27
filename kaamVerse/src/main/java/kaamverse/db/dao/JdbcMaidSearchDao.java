package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.Maid;
import kaamverse.bl.model.MaidSearchFilter;
import kaamverse.bl.model.MaidService;
import kaamverse.bl.model.Review;
import kaamverse.common.DatabaseException;
import kaamverse.common.MaidStatus;
import kaamverse.common.ReviewStatus;

public class JdbcMaidSearchDao implements MaidSearchDao {

    private static final String SELECT = "SELECT m.maid_id, m.name, m.father_name, m.cnic, m.phone, m.address, "
            + "m.pin, m.cnic_image_path, m.status, m.average_rating, m.street_id, m.registered_date "
            + "FROM maid m JOIN street s ON s.street_id = m.street_id "
            + "WHERE m.status = 'Verified' AND m.average_rating >= ? ";

    @Override
    public List<Maid> findMatching(MaidSearchFilter filter) {
        StringBuilder sql = new StringBuilder(SELECT);
        List<Object> params = new ArrayList<>();
        params.add(filter.minRating());
        if (filter.localityId() != null) {
            sql.append("AND s.locality_id = ? ");
            params.add(filter.localityId());
        }
        if (filter.streetId() != null) {
            sql.append("AND m.street_id = ? ");
            params.add(filter.streetId());
        }
        if (filter.serviceId() != null) {
            sql.append("AND EXISTS (SELECT 1 FROM maid_service ms WHERE ms.maid_id = m.maid_id AND ms.service_id = ?) ");
            params.add(filter.serviceId());
        }
        if (filter.day() != null || filter.timeSlot() != null) {
            // at least one free slot that fits
            sql.append("AND EXISTS (SELECT 1 FROM availability_slot a WHERE a.maid_id = m.maid_id AND a.is_booked = FALSE ");
            if (filter.day() != null) {
                sql.append("AND a.day_of_week = ? ");
                params.add(filter.day().name());
            }
            if (filter.timeSlot() != null) {
                sql.append("AND a.time_slot = ? ");
                params.add(filter.timeSlot().name());
            }
            sql.append(") ");
        }
        sql.append("ORDER BY m.average_rating DESC, m.name, m.maid_id");
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<Maid> maids = new ArrayList<>();
                while (rows.next()) {
                    maids.add(mapMaid(rows));
                }
                return maids;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not search for maids", e);
        }
    }

    @Override
    public List<MaidService> findServices(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT maid_id, service_id, years FROM maid_service WHERE maid_id = ? ORDER BY service_id")) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                List<MaidService> services = new ArrayList<>();
                while (rows.next()) {
                    MaidService item = new MaidService();
                    item.setMaidId(rows.getInt("maid_id"));
                    item.setServiceId(rows.getInt("service_id"));
                    item.setYears(rows.getInt("years"));
                    services.add(item);
                }
                return services;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maid's services", e);
        }
    }

    @Override
    public List<Review> findApprovedReviews(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT r.review_id, r.hire_id, r.overall_rating, r.punctuality, r.honesty, r.cleanliness, "
                     + "r.behaviour, r.would_rehire, r.comment, r.is_flagged, r.status, r.created_date "
                     + "FROM review r JOIN hire h ON h.hire_id = r.hire_id "
                     + "WHERE h.maid_id = ? AND r.status = 'Approved' ORDER BY r.created_date DESC, r.review_id DESC")) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (rows.next()) {
                    reviews.add(mapReview(rows));
                }
                return reviews;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maid's reviews", e);
        }
    }

    private Maid mapMaid(ResultSet rows) throws SQLException {
        Maid maid = new Maid();
        maid.setMaidId(rows.getInt("maid_id"));
        maid.setName(rows.getString("name"));
        maid.setFatherName(rows.getString("father_name"));
        maid.setCnic(rows.getString("cnic"));
        maid.setPhone(rows.getString("phone"));
        maid.setAddress(rows.getString("address"));
        maid.setPin(rows.getString("pin"));
        maid.setCnicImagePath(rows.getString("cnic_image_path"));
        maid.setStatus(MaidStatus.valueOf(rows.getString("status")));
        maid.setAverageRating(rows.getDouble("average_rating"));
        maid.setStreetId(rows.getInt("street_id"));
        maid.setRegisteredDate(rows.getObject("registered_date", LocalDate.class));
        return maid;
    }

    private Review mapReview(ResultSet rows) throws SQLException {
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