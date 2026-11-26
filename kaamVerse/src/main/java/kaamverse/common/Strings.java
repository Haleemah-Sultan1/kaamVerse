package kaamverse.common;

/**
 * All user-visible UI text. Replace the values here to switch to Urdu.
 * GRASP Protected Variations: screens do not change when the wording does.
 * Add new text under a comment for your screen to avoid merge conflicts.
 */
public final class Strings {

    // Application
    public static final String APP_TITLE = "KaamVerse";
    public static final String WELCOME = "Welcome to KaamVerse";

    // Alerts
    public static final String ERROR_TITLE = "Something went wrong";
    public static final String INFO_TITLE = "Information";
    public static final String CONFIRM_TITLE = "Please confirm";
    public static final String DATABASE_ERROR =
            "We could not reach the database. Please check that MySQL is running and try again.";
    public static final String DUPLICATE_ERROR =
            "That phone number or CNIC is already registered.";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again.";

    // Login
    public static final String ROLE_LABEL = "I am a";
    public static final String PHONE_LABEL = "Phone number";
    public static final String PIN_LABEL = "PIN";
    public static final String USERNAME_LABEL = "Username";
    public static final String PASSWORD_LABEL = "Password";
    public static final String LOGIN = "Log in";
    public static final String LOGOUT = "Log out";
    public static final String MISSING_CREDENTIALS = "Please fill in both fields.";
    public static final String WRONG_PIN_LOGIN = "The phone number or PIN is not correct.";
    public static final String WRONG_OFFICER_LOGIN = "The username or password is not correct.";
    public static final String ROLE_MAID = "Maid";
    public static final String ROLE_HIRING_WOMAN = "Hiring woman";
    public static final String ROLE_OFFICER = "Officer";
    private static final String LOGGED_IN_AS = "%s (%s)";

    // Main menu and notifications
    public static final String NOTIFICATIONS = "Unread notifications";
    public static final String NO_NOTIFICATIONS = "No unread notifications";
    public static final String MARK_AS_READ = "Mark as read";
    public static final String REFRESH = "Refresh";
    public static final String BACK = "Back";
    public static final String COMING_SOON = "Coming soon";

    // Person A: use cases
    public static final String UC1_REGISTER = "Register as a maid";
    public static final String UC2_VERIFY = "Verify worker identity";
    public static final String UC7_AVAILABILITY = "My availability";
    public static final String UC8_FAMILY = "Maid family records";
    public static final String UC9_FUNDING = "Request event funding";
    public static final String UC10_CONTRIBUTE = "Contribute to a funding request";

    // Person B: use cases
    public static final String UC3_HIRE = "Find and hire a maid";
    public static final String UC4_EXPERIENCE = "Share past experience";
    public static final String UC5_BADGE = "Reviews and trust badges";
    public static final String UC6_WAGES = "Set standard wage bands";
    public static final String UC11_COMPLAINT = "File a confidential complaint";
    public static final String UC12_REVIEW_COMPLAINTS = "Review harassment complaints";

    private Strings() {
    }

    public static String roleName(UserRole role) {
        return switch (role) {
            case MAID -> ROLE_MAID;
            case HIRING_WOMAN -> ROLE_HIRING_WOMAN;
            case OFFICER -> ROLE_OFFICER;
        };
    }

    public static String loggedInAs(String name, UserRole role) {
        return String.format(LOGGED_IN_AS, name, roleName(role));
    }
}
