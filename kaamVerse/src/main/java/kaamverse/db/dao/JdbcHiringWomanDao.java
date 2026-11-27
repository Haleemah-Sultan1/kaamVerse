package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.HiringWoman;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.HireStatus;

/** JDBC implementation of HiringWomanDao. */
public class JdbcHiringWomanDao implements HiringWomanDao {

    private static final String SELECT =
            "SELECT hiring_woman_id, name, phone, address, pin, is_hiring_suspended FROM hiring_woman ";
    private static final String INSERT =
            "INSERT INTO hiring_woman (name, phone, address, pin, is_hiring_suspended) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE hiring_woman SET name = ?, phone = ?, address = ?, pin = ?, "
            + "is_hiring_suspended = ? WHERE hiring_woman_id = ?";
    private static final String SELECT_ACTIVE_EMPLOYERS = "SELECT DISTINCT w.hiring_woman_id, w.name, w.phone, "
            + "w.address, w.pin, w.is_hiring_suspended FROM hiring_woman w "
            + "JOIN hire h ON h.hiring_woman_id = w.hiring_woman_id "
            + "WHERE h.maid_id = ? AND h.status = ? ORDER BY w.name, w.hiring_woman_id";

    @Override
    public int insert(HiringWoman woman) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, woman);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the hiring woman: no ID was returned");
                }
                woman.setHiringWomanId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateRecordException("This phone number is already registered", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the hiring woman", e);
        }
    }

    @Override
    public void update(HiringWoman woman) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bind(statement, woman);
            statement.setInt(6, woman.getHiringWomanId());
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateRecordException("This phone number is already registered", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the hiring woman", e);
        }
    }

    @Override
    public Optional<HiringWoman> findById(int hiringWomanId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE hiring_woman_id = ?")) {
            statement.setInt(1, hiringWomanId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the hiring woman", e);
        }
    }

    @Override
    public Optional<HiringWoman> findByPhone(String phone) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE phone = ?")) {
            statement.setString(1, phone);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the hiring woman", e);
        }
    }

    @Override
    public List<HiringWoman> findActiveEmployersOfMaid(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ACTIVE_EMPLOYERS)) {
            statement.setInt(1, maidId);
            statement.setString(2, HireStatus.Active.name());
            try (ResultSet rows = statement.executeQuery()) {
                List<HiringWoman> employers = new ArrayList<>();
                while (rows.next()) {
                    employers.add(map(rows));
                }
                return employers;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maid's current employers", e);
        }
    }

    private void bind(PreparedStatement statement, HiringWoman woman) throws SQLException {
        statement.setString(1, woman.getName());
        statement.setString(2, woman.getPhone());
        statement.setString(3, woman.getAddress());
        statement.setString(4, woman.getPin());
        statement.setBoolean(5, woman.isHiringSuspended());
    }

    private HiringWoman map(ResultSet rows) throws SQLException {
        HiringWoman woman = new HiringWoman();
        woman.setHiringWomanId(rows.getInt("hiring_woman_id"));
        woman.setName(rows.getString("name"));
        woman.setPhone(rows.getString("phone"));
        woman.setAddress(rows.getString("address"));
        woman.setPin(rows.getString("pin"));
        woman.setHiringSuspended(rows.getBoolean("is_hiring_suspended"));
        return woman;
    }
}
