package kaamverse.bl.controller;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import kaamverse.bl.model.Officer;
import kaamverse.bl.model.VerificationCase;
import kaamverse.bl.service.Session;
import kaamverse.bl.service.VerificationService;
import kaamverse.common.Permission;
import kaamverse.common.Strings;

/**
 * GRASP Controller for UC2 Verify Worker Identity. The screen talks only to
 * this class; it checks the officer's permission on every call, because the
 * menu only hides the button.
 * Pattern: findPending, selectRequest (getDetails), markMatched / reject.
 */
public class VerificationController {

    /** Length of verification_request.rejection_reason. */
    private static final int MAX_REASON_LENGTH = 500;

    private final VerificationService service = new VerificationService();

    public List<VerificationCase> getPendingRequests() {
        requireVerifier();
        return service.pendingCases();
    }

    /** Loads the latest details of a request that is still pending. */
    public VerificationCase selectRequest(int requestId) {
        requireVerifier();
        return service.pendingCase(requestId)
                .orElseThrow(() -> new ValidationException(Strings.UC2_ERR_NOT_PENDING));
    }

    /** The CNIC image file for the detail view; empty if it is missing. */
    public Optional<Path> getCnicImage(VerificationCase item) {
        requireVerifier();
        return service.cnicImageFile(item.maid());
    }

    /** Request becomes Matched and the maid Verified. */
    public void markMatched(int requestId) {
        Officer officer = requireVerifier();
        if (!service.markMatched(officer, requestId)) {
            throw new ValidationException(Strings.UC2_ERR_NOT_PENDING);
        }
    }

    /** Request becomes Rejected, with a required reason, and the maid Rejected. */
    public void reject(int requestId, String reason) {
        Officer officer = requireVerifier();
        String checked = validateRejectionReason(reason);
        if (!service.reject(officer, requestId, checked)) {
            throw new ValidationException(Strings.UC2_ERR_NOT_PENDING);
        }
    }

    /** The reason, trimmed; throws ValidationException if empty or too long. */
    public String validateRejectionReason(String reason) {
        String text = reason == null ? "" : reason.trim();
        if (text.isEmpty()) {
            throw new ValidationException(Strings.UC2_ERR_REASON_REQUIRED);
        }
        if (text.length() > MAX_REASON_LENGTH) {
            throw new ValidationException(Strings.UC2_ERR_REASON_TOO_LONG.formatted(MAX_REASON_LENGTH));
        }
        return text;
    }

    private Officer requireVerifier() {
        if (Session.isLoggedIn()
                && Session.getUser() instanceof Officer officer
                && officer.getPermissions().contains(Permission.VERIFY_MAIDS)) {
            return officer;
        }
        throw new ValidationException(Strings.UC2_ERR_NOT_ALLOWED);
    }
}
