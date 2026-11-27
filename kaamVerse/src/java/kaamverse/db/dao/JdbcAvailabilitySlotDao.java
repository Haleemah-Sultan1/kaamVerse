package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.AvailabilitySlot;
import kaamverse.common.DatabaseException;
import kaamverse.common.TimeSlot;
import kaamverse.common.WeekDay;

/** JDBC implementation of AvailabilitySlotDao. */
public class JdbcAvailabilitySlotDao implements AvailabilitySlotDao {

    private static final String SELECT = "SELECT slot_id, maid_id, day_of_week, time_slot, is_booked "
            + "FROM availability_slot WHERE maid_id = ? ORDER BY slot_id";
    private static final String DELETE = "DELETE FROM availability_slot WHERE maid_id = ?";
    private static final String INSERT =
            "INSERT INTO availability_slot (maid_id, day_of_week, time_slot, is_booked) VALUES (?, ?, ?, ?)";

    @Override
    public List<AvailabilitySlot> findByMaid(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                List<AvailabilitySlot> result = new ArrayList<>();
                while (rows.next()) {
                    AvailabilitySlot slot = new AvailabilitySlot();
                    slot.setSlotId(rows.getInt("slot_id"));
                    slot.setMaidId(rows.getInt("maid_id"));
                    slot.setDay(WeekDay.valueOf(rows.getString("day_of_week")));
                    slot.setTimeSlot(TimeSlot.valueOf(rows.getString("time_slot")));
                    slot.setBooked(rows.getBoolean("is_booked"));
                    result.add(slot);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the availability", e);
        }
    }

    @Override
    public void deleteByMaid(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, maidId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not clear the availability", e);
        }
    }

    @Override
    public void insertAll(List<AvailabilitySlot> slots) {
        if (slots.isEmpty()) {
            return;
        }
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            for (AvailabilitySlot slot : slots) {
                statement.setInt(1, slot.getMaidId());
                statement.setString(2, slot.getDay().name());
                statement.setString(3, slot.getTimeSlot().name());
                statement.setBoolean(4, slot.isBooked());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the availability", e);
        }
    }
}
