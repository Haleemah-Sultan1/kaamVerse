package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Maid;
import kaamverse.bl.model.MaidService;
import kaamverse.common.DatabaseException;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.MaidStatus;

/** JDBC implementation of MaidDao. */
public class JdbcMaidDao implements MaidDao {

    private static final String SELECT = "SELECT maid_id, name, father_name, cnic, phone, address, pin, "
            + "cnic_image_path, status, average_rating, street_id, registered_date FROM maid ";
    private static final String INSERT = "INSERT INTO maid (name, father_name, cnic, phone, address, pin, "
            + "cnic_image_path, status, average_rating, street_id, registered_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE maid SET name = ?, father_name = ?, cnic = ?, phone = ?, "
            + "address = ?, pin = ?, cnic_image_path = ?, status = ?, average_rating = ?, street_id = ?, "
            + "registered_date = ? WHERE maid_id = ?";

    @Override
    public int insert(Maid maid) {
        if (maid.getStatus() == null) {
            maid.setStatus(MaidStatus.PendingVerification);
        }
        if (maid.getRegisteredDate() == null) {
            maid.setRegisteredDate(LocalDate.now());
        }
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, maid);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the maid: no ID was returned");
                }
                maid.setMaidId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateRecordException("This phone number or CNIC is already registered", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the maid", e);
        }
    }

    @Override
    public void update(Maid maid) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            bind(statement, maid);
            statement.setInt(12, maid.getMaidId());
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DuplicateRecordException("This phone number or CNIC is already registered", e);
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the maid", e);
        }
    }

    @Override
    public Optional<Maid> findById(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE maid_id = ?")) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maid", e);
        }
    }

    @Override
    public Optional<Maid> findByPhone(String phone) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE phone = ?")) {
            statement.setString(1, phone);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maid", e);
        }
    }

    @Override
    public Optional<Maid> findByCnic(String cnic) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT + "WHERE cnic = ?")) {
            statement.setString(1, cnic);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the maid", e);
        }
    }

    @Override
    public void saveServices(int maidId, List<MaidService> services) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO maid_service (maid_id, service_id, years) VALUES (?, ?, ?)")) {
            for (MaidService service : services) {
                statement.setInt(1, maidId);
                statement.setInt(2, service.getServiceId());
                statement.setInt(3, service.getYears());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the maid's services", e);
        }
    }

    @Override
    public List<Maid> search(String nameOrCnic) {
        String escaped = nameOrCnic.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String pattern = "%" + escaped + "%";
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT
                     + "WHERE name LIKE ? OR cnic LIKE ? OR REPLACE(cnic, '-', '') LIKE ? ORDER BY name LIMIT 50")) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet rows = statement.executeQuery()) {
                List<Maid> result = new java.util.ArrayList<>();
                while (rows.next()) {
                    result.add(map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not search for maids", e);
        }
    }

    @Override
    public boolean hasTrustBadge(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT 1 FROM trust_badge WHERE maid_id = ?")) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the maid's trust badge", e);
        }
    }

    private void bind(PreparedStatement statement, Maid maid) throws SQLException {
        statement.setString(1, maid.getName());
        statement.setString(2, maid.getFatherName());
        statement.setString(3, maid.getCnic());
        statement.setString(4, maid.getPhone());
        statement.setString(5, maid.getAddress());
        statement.setString(6, maid.getPin());
        statement.setString(7, maid.getCnicImagePath());
        statement.setString(8, maid.getStatus().name());
        statement.setDouble(9, maid.getAverageRating());
        statement.setInt(10, maid.getStreetId());
        statement.setObject(11, maid.getRegisteredDate());
    }

    private Maid map(ResultSet rows) throws SQLException {
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
}
