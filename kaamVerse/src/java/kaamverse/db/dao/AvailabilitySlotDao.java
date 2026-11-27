package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.AvailabilitySlot;

/** Saves and reads the weekly availability slots of a maid. */
public interface AvailabilitySlotDao {

    /** All slots of one maid, with their booked flag. */
    List<AvailabilitySlot> findByMaid(int maidId);

    /** Deletes all slots of one maid. */
    void deleteByMaid(int maidId);

    /** Saves the slots (each with its maid ID and booked flag). */
    void insertAll(List<AvailabilitySlot> slots);
}
