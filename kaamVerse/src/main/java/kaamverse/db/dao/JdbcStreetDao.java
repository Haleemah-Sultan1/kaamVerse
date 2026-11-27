package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.Street;
import kaamverse.common.DatabaseException;

/** JDBC implementation of StreetDao. */
public class JdbcStreetDao implements StreetDao {

    @Override
    public List<Street> findByLocality(int localityId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT street_id, locality_id, name FROM street WHERE locality_id = ? ORDER BY name")) {
            statement.setInt(1, localityId);
            try (ResultSet rows = statement.executeQuery()) {
                List<Street> result = new ArrayList<>();
                while (rows.next()) {
                    Street item = new Street();
                    item.setStreetId(rows.getInt("street_id"));
                    item.setLocalityId(rows.getInt("locality_id"));
                    item.setName(rows.getString("name"));
                    result.add(item);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load streets", e);
        }
    }
}
