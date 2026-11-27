package kaamverse.bl.controller;

import java.math.BigDecimal;
import java.util.List;

import kaamverse.bl.model.Contribution;
import kaamverse.bl.model.ContributionReceipt;
import kaamverse.bl.model.FundingCase;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.service.ContributionService;
import kaamverse.bl.service.FundingRequestService;
import kaamverse.bl.service.Session;
import kaamverse.common.ContributionRejectedException;
import kaamverse.common.Strings;

/**
 * GRASP Controller for UC10 Contribute to a Funding Request. It holds the
 * request being looked at and the amount she entered; nothing is written
 * until confirmContribution(). It checks that the user is a hiring woman
 * itself, because the menu only hides the button, and lists only her own
 * requests.
 * Pattern: getSupportRequests, openRequest, enterContribution, confirmContribution.
 */
public class ContributionController {

    /** Largest value of contribution.amount, DECIMAL(10,2). */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private final ContributionService service = new ContributionService();

    private FundingCase current;
    private BigDecimal amount;
    private String note;
    private ContributionReceipt receipt;

    /** Pending or partially funded requests sent to the logged-in hiring woman. */
    public List<FundingCase> getSupportRequests() {
        return service.findOpenCases(requireHiringWoman());
    }

    /**
     * Opens one request, read again from the database, and clears anything
     * entered before.
     */
    public FundingCase openRequest(int requestId) {
        FundingCase found = service.findOpenCase(requireHiringWoman(), requestId)
                .orElseThrow(() -> new ValidationException(Strings.UC10_ERR_NOT_AVAILABLE));
        current = found;
        amount = null;
        note = null;
        receipt = null;
        return found;
    }

    /** Validates the amount (against the remaining amount as shown) and the note, then keeps them. */
    public void enterContribution(String amountText, String noteText) {
        FundingCase open = requireOpen();
        BigDecimal valid = validAmount(amountText);
        if (valid.compareTo(BigDecimal.valueOf(open.request().getRemainingAmount())) > 0) {
            throw new ValidationException(Strings.UC10_ERR_AMOUNT_TOO_MUCH.formatted(
                    FundingRequestService.formatAmount(open.request().getRemainingAmount())));
        }
        String trimmed = noteText == null ? "" : noteText.trim();
        if (trimmed.length() > Strings.UC10_MAX_NOTE) {
            throw new ValidationException(Strings.UC10_ERR_NOTE_TOO_LONG.formatted(Strings.UC10_MAX_NOTE));
        }
        amount = valid;
        note = trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Saves the contribution. The remaining amount is checked again in the
     * database first, because another employer may have contributed since.
     *
     * @return the contribution, with its acknowledgement number
     */
    public Contribution confirmContribution() {
        FundingCase open = requireOpen();
        if (amount == null) {
            throw new IllegalStateException("enterContribution() has not been called");
        }
        if (receipt != null) {
            throw new IllegalStateException("This contribution was already confirmed");
        }
        try {
            receipt = service.contribute(requireHiringWoman(), open.request().getRequestId(), amount, note);
        } catch (ContributionRejectedException e) {
            amount = null;
            if (e.getReason() == ContributionRejectedException.Reason.TOO_MUCH) {
                throw new ValidationException(Strings.UC10_ERR_CHANGED.formatted(
                        FundingRequestService.formatAmount(e.getRemaining())));
            }
            throw new ValidationException(Strings.UC10_ERR_NOT_AVAILABLE);
        }
        return receipt.contribution();
    }

    // Read-back for the screen

    public FundingCase getCurrent() {
        return requireOpen();
    }

    /** The saved contribution and the request after it, for the acknowledgement. */
    public ContributionReceipt getReceipt() {
        if (receipt == null) {
            throw new IllegalStateException("The contribution has not been confirmed");
        }
        return receipt;
    }

    public String getAmountText() {
        return FundingRequestService.formatAmount(amount.doubleValue());
    }

    // Checks

    private static HiringWoman requireHiringWoman() {
        if (Session.isLoggedIn() && Session.getUser() instanceof HiringWoman woman) {
            return woman;
        }
        throw new ValidationException(Strings.UC10_ERR_NOT_ALLOWED);
    }

    private FundingCase requireOpen() {
        if (current == null) {
            throw new IllegalStateException("openRequest() has not been called");
        }
        return current;
    }

    /** More than 0, at most 2 decimals, and it fits the database column. */
    private static BigDecimal validAmount(String typed) {
        String text = toAsciiDigits(typed == null ? "" : typed.trim()).replace(",", "").replace(" ", "");
        if (!text.matches("[0-9]{1,12}(\\.[0-9]{1,2})?")) {
            throw new ValidationException(Strings.UC10_ERR_AMOUNT);
        }
        BigDecimal value = new BigDecimal(text);
        if (value.signum() <= 0 || value.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException(Strings.UC10_ERR_AMOUNT);
        }
        return value.setScale(2);
    }

    /** Keyboards can type Urdu digits; they are stored as 0-9. */
    private static String toAsciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            result.append(Character.isDigit(c) ? (char) ('0' + Character.digit(c, 10)) : c);
        }
        return result.toString();
    }
}
