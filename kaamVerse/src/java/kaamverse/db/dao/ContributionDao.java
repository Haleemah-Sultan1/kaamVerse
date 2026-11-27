package kaamverse.db.dao;

import java.util.Optional;

import kaamverse.bl.model.Contribution;

/** Saves and reads contributions. */
public interface ContributionDao {

    /**
     * UC10: saves a contribution, sets its ID and returns it.
     *
     * @throws kaamverse.common.DuplicateRecordException if the acknowledgement number is already used
     */
    int insert(Contribution contribution);

    /** The highest acknowledgement number that starts with the prefix (for example "AK-2026"), if any. */
    Optional<String> findLatestAcknowledgementNo(String prefix);
}
