package kaamverse.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import kaamverse.bl.model.FamilyMember;
import kaamverse.common.DatabaseException;

/** JDBC implementation of FamilyMemberDao. */
public class JdbcFamilyMemberDao implements FamilyMemberDao {

    private static final String SELECT = "SELECT family_member_id, maid_id, name, relation, age, "
            + "cnic_or_bform, is_dependent, school, contact_number FROM family_member "
            + "WHERE maid_id = ? ORDER BY family_member_id";
    private static final String INSERT = "INSERT INTO family_member (maid_id, name, relation, age, "
            + "cnic_or_bform, is_dependent, school, contact_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE family_member SET name = ?, relation = ?, age = ?, "
            + "cnic_or_bform = ?, is_dependent = ?, school = ?, contact_number = ? "
            + "WHERE family_member_id = ? AND maid_id = ?";
    private static final String DELETE = "DELETE FROM family_member WHERE family_member_id = ? AND maid_id = ?";

    @Override
    public List<FamilyMember> findByMaid(int maidId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setInt(1, maidId);
            try (ResultSet rows = statement.executeQuery()) {
                List<FamilyMember> result = new ArrayList<>();
                while (rows.next()) {
                    FamilyMember member = new FamilyMember();
                    member.setFamilyMemberId(rows.getInt("family_member_id"));
                    member.setMaidId(rows.getInt("maid_id"));
                    member.setName(rows.getString("name"));
                    member.setRelation(rows.getString("relation"));
                    member.setAge(rows.getInt("age"));
                    member.setCnicOrBForm(rows.getString("cnic_or_bform"));
                    member.setDependent(rows.getBoolean("is_dependent"));
                    member.setSchool(rows.getString("school"));
                    member.setContactNumber(rows.getString("contact_number"));
                    result.add(member);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the family members", e);
        }
    }

    @Override
    public int insert(FamilyMember member) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, member.getMaidId());
            statement.setString(2, member.getName());
            statement.setString(3, member.getRelation());
            statement.setInt(4, member.getAge());
            statement.setString(5, member.getCnicOrBForm());
            statement.setBoolean(6, member.isDependent());
            statement.setString(7, member.getSchool());
            statement.setString(8, member.getContactNumber());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DatabaseException("Could not save the family member: no ID was returned");
                }
                member.setFamilyMemberId(keys.getInt(1));
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the family member", e);
        }
    }

    @Override
    public boolean update(FamilyMember member) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, member.getName());
            statement.setString(2, member.getRelation());
            statement.setInt(3, member.getAge());
            statement.setString(4, member.getCnicOrBForm());
            statement.setBoolean(5, member.isDependent());
            statement.setString(6, member.getSchool());
            statement.setString(7, member.getContactNumber());
            statement.setInt(8, member.getFamilyMemberId());
            statement.setInt(9, member.getMaidId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the family member", e);
        }
    }

    @Override
    public boolean delete(int maidId, int familyMemberId) {
        try (Connection connection = TransactionManager.connection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setInt(1, familyMemberId);
            statement.setInt(2, maidId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete the family member", e);
        }
    }
}
