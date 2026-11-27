package kaamverse.db.dao;

import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.FundingRequest;

/** Saves and reads funding requests; recipients and contributions are not loaded. */
public interface FundingRequestDao {

    /**
     * UC9: saves a new request (not its recipients), sets its ID and returns it.
     *
     * @throws kaamverse.common.DuplicateRecordException if the tracking number is already used
     */
    int insert(FundingRequest request);

    /**
     * The highest tracking number that starts with the prefix (for example
     * "FR-2026"), or empty if there is none yet.
     */
    Optional<String> findLatestTrackingNo(String prefix);

    /** UC10: one request as it is now (no recipients or contributions loaded). */
    Optional<FundingRequest> findById(int requestId);

    /**
     * UC10: same as findById, but locks the row until the surrounding
     * transaction ends, so two employers cannot overfund the same request.
     */
    Optional<FundingRequest> findByIdForUpdate(int requestId);

    /** UC10: requests sent to this hiring woman that are Pending or PartiallyFunded, newest first. */
    List<FundingRequest> findOpenForRecipient(int hiringWomanId);

    /** UC10: saves the new amount received and status of a request. */
    void updateProgress(FundingRequest request);
}
