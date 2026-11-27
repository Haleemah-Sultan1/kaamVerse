package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Hire;
import kaamverse.bl.model.HireService;
import kaamverse.common.DatabaseException;
import kaamverse.common.HireStatus;

public class JdbcHireDao implements HireDao {

    private static final String SELECT = "SELECT hire_id, maid_id, hiring_woman_id, working_hours, request_date, "
            + "start_date, end_date, status, calculated_wage FROM hire ";
    private static final String INSERT = "INSERT INTO hire (maid_id, hiring_woman_id, working_hours, request_date, "
            + "start_date, end_date, status, calculated_wage) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_LINE =
            "INSERT INTO hire_service (hire_id, service_id, quantity, unit, wage) VALUES (?, ?, ?, ?, ?)";

    @Override
    public int insert(Hire hire) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, hire.getMaidId());
            statement.setInt(2, hire.getHiringWomanId());
            statement.setString(3, hire.getWorkingHours());
            setDate(statement, 4, hire.getRequestDate());
            setDate(statement, 5, hire.getStartDate());
            setDate(statement, 6, hire.getEndDate());
            statement.setString(7, hire.getStatus().name());
            statement.setDouble(8, hire.getCalculatedWage());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the hire: no ID was returned");
                }
                hire.setHireId(keys.getInt(1));
            }
            insertLines(connection, hire);
            return hire.getHireId();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the hire", e);
        }
    }

    @Override
    public Optional<Hire> findById(int hireId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE hire_id = ?")) {
            statement.setInt(1, hireId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the hire", e);
        }
    }

    @Override
    public List<Hire> findByWoman(int hiringWomanId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT + "WHERE hiring_woman_id = ? ORDER BY request_date DESC, hire_id DESC")) {
            statement.setInt(1, hiringWomanId);
            try (ResultSet rows = statement.executeQuery()) {
                List<Hire> hires = new ArrayList<>();
                while (rows.next()) {
                    hires.add(map(rows));
                }
                return hires;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the hires", e);
        }
    }

    @Override
    public void updateStatus(int hireId, HireStatus status, LocalDate startDate, LocalDate endDate) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE hire SET status = ?, start_date = ?, end_date = ? WHERE hire_id = ?")) {
            statement.setString(1, status.name());
            setDate(statement, 2, startDate);
            setDate(statement, 3, endDate);
            statement.setInt(4, hireId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the hire", e);
        }
    }

    private void insertLines(Connection connection, Hire hire) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_LINE)) {
            for (HireService line : hire.getServices()) {
                statement.setInt(1, hire.getHireId());
                statement.setInt(2, line.getServiceId());
                statement.setDouble(3, line.getQuantity());
                statement.setString(4, line.getUnit().name());
                statement.setDouble(5, line.getWage());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void setDate(PreparedStatement statement, int index, LocalDate date) throws SQLException {
        if (date == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setObject(index, date);
        }
    }

    private Hire map(ResultSet rows) throws SQLException {
        Hire hire = new Hire();
        hire.setHireId(rows.getInt("hire_id"));
        hire.setMaidId(rows.getInt("maid_id"));
        hire.setHiringWomanId(rows.getInt("hiring_woman_id"));
        hire.setWorkingHours(rows.getString("working_hours"));
        hire.setRequestDate(rows.getObject("request_date", LocalDate.class));
        hire.setStartDate(rows.getObject("start_date", LocalDate.class));
        hire.setEndDate(rows.getObject("end_date", LocalDate.class));
        hire.setStatus(HireStatus.valueOf(rows.getString("status")));
        hire.setCalculatedWage(rows.getDouble("calculated_wage"));
        return hire;
    }
}