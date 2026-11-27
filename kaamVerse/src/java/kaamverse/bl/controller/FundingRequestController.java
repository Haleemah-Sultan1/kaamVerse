package kaamverse.bl.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kaamverse.bl.model.FundingRequest;
import kaamverse.bl.model.HiringWoman;
import kaamverse.bl.model.Maid;
import kaamverse.bl.service.AudioPromptService;
import kaamverse.bl.service.FundingRequestService;
import kaamverse.bl.service.LoggingAudioPromptService;
import kaamverse.bl.service.Session;
import kaamverse.common.EventType;
import kaamverse.common.MaidStatus;
import kaamverse.common.Strings;

/**
 * GRASP Controller for UC9 Request Event Funding. It holds the draft of the
 * request while the maid goes through the wizard; nothing is written to the
 * database until submitRequest(). It checks the preconditions itself, because
 * the menu only hides the button.
 * Pattern: startNewRequest, enterEventType, enterAmountAndDate, enterReason,
 * selectEmployers, submitRequest.
 */
public class FundingRequestController {

    /** Length of funding_request.reason. */
    public static final int MAX_REASON_LENGTH = 1000;
    /** Largest value of funding_request.amount_needed, DECIMAL(10,2). */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private final FundingRequestService service = new FundingRequestService();
    private final AudioPromptService audio = new LoggingAudioPromptService();

    // The draft. The maid is read again from the database, with her current employers.
    private Maid maid;
    private EventType eventType;
    private Double amount;
    private LocalDate eventDate;
    private String reason;
    private final Set<Integer> chosenEmployerIds = new LinkedHashSet<>();
    private FundingRequest saved;

    /**
     * Starts a new draft. The maid must be Verified and have at least one
     * Active hire; otherwise a ValidationException carries the message to show
     * and the wizard is not opened.
     */
    public void startNewRequest() {
        maid = loadEligibleMaid();
        eventType = null;
        amount = null;
        eventDate = null;
        reason = null;
        chosenEmployerIds.clear();
        saved = null;
    }

    public void playPrompt(String text) {
        audio.play(text);
    }

    public void enterEventType(EventType chosen) {
        if (chosen == null) {
            throw new ValidationException(Strings.UC9_ERR_EVENT);
        }
        eventType = chosen;
    }

    /** Validates both values before keeping either of them. */
    public void enterAmountAndDate(String amountText, LocalDate date) {
        double validAmount = validAmount(amountText);
        requireNotPast(date);
        amount = validAmount;
        eventDate = date;
    }

