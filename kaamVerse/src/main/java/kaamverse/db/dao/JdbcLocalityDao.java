package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.Locality;
import kaamverse.common.DatabaseException;

/** JDBC implementation of LocalityDao. */
public class JdbcLocalityDao implements LocalityDao {

    @Override
    public List<Locality> findAll() {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT locality_id, name FROM locality ORDER BY name")) {
            try (ResultSet rows = statement.executeQuery()) {
                List<Locality> result = new ArrayList<>();
                while (rows.next()) {
                    Locality item = new Locality();
                    item.setLocalityId(rows.getInt("locality_id"));
                    item.setName(rows.getString("name"));
                    result.add(item);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load localities", e);
        }
    }
}
