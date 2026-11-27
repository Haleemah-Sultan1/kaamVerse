package kaamverse.db.dao;

import java.util.List;
import java.util.Optional;
import kaamverse.bl.model.HiringWoman;

/** Saves and reads hiring women; their hires are not loaded. */
public interface HiringWomanDao {

    /** Saves a new hiring woman, sets her ID and returns it. */
    int insert(HiringWoman woman);

    void update(HiringWoman woman);

    Optional<HiringWoman> findById(int hiringWomanId);

    Optional<HiringWoman> findByPhone(String phone);

    /**
     * UC9: the employers a maid works for now, that is, those with an Active
     * hire of her. Each employer appears once, ordered by name. Read-only.
     */
    List<HiringWoman> findActiveEmployersOfMaid(int maidId);
}
