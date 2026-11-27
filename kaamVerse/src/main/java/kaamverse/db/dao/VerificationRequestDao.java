package kaamverse.db.dao;

import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.VerificationRequest;

/** Saves and reads verification requests. */
public interface VerificationRequestDao {

    /** Saves a new request, sets its ID and returns it. */
    int insert(VerificationRequest request);

    /** All requests still waiting for a decision, oldest first. */
    List<VerificationRequest> findPending();

    Optional<VerificationRequest> findById(int requestId);

    /**
     * UC2: saves the officer's decision (status, reason, officer, time), but only
     * if the request is still Pending, so two officers cannot both decide it.
     *
     * @return true if saved; false if the request was already decided or does not exist
     */
    boolean saveDecision(VerificationRequest request);
}
