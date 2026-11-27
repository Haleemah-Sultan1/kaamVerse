package kaamverse.db.dao;

import java.util.List;
import java.util.Optional;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.MaidService;

/** Saves and reads the maid table only; related lists (services, slots, family) are not loaded. */
public interface MaidDao {

    /** Saves a new maid, sets her ID and returns it. */
    int insert(Maid maid);

    void update(Maid maid);

    Optional<Maid> findById(int maidId);

    Optional<Maid> findByPhone(String phone);

    Optional<Maid> findByCnic(String cnic);

    /** Adds the services a maid offers, with her years of experience. */
    void saveServices(int maidId, List<MaidService> services);

    /** UC8: maids whose name or CNIC contains the text (at most 50), ordered by name. */
    List<Maid> search(String nameOrCnic);

    /** UC10: true if the maid has been awarded a Verified Trust Badge. */
    boolean hasTrustBadge(int maidId);
}
