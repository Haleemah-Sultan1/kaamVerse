package kaamverse.common;

public enum ProblemType {
    VERBAL_ABUSE("Verbal abuse", "Normal"),
    PHYSICAL("Physical harassment", "High"),
    SEXUAL("Sexual harassment", "High"),
    THREATS("Threats or intimidation", "High"),
    OTHER("Other", "Normal");

    private final String label;
    private final String urgency;

    ProblemType(String label, String urgency) {
        this.label = label;
        this.urgency = urgency;
    }

    public String getLabel() {
        return label;
    }

    public String getUrgency() {
        return urgency;
    }
}