    public void enterReason(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(Strings.UC9_ERR_REASON);
        }
        if (trimmed.length() > MAX_REASON_LENGTH) {
            throw new ValidationException(Strings.UC9_ERR_REASON_TOO_LONG.formatted(MAX_REASON_LENGTH));
        }
        reason = trimmed;
    }

    /** The employers she can choose from (read-only). */
    public List<HiringWoman> getCurrentEmployers() {
        return requireDraft().getCurrentEmployers();
    }

    /** @param employerIds the chosen employers; each must be one of her current employers */
    public void selectEmployers(Collection<Integer> employerIds) {
        if (employerIds == null || employerIds.isEmpty()) {
            throw new ValidationException(Strings.UC9_ERR_NO_EMPLOYER_CHOSEN);
        }
        Set<Integer> allowed = idsOf(requireDraft().getCurrentEmployers());
        if (!allowed.containsAll(employerIds)) {
            throw new ValidationException(Strings.UC9_ERR_EMPLOYER_CHANGED);
        }
        chosenEmployerIds.clear();
        chosenEmployerIds.addAll(employerIds);
    }

    /**
     * Saves the request and sends it to the chosen employers. Everything is
     * checked again against the database first, because time has passed since
     * the maid entered it.
     *
     * @return the saved request, with its tracking number
     */
    public FundingRequest submitRequest() {
        requireDraft();
        if (saved != null) {
            throw new IllegalStateException("This request was already submitted");
        }
        if (eventType == null || amount == null || eventDate == null || reason == null) {
            throw new IllegalStateException("The request is not complete");
        }
        requireNotPast(eventDate);

        Maid current = loadEligibleMaid();
        maid = current;
        Set<Integer> stillEmployed = idsOf(current.getCurrentEmployers());
        boolean changed = !stillEmployed.containsAll(chosenEmployerIds);
        chosenEmployerIds.retainAll(stillEmployed);
        if (changed) {
            throw new ValidationException(Strings.UC9_ERR_EMPLOYER_CHANGED);
        }
        if (chosenEmployerIds.isEmpty()) {
            throw new ValidationException(Strings.UC9_ERR_NO_EMPLOYER_CHOSEN);
        }
        saved = service.submit(current, eventType, amount, eventDate, reason, getChosenEmployers());
        return saved;
    }

    // Read-back for the screen

    public EventType getEventType() {
        return eventType;
    }

    /** The amount as shown to the maid, for example 25,000. */
    public String getAmountText() {
        return FundingRequestService.formatAmount(amount);
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public String getReason() {
        return reason;
    }

    /** The chosen employers, in the order of the employer list. */
    public List<HiringWoman> getChosenEmployers() {
        List<HiringWoman> chosen = new ArrayList<>();
        for (HiringWoman employer : requireDraft().getCurrentEmployers()) {
            if (chosenEmployerIds.contains(employer.getHiringWomanId())) {
                chosen.add(employer);
            }
        }
        return chosen;
    }

    /** The tracking number of the submitted request. */
    public String getTrackingNo() {
        if (saved == null) {
            throw new IllegalStateException("The request has not been submitted");
        }
        return saved.getTrackingNo();
    }

    // Checks

    /** The logged-in maid, as she is in the database now, if she may raise a request. */
    private Maid loadEligibleMaid() {
        Maid current = requireMaid();
        Maid fresh = service.findMaidWithEmployers(current.getMaidId())
                .orElseThrow(() -> new ValidationException(Strings.UC9_ERR_NOT_ALLOWED));
        if (fresh.getStatus() != MaidStatus.Verified) {
            throw new ValidationException(Strings.UC9_ERR_NOT_VERIFIED);
        }
        if (fresh.getCurrentEmployers().isEmpty()) {
            throw new ValidationException(Strings.UC9_ERR_NO_EMPLOYER);
        }
        return fresh;
    }

    private static Maid requireMaid() {
        if (Session.isLoggedIn() && Session.getUser() instanceof Maid current) {
            return current;
        }
        throw new ValidationException(Strings.UC9_ERR_NOT_ALLOWED);
    }

    private Maid requireDraft() {
        if (maid == null) {
            throw new IllegalStateException("startNewRequest() has not been called");
        }
        return maid;
    }

    /** More than 0, at most 2 decimals, and it fits the database column. */
    private static double validAmount(String typed) {
        String text = toAsciiDigits(typed == null ? "" : typed.trim()).replace(",", "").replace(" ", "");
        if (!text.matches("[0-9]{1,12}(\\.[0-9]{1,2})?")) {
            throw new ValidationException(Strings.UC9_ERR_AMOUNT);
        }
        BigDecimal value = new BigDecimal(text);
        if (value.signum() <= 0) {
            throw new ValidationException(Strings.UC9_ERR_AMOUNT);
        }
        if (value.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException(Strings.UC9_ERR_AMOUNT_TOO_LARGE);
        }
        return value.doubleValue();
    }

    /** Today is allowed; yesterday and earlier are not. */
    private static void requireNotPast(LocalDate date) {
        if (date == null) {
            throw new ValidationException(Strings.UC9_ERR_DATE);
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException(Strings.UC9_ERR_DATE_PAST);
        }
    }

    private static Set<Integer> idsOf(List<HiringWoman> employers) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (HiringWoman employer : employers) {
            ids.add(employer.getHiringWomanId());
        }
        return ids;
    }

    /** Urdu keyboards can type Urdu digits; they are stored as 0-9. */
    private static String toAsciiDigits(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            result.append(Character.isDigit(c) ? (char) ('0' + Character.digit(c, 10)) : c);
        }
        return result.toString();
    }
}
