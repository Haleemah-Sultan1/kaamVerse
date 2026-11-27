package kaamverse.db.dao;

import java.util.List;

import kaamverse.bl.model.WageBand;

/** Saves and reads the wage_band table. */
public interface WageBandDao {

    /** Saves a new band, sets its ID and returns it. */
    int insert(WageBand band);

    /** Marks one band as archived. Archived bands are kept for history. */
    void archive(int bandId);

    /** Every band that is not archived, ordered by service and effective date. */
    List<WageBand> findNotArchived();

    /** All bands of one service, archived or not, oldest first. */
    List<WageBand> findByService(int serviceId);
}