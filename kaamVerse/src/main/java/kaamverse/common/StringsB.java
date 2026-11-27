package kaamverse.common;

/**
 * Person B's UI text (UC3 to UC6, UC11, UC12). Kept apart from Strings so that
 * two people never edit the same file. GRASP Protected Variations: screens do
 * not change when the wording does.
 */
public final class StringsB {

    private StringsB() {
    }

    /** English name of a wage unit, for tables and drop-downs. */
    public static String unitName(WageUnit unit) {
        return switch (unit) {
            case PER_HOUR -> "per hour";
            case PER_DAY -> "per day";
            case PER_VISIT -> "per visit";
        };
    }

    // UC6 Set standard wage bands
    public static final String UC6_HEADING = "Standard wage bands";
    public static final String UC6_COL_SERVICE = "Service";
    public static final String UC6_COL_MIN = "Min rate (Rs)";
    public static final String UC6_COL_MAX = "Max rate (Rs)";
    public static final String UC6_COL_UNIT = "Unit";
    public static final String UC6_COL_EFFECTIVE = "Effective from";
    public static final String UC6_COL_STATUS = "Status";
    public static final String UC6_STATUS_CURRENT = "Current";
    public static final String UC6_STATUS_SCHEDULED = "Scheduled";
    public static final String UC6_NO_BANDS = "No wage bands have been set yet.";
    public static final String UC6_FORM_HEADING = "Set a new band (select a row to copy its values)";
    public static final String UC6_SERVICE = "Service";
    public static final String UC6_MIN = "Minimum rate (Rs)";
    public static final String UC6_MAX = "Maximum rate (Rs)";
    public static final String UC6_UNIT = "Unit";
    public static final String UC6_EFFECTIVE = "Effective date";
    public static final String UC6_SAVE = "Save band";
    public static final String UC6_SAVED = "The wage band was saved.";
    public static final String UC6_ERR_NOT_ALLOWED = "You are not allowed to manage wage bands.";
    public static final String UC6_ERR_SERVICE = "Please choose a service.";
    public static final String UC6_ERR_UNIT = "Please choose a unit.";
    public static final String UC6_ERR_MIN = "The minimum rate must be a number greater than zero.";
    public static final String UC6_ERR_MAX = "The maximum rate must be a number greater than zero.";
    public static final String UC6_ERR_RANGE = "The minimum rate must be less than the maximum rate.";
    public static final String UC6_ERR_TOO_HIGH = "A rate cannot be more than Rs 1,000,000.";
    public static final String UC6_ERR_DATE = "Please choose an effective date that is today or later.";
    public static final String UC6_AUDIT_SET = "Set wage band for %s: Rs %.2f to Rs %.2f %s from %s";
}