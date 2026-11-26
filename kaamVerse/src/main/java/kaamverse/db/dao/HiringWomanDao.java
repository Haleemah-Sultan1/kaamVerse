package kaamverse.db.dao;

import java.util.Optional;
import kaamverse.bl.model.HiringWoman;

/** Saves and reads hiring women; their hires are not loaded. */
public interface HiringWomanDao {

    /** Saves a new hiring woman, sets her ID and returns it. */
    int insert(HiringWoman woman);

    void update(HiringWoman woman);

    Optional<HiringWoman> findById(int hiringWomanId);

    Optional<HiringWoman> findByPhone(String phone);
}
