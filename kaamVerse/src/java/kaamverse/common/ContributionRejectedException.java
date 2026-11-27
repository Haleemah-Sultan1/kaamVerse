package kaamverse.common;

/** UC10: a contribution cannot be accepted because the request changed since it was shown. */
public class ContributionRejectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Why it was rejected. */
    public enum Reason { NOT_AVAILABLE, TOO_MUCH }

    private final Reason reason;
    private final double remaining;

    public ContributionRejectedException(Reason reason, double remaining) {
        super(reason.name());
        this.reason = reason;
        this.remaining = remaining;
    }

    public Reason getReason() {
        return reason;
    }

    /** The amount still needed now (0 when the request is not available). */
    public double getRemaining() {
        return remaining;
    }
}
