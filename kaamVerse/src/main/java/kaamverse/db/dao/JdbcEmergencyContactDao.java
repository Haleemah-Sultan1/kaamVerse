package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.EmergencyContact;
import kaamverse.common.DatabaseException;

/** JDBC implementation of EmergencyContactDao. */
public class JdbcEmergencyContactDao implements EmergencyContactDao {

    private static final String SELECT = "SELECT contact_id, maid_id, name, relation, phone_number "
            + "FROM emergency_contact WHERE maid_id = ? ORDER BY contact_id";
    private static final String INSERT =
            "INSERT INTO emergency_contact (maid_id, name, relation, phone_number) VALUES (?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM emergency_contact WHERE contact_id = ? AND maid_id = ?";

    @Override
    public List<EmergencyContact> findByMaid(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                List<EmergencyContact> result = new ArrayList<>();
                while (rows.next()) {
                    EmergencyContact contact = new EmergencyContact();
                    contact.setContactId(rows.getInt("contact_id"));
                    contact.setMaidId(rows.getInt("maid_id"));
                    contact.setName(rows.getString("name"));
                    contact.setRelation(rows.getString("relation"));
                    contact.setPhoneNumber(rows.getString("phone_number"));
                    result.add(contact);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the emergency contacts", e);
        }
    }

    @Override
    public int insert(EmergencyContact contact) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, contact.getMaidId());
            statement.setString(2, contact.getName());
            statement.setString(3, contact.getRelation());
            statement.setString(4, contact.getPhoneNumber());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the contact: no ID was returned");
                }
                contact.setContactId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the emergency contact", e);
        }
    }

    @Override
    public boolean delete(int maidId, int contactId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, contactId);
            statement.setInt(2, maidId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete the emergency contact", e);
        }
    }
}
