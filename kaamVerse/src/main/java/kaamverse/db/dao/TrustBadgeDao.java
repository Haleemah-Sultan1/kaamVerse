package kaamverse.db.dao;

import kaamverse.bl.model.TrustBadge;

/** Saves the trust_badge table. */
public interface TrustBadgeDao {

    /** Saves the badge, sets its ID and returns it. Throws DuplicateRecordException if the maid already has one. */
    int insert(TrustBadge badge);
}