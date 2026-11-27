package kaamverse.bl.service;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import kaamverse.bl.model.AuditLogEntry;
import kaamverse.bl.model.Maid;
import kaamverse.bl.model.Officer;
import kaamverse.bl.model.VerificationCase;
import kaamverse.bl.model.VerificationRequest;
import kaamverse.common.DatabaseException;
import kaamverse.common.MaidStatus;
import kaamverse.common.Strings;
import kaamverse.common.VerificationStatus;
import kaamverse.db.dao.DaoFactory;
import kaamverse.db.dao.MaidDao;
import kaamverse.db.dao.TransactionManager;
import kaamverse.db.dao.VerificationRequestDao;

/** UC2 data access and saving. GRASP High Cohesion: identity verification only. */
public class VerificationService {

    private static final Logger LOG = Logger.getLogger(VerificationService.class.getName());

    /** Length of the action column in audit_log_entry. */
    private static final int MAX_ACTION_LENGTH = 255;

    private final VerificationRequestDao requestDao = DaoFactory.verificationRequestDao();
    private final MaidDao maidDao = DaoFactory.maidDao();
    private final NotificationService notificationService = new NotificationService();

    /** Pending requests, each with its maid, oldest first. */
    public List<VerificationCase> pendingCases() {
        List<VerificationCase> cases = new ArrayList<>();
        for (VerificationRequest request : requestDao.findPending()) {
            maidDao.findById(request.getMaidId())
                    .ifPresent(maid -> cases.add(new VerificationCase(request, maid)));
        }
        return cases;
    }

    /** One request with its maid, or empty if it is gone or already decided. */
    public Optional<VerificationCase> pendingCase(int requestId) {
        return requestDao.findById(requestId)
                .filter(request -> request.getStatus() == VerificationStatus.Pending)
                .flatMap(request -> maidDao.findById(request.getMaidId())
                        .map(maid -> new VerificationCase(request, maid)));
    }

    /** The stored CNIC image, or empty if there is none or the file is missing. */
    public Optional<Path> cnicImageFile(Maid maid) {
        String stored = maid.getCnicImagePath();
        if (stored == null || stored.isBlank()) {
            return Optional.empty();
        }
        try {
            Path file = Path.of(stored);
            return Files.isRegularFile(file) ? Optional.of(file) : Optional.empty();
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    /**
     * Request becomes Matched and the maid Verified.
     *
     * @return false if the request was already decided (nothing was changed)
     */
    public boolean markMatched(Officer officer, int requestId) {
        return decide(officer, requestId, VerificationStatus.Matched, null);
    }

    /**
     * Request becomes Rejected with the reason, and the maid Rejected.
     *
     * @return false if the request was already decided (nothing was changed)
     */
    public boolean reject(Officer officer, int requestId, String reason) {
        return decide(officer, requestId, VerificationStatus.Rejected, reason);
    }

    private boolean decide(Officer officer, int requestId, VerificationStatus outcome, String reason) {
        Optional<VerificationRequest> found = requestDao.findById(requestId)
                .filter(request -> request.getStatus() == VerificationStatus.Pending);
        if (found.isEmpty()) {
            return false;
        }
        VerificationRequest request = found.get();
        boolean matched = outcome == VerificationStatus.Matched;
        if (matched) {
            request.markVerified(officer.getOfficerId());
        } else {
            request.markRejected(officer.getOfficerId(), reason);
        }

        String action = matched
                ? Strings.UC2_AUDIT_MATCHED.formatted(requestId, request.getMaidId())
                : Strings.UC2_AUDIT_REJECTED.formatted(requestId, request.getMaidId(), reason);

        // Request, maid and audit entry change together, or not at all. The maid
        // is read again inside the transaction so that no stale value is written back.
        // call() is run() for work that returns a value.
        Maid decided = TransactionManager.call(() -> {
            if (!requestDao.saveDecision(request)) {
                return null; // someone else decided it first
            }
            Maid maid = maidDao.findById(request.getMaidId())
                    .orElseThrow(() -> new DatabaseException("The maid for this request no longer exists"));
            maid.setStatus(matched ? MaidStatus.Verified : MaidStatus.Rejected);
            maidDao.update(maid);
            AuditLogEntry.log(officer, truncate(action));
            return maid;
        });
        if (decided == null) {
            return false;
        }

        // After the commit: the decision is saved, so a failed message is only logged.
        try {
            notificationService.send(decided,
                    matched ? Strings.UC2_NOTIFY_MATCHED : Strings.UC2_NOTIFY_REJECTED.formatted(reason));
        } catch (DatabaseException e) {
            LOG.log(Level.WARNING, "Could not notify maid " + decided.getMaidId(), e);
        }
        return true;
    }

    private static String truncate(String action) {
        return action.length() <= MAX_ACTION_LENGTH
                ? action
                : action.substring(0, MAX_ACTION_LENGTH - 3) + "...";
    }
}
