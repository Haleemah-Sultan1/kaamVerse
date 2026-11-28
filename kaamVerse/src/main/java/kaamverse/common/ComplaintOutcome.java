package kaamverse.common;

public enum ComplaintOutcome {
    ESCALATE("Escalate to an authority"),
    RESOLVED("Closed: resolved"),
    NOT_SUBSTANTIATED("Closed: not substantiated");

    private final String label;

    ComplaintOutcome(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEscalation() {
        return this == ESCALATE;
    }

    @Override
    public String toString() {
        return label;
    }
}