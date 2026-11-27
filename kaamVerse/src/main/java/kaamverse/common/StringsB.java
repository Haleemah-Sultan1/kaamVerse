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

    public static String quantityName(WageUnit unit) {
        return switch (unit) {
            case PER_HOUR -> "hours";
            case PER_DAY -> "days";
            case PER_VISIT -> "visits";
        };
    }

    // UC3 Search and hire
    public static final String UC3_HEADING = "Find and hire a maid";
    public static final String UC3_LOCALITY = "Locality";
    public static final String UC3_STREET = "Street";
    public static final String UC3_SERVICE = "Service";
    public static final String UC3_MIN_RATING = "Minimum rating (0 to 5)";
    public static final String UC3_DAY = "Available on";
    public static final String UC3_TIME = "Time of day";
    public static final String UC3_ANY = "Any";
    public static final String UC3_SEARCH = "Search";
    public static final String UC3_CLEAR = "Clear filters";
    public static final String UC3_VIEW_PROFILE = "View profile";
    public static final String UC3_MY_HIRES = "My hires";
    public static final String UC3_COL_NAME = "Name";
    public static final String UC3_COL_BADGE = "Badge";
    public static final String UC3_COL_RATING = "Rating";
    public static final String UC3_COL_AREA = "Area";
    public static final String UC3_COL_SERVICES = "Services and standard wage";
    public static final String UC3_BADGE = "Verified Trust Badge";
    public static final String UC3_NO_BADGE = "-";
    public static final String UC3_NO_RATING = "No reviews yet";
    public static final String UC3_NO_RESULTS = "No verified maid matches these filters.";
    public static final String UC3_NO_BAND = "No wage band set";
    public static final String UC3_PROFILE_HEADING = "Maid profile";
    public static final String UC3_PROFILE_AREA = "Area: %s";
    public static final String UC3_PROFILE_RATING = "Rating: %s";
    public static final String UC3_PROFILE_YEARS = "%d years";
    public static final String UC3_PROFILE_AVAILABLE = "Free slots: %s";
    public static final String UC3_PROFILE_NO_SLOTS = "Free slots: none saved";
    public static final String UC3_PROFILE_REVIEWS = "Reviews from earlier employers";
    public static final String UC3_PROFILE_NO_REVIEWS = "No approved reviews yet.";
    public static final String UC3_REVIEW_LINE = "Overall %d/5, %s. %s";
    public static final String UC3_WOULD_REHIRE = "would hire again";
    public static final String UC3_WOULD_NOT_REHIRE = "would not hire again";
    public static final String UC3_HIRE_HEADING = "Hire request (tick the services you need)";
    public static final String UC3_COL_OFFER = "Service";
    public static final String UC3_COL_EXPERIENCE = "Experience";
    public static final String UC3_COL_WAGE = "Standard wage";
    public static final String UC3_COL_QUANTITY = "How many";
    public static final String UC3_HOURS = "Working hours (for example 9am to 12pm)";
    public static final String UC3_SEND = "Send hire request";
    public static final String UC3_SUMMARY_HEAD = "Hire request for %s";
    public static final String UC3_SUMMARY_LINE = "%s: %s %s = Rs %s";
    public static final String UC3_SUMMARY_HOURS = "Working hours: %s";
    public static final String UC3_SUMMARY_TOTAL = "Total wage: Rs %s";
    public static final String UC3_SUMMARY_RULE = "Wage rule: the maximum rate of the standard band x quantity.";
    public static final String UC3_SUMMARY_ASK = "Send this request?";
    public static final String UC3_SAVED = "Your hire request was sent. Status: Pending.";
    public static final String UC3_NOTIFY_MAID = "%s sent you a hire request. Working hours: %s.";
    public static final String UC3_NOTIFY_FALLBACK = "An employer";
    public static final String UC3_ERR_NOT_ALLOWED = "Only a hiring woman can use this screen.";
    public static final String UC3_ERR_RATING = "The minimum rating must be a number from 0 to 5.";
    public static final String UC3_ERR_SELECT = "Please select a maid from the list first.";
    public static final String UC3_ERR_SUSPENDED = "Your hiring is suspended, so you cannot send hire requests.";
    public static final String UC3_ERR_MAID_GONE = "This maid is no longer available. Please search again.";
    public static final String UC3_ERR_OPEN_HIRE = "You already have a pending or active hire with this maid.";
    public static final String UC3_ERR_NO_SERVICE = "Please tick at least one service.";
    public static final String UC3_ERR_NO_BAND = "%s has no standard wage yet, so it cannot be hired.";
    public static final String UC3_ERR_QTY = "Enter a number greater than zero (up to 1000) for %s.";
    public static final String UC3_ERR_HOURS = "Please enter the working hours (up to 100 characters).";
    public static final String UC3_ERR_PRICE = "The wage could not be worked out. Please check the services.";
    public static final String UC3_ERR_TOTAL = "The total wage is too large. Please reduce the quantities.";
    public static final String UC3_ERR_NO_DRAFT = "There is no hire request to confirm. Please start again.";

    // UC3 My hires
    public static final String UC3_MY_HEADING = "My hires";
    public static final String UC3_MY_COL_MAID = "Maid";
    public static final String UC3_MY_COL_HOURS = "Working hours";
    public static final String UC3_MY_COL_REQUESTED = "Requested";
    public static final String UC3_MY_COL_STARTED = "Started";
    public static final String UC3_MY_COL_ENDED = "Ended";
    public static final String UC3_MY_COL_STATUS = "Status";
    public static final String UC3_MY_COL_WAGE = "Wage (Rs)";
    public static final String UC3_MY_NONE = "You have no hires yet.";
    public static final String UC3_ACTIVATE = "Activate";
    public static final String UC3_END = "End hire";
    public static final String UC3_FIND = "Find a maid";
    public static final String UC3_END_ASK = "End this hire? You can then review the maid.";
    public static final String UC3_ERR_SELECT_HIRE = "Please select a hire from the list first.";
    public static final String UC3_ERR_NOT_PENDING = "Only a pending hire can be activated.";
    public static final String UC3_ERR_NOT_ACTIVE = "Only an active hire can be ended.";
    public static final String UC3_ERR_HIRE_GONE = "This hire could not be found. Please refresh.";


}