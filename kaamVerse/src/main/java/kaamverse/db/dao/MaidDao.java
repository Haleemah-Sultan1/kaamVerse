package kaamverse.db.dao;

import java.util.Optional;
import kaamverse.bl.model.Maid;

/** Saves and reads the maid table only; related lists (services, slots, family) are not loaded. */
public interface MaidDao {

    /** Saves a new maid, sets her ID and returns it. */
    int insert(Maid maid);

    void update(Maid maid);

    Optional<Maid> findById(int maidId);

    Optional<Maid> findByPhone(String phone);

    Optional<Maid> findByCnic(String cnic);
}
