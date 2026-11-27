package kaamverse.common;

/**
 * UC9: the kinds of event a maid can ask for help with. The label is the
 * English text stored in funding_request.event_type and shown to employers
 * and contributors; the Urdu wording for the maid is in Strings.
 */
public enum EventType {
    WEDDING("Wedding"),
    MEDICAL("Medical emergency"),
    FUNERAL("Funeral"),
    EDUCATION("Education"),
    OTHER("Other");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    /** The text saved in the database. */
    public String getLabel() {
        return label;
    }
}
