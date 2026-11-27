package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.Service;
import kaamverse.common.DatabaseException;

/** JDBC implementation of ServiceDao. */
public class JdbcServiceDao implements ServiceDao {

    @Override
    public List<Service> findAll() {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT service_id, name FROM service ORDER BY name")) {
            try (ResultSet rows = statement.executeQuery()) {
                List<Service> result = new ArrayList<>();
                while (rows.next()) {
                    Service item = new Service();
                    item.setServiceId(rows.getInt("service_id"));
                    item.setName(rows.getString("name"));
                    result.add(item);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load services", e);
        }
    }
}
