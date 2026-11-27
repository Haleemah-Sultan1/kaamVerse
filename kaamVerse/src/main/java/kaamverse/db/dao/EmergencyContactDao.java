package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.EmergencyContact;

/** Saves and reads the emergency contacts of a maid. */
public interface EmergencyContactDao {

    List<EmergencyContact> findByMaid(int maidId);

    /** Saves a new contact, sets its ID and returns it. */
    int insert(EmergencyContact contact);

    /** Deletes one contact of the maid; false if it no longer exists. */
    boolean delete(int maidId, int contactId);
}
