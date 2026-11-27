package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.WageBand;
import kaamverse.common.DatabaseException;
import kaamverse.common.WageUnit;

/** JDBC implementation of WageBandDao. */
public class JdbcWageBandDao implements WageBandDao {

    private static final String SELECT =
            "SELECT band_id, service_id, min_rate, max_rate, unit, effective_date, is_archived FROM wage_band ";
    private static final String INSERT = "INSERT INTO wage_band "
            + "(service_id, min_rate, max_rate, unit, effective_date, is_archived) VALUES (?, ?, ?, ?, ?, ?)";

    @Override
    public int insert(WageBand band) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, band.getServiceId());
            statement.setDouble(2, band.getMinRate());
            statement.setDouble(3, band.getMaxRate());
            statement.setString(4, band.getUnit().name());
            statement.setObject(5, band.getEffectiveDate());
            statement.setBoolean(6, band.isArchived());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the wage band: no ID was returned");
                }
                band.setBandId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the wage band", e);
        }
    }

    @Override
    public void archive(int bandId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement("UPDATE wage_band SET is_archived = TRUE WHERE band_id = ?")) {
            statement.setInt(1, bandId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not archive the wage band", e);
        }
    }

    @Override
    public List<WageBand> findNotArchived() {
        return query(SELECT + "WHERE is_archived = FALSE ORDER BY service_id, effective_date, band_id", null);
    }

    @Override
    public List<WageBand> findByService(int serviceId) {
        return query(SELECT + "WHERE service_id = ? ORDER BY effective_date, band_id", serviceId);
    }

    private List<WageBand> query(String sql, Integer serviceId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (serviceId != null) {
                statement.setInt(1, serviceId);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<WageBand> bands = new ArrayList<>();
                while (rows.next()) {
                    bands.add(map(rows));
                }
                return bands;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the wage bands", e);
        }
    }

    private WageBand map(ResultSet rows) throws SQLException {
        WageBand band = new WageBand();
        band.setBandId(rows.getInt("band_id"));
        band.setServiceId(rows.getInt("service_id"));
        band.setMinRate(rows.getDouble("min_rate"));
        band.setMaxRate(rows.getDouble("max_rate"));
        band.setUnit(WageUnit.valueOf(rows.getString("unit")));
        band.setEffectiveDate(rows.getObject("effective_date", LocalDate.class));
        band.setArchived(rows.getBoolean("is_archived"));
        return band;
    }
}