package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.FamilyMember;

/** Saves and reads the family members of a maid. */
public interface FamilyMemberDao {

    List<FamilyMember> findByMaid(int maidId);

    /** Saves a new member, sets its ID and returns it. */
    int insert(FamilyMember member);

    /** Saves the changes; false if that member of that maid no longer exists. */
    boolean update(FamilyMember member);

    /** Deletes one member of the maid; false if it no longer exists. */
    boolean delete(int maidId, int familyMemberId);
}
