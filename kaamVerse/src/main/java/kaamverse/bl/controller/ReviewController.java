package kaamverse.bl.controller;

import java.util.List;

import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.service.ExperienceService;
import kaamverse.bl.service.ExperienceService.PastHire;
import kaamverse.bl.service.Session;
import kaamverse.common.DuplicateRecordException;
import kaamverse.common.StringsB;

// Controller for UC4. Checks the user and every input itself.
public class ReviewController {

    private static final int MAX_COMMENT = 1000;

    private final ExperienceService service = new ExperienceService();

    public List<PastHire> getPastMaids() {
        return service.endedHires(requireWoman().getHiringWomanId());
    }

    // opens the form for one hire; it must be hers, Ended and not reviewed yet
    public PastHire selectHire(int hireId) {
        PastHire past = service.findEndedHire(requireWoman().getHiringWomanId(), hireId)
                .orElseThrow(() -> new ValidationException(StringsB.UC4_ERR_HIRE));
        if (past.reviewed()) {
            throw new ValidationException(StringsB.UC4_ERR_DUPLICATE);
        }
        return past;
    }

    public void submitReview(int hireId, int overall, int punctuality, int honesty, int cleanliness,
                             int behaviour, boolean wouldRehire, String commentText) {
        selectHire(hireId);
        checkRating(overall, StringsB.UC4_OVERALL);
        checkRating(punctuality, StringsB.UC4_PUNCTUALITY);
        checkRating(honesty, StringsB.UC4_HONESTY);
        checkRating(cleanliness, StringsB.UC4_CLEANLINESS);
        checkRating(behaviour, StringsB.UC4_BEHAVIOUR);
        String comment = commentText == null ? "" : commentText.trim();
        if (comment.length() > MAX_COMMENT) {
            throw new ValidationException(StringsB.UC4_ERR_COMMENT);
        }
        try {
            service.saveReview(hireId, overall, punctuality, honesty, cleanliness, behaviour, wouldRehire,
                    comment.isEmpty() ? null : comment);
        } catch (DuplicateRecordException e) {
            throw new ValidationException(StringsB.UC4_ERR_DUPLICATE);
        }
    }

    private void checkRating(int value, String label) {
        if (value < 1 || value > 5) {
            throw new ValidationException(StringsB.UC4_ERR_RATING.formatted(label));
        }
    }

    private HiringWoman requireWoman() {
        if (Session.isLoggedIn() && Session.getUser() instanceof HiringWoman woman) {
            return woman;
        }
        throw new ValidationException(StringsB.UC3_ERR_NOT_ALLOWED);
    }
}