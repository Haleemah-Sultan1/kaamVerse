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
    public static final String INTRO_TAGLINE = "Honest work. Trusted homes.";
    public static final String INTRO_ENTER = "Enter  \u2192";
    public static final String WELCOME_BACK = "Welcome back";
    public static final String SIGN_IN_HINT = "Sign in to continue to your account.";
    public static final String OR = "or";
    public static final String MENU_HEADING = "What would you like to do today?";

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
    // UC1 screen (Urdu)
    public static final String UC1_BACK = "واپس";
    public static final String UC1_NEXT = "آگے";
    public static final String UC1_CANCEL = "منسوخ کریں";
    public static final String UC1_SUBMIT = "جمع کروائیں";
    public static final String UC1_LISTEN = "سنیں";
    public static final String UC1_STEP_DETAILS = "ذاتی معلومات";
    public static final String UC1_STEP_LOCALITY = "علاقہ";
    public static final String UC1_STEP_STREET = "گلی";
    public static final String UC1_STEP_SERVICES = "کام اور تجربہ";
    public static final String UC1_STEP_IMAGE = "شناختی کارڈ کی تصویر";
    public static final String UC1_STEP_REVIEW = "معلومات کی جانچ";
    public static final String UC1_PROMPT_DETAILS = "اپنی معلومات لکھیں۔";
    public static final String UC1_PROMPT_LOCALITY = "اپنا علاقہ منتخب کریں۔";
    public static final String UC1_PROMPT_STREET = "اپنی گلی منتخب کریں۔";
    public static final String UC1_PROMPT_SERVICES = "اپنے کام منتخب کریں اور ہر کام کا تجربہ لکھیں۔";
    public static final String UC1_PROMPT_IMAGE = "اپنے شناختی کارڈ کی تصویر منتخب کریں۔";
    public static final String UC1_PROMPT_REVIEW = "اپنی معلومات دیکھ لیں، پھر جمع کروائیں۔";
    public static final String UC1_NAME = "نام";
    public static final String UC1_FATHER_NAME = "والد کا نام";
    public static final String UC1_CNIC = "شناختی کارڈ نمبر";
    public static final String UC1_PHONE = "فون نمبر";
    public static final String UC1_ADDRESS = "پتہ";
    public static final String UC1_PIN = "خفیہ کوڈ (4 ہندسے)";
    public static final String UC1_LOCALITY = "علاقہ";
    public static final String UC1_STREET = "گلی";
    public static final String UC1_YEARS = "تجربہ (سال)";
    public static final String UC1_YEARS_VALUE = "%d سال";
    public static final String UC1_CHOOSE_IMAGE = "تصویر منتخب کریں";
    public static final String UC1_IMAGE_FILES = "تصاویر";
    public static final String UC1_NO_IMAGE = "کوئی تصویر منتخب نہیں کی گئی";
    public static final String UC1_SUCCESS = "آپ کی درخواست جمع ہو گئی ہے۔ تصدیق ابھی باقی ہے۔";
    public static final String UC1_ERR_REQUIRED = "یہ خانہ خالی ہے: %s";
    public static final String UC1_ERR_TOO_LONG = "یہ تحریر بہت لمبی ہے: %s";
    public static final String UC1_ERR_CNIC = "شناختی کارڈ نمبر 13 ہندسوں کا ہونا چاہیے۔";
    public static final String UC1_ERR_CNIC_USED = "یہ شناختی کارڈ نمبر پہلے سے رجسٹرڈ ہے۔";
    public static final String UC1_ERR_PHONE = "فون نمبر 11 ہندسوں کا ہو اور 03 سے شروع ہو۔";
    public static final String UC1_ERR_PHONE_USED = "یہ فون نمبر پہلے سے رجسٹرڈ ہے۔";
    public static final String UC1_ERR_PIN = "خفیہ کوڈ 4 ہندسوں کا ہونا چاہیے۔";
    public static final String UC1_ERR_LOCALITY = "براہ کرم اپنا علاقہ منتخب کریں۔";
    public static final String UC1_ERR_STREET = "براہ کرم اپنی گلی منتخب کریں۔";
    public static final String UC1_ERR_NO_SERVICE = "کم از کم ایک کام منتخب کریں۔";
    public static final String UC1_ERR_YEARS = "تجربہ سالوں میں لکھیں، 0 سے 99 تک۔";
    public static final String UC1_ERR_IMAGE = "براہ کرم شناختی کارڈ کی JPG یا PNG تصویر منتخب کریں۔";
    public static final String UC1_ERR_IMAGE_SAVE = "تصویر محفوظ نہیں ہو سکی۔ دوبارہ کوشش کریں۔";
    public static final String UC1_ERR_DUPLICATE = "یہ فون نمبر یا شناختی کارڈ نمبر پہلے سے رجسٹرڈ ہے۔";
    public static final String UC1_ERR_DATABASE = "ڈیٹا بیس سے رابطہ نہیں ہو سکا۔ دوبارہ کوشش کریں۔";
    // Other Person A use cases
    public static final String UC2_VERIFY = "Verify worker identity";
    public static final String UC7_AVAILABILITY = "My availability";
    public static final String UC8_FAMILY = "Maid family records";
    public static final String UC9_FUNDING = "Request event funding";
    public static final String UC10_CONTRIBUTE = "Contribute to a funding request";

    // UC2 screen
    public static final String UC2_COL_REQUEST = "Request";
    public static final String UC2_COL_NAME = "Name";
    public static final String UC2_COL_CNIC = "CNIC";
    public static final String UC2_COL_SUBMITTED = "Submitted";
    public static final String UC2_NO_PENDING = "There are no pending verification requests.";
    public static final String UC2_REVIEW = "Review";
    public static final String UC2_DETAIL_HEADING = "Verify %s";
    public static final String UC2_FIELD_NAME = "Name";
    public static final String UC2_FIELD_FATHER = "Father's name";
    public static final String UC2_FIELD_CNIC = "CNIC";
    public static final String UC2_FIELD_ADDRESS = "Address";
    public static final String UC2_FIELD_SUBMITTED = "Submitted on";
    public static final String UC2_CNIC_IMAGE = "CNIC image";
    public static final String UC2_NO_IMAGE = "The CNIC image is missing or could not be opened.";
    public static final String UC2_REASON_LABEL = "Reason (required to reject)";
    public static final String UC2_MATCHED = "Matched";
    public static final String UC2_REJECTED = "Rejected";
    public static final String UC2_CONFIRM_MATCH = "Mark %s as matched and verify her?";
    public static final String UC2_CONFIRM_REJECT = "Reject the verification of %s?";
    public static final String UC2_MATCHED_DONE = "%s is now Verified.";
    public static final String UC2_REJECTED_DONE = "%s was rejected.";
    public static final String UC2_ERR_REASON_REQUIRED = "Please enter the reason for rejecting this request.";
    public static final String UC2_ERR_REASON_TOO_LONG = "The reason must be %d characters or fewer.";
    public static final String UC2_ERR_NOT_PENDING =
            "This request was already decided or no longer exists. Please refresh the list.";
    public static final String UC2_ERR_NOT_ALLOWED = "You do not have permission to verify maids.";
    public static final String UC2_NOTIFY_MATCHED = "Your identity has been verified. Your profile is now Verified.";
    public static final String UC2_NOTIFY_REJECTED = "Your identity verification was rejected. Reason: %s";
    public static final String UC2_AUDIT_MATCHED = "Matched verification request #%d (maid #%d)";
    public static final String UC2_AUDIT_REJECTED = "Rejected verification request #%d (maid #%d): %s";

    // UC9 screen (Urdu)
    public static final String UC9_TITLE = "فنڈ کی درخواست";
    public static final String UC9_BACK = "واپس";
    public static final String UC9_NEXT = "آگے";
    public static final String UC9_CANCEL = "منسوخ کریں";
    public static final String UC9_SUBMIT = "درخواست بھیجیں";
    public static final String UC9_FINISH = "مکمل";
    public static final String UC9_LISTEN = "سنیں";
    public static final String UC9_STEP_OF = "مرحلہ %d از %d";
    public static final String UC9_STEP_EVENT = "تقریب کی قسم";
    public static final String UC9_STEP_AMOUNT = "رقم اور تاریخ";
    public static final String UC9_STEP_REASON = "وجہ";
    public static final String UC9_STEP_EMPLOYERS = "آجر کا انتخاب";
    public static final String UC9_STEP_CONFIRM = "درخواست کی جانچ";
    public static final String UC9_STEP_DONE = "درخواست بھیج دی گئی";
    public static final String UC9_PROMPT_EVENT = "وہ تقریب منتخب کریں جس کے لیے آپ کو مدد چاہیے۔";
    public static final String UC9_PROMPT_AMOUNT = "مطلوبہ رقم روپوں میں لکھیں اور تقریب کی تاریخ منتخب کریں۔";
    public static final String UC9_PROMPT_REASON = "بتائیں کہ آپ کو مدد کی ضرورت کیوں ہے۔";
    public static final String UC9_PROMPT_EMPLOYERS = "وہ آجر منتخب کریں جنہیں آپ یہ درخواست بھیجنا چاہتی ہیں۔";
    public static final String UC9_PROMPT_CONFIRM = "اپنی درخواست دیکھ لیں، پھر بھیج دیں۔";
    public static final String UC9_READBACK = "آپ نے %s روپے اور تاریخ %s لکھی ہے۔";
    public static final String UC9_EVENT = "تقریب";
    public static final String UC9_AMOUNT = "رقم (روپے)";
    public static final String UC9_EVENT_DATE = "تقریب کی تاریخ";
    public static final String UC9_REASON = "وجہ";
    public static final String UC9_EMPLOYERS = "آجر";
    public static final String UC9_EVENT_WEDDING = "شادی";
    public static final String UC9_EVENT_MEDICAL = "طبی ایمرجنسی";
    public static final String UC9_EVENT_FUNERAL = "فوتگی";
    public static final String UC9_EVENT_EDUCATION = "تعلیم";
    public static final String UC9_EVENT_OTHER = "دیگر";
    public static final String UC9_TRACKING_LABEL = "ٹریکنگ نمبر";
    public static final String UC9_SUCCESS = "آپ کی درخواست بھیج دی گئی ہے۔ آپ کا ٹریکنگ نمبر %s ہے۔";
    public static final String UC9_DONE_HINT = "یہ نمبر سنبھال کر رکھیں۔ آپ کے منتخب آجر کو اطلاع بھیج دی گئی ہے۔";
    public static final String UC9_ERR_NOT_ALLOWED = "یہ سہولت صرف ملازمہ کے لیے ہے۔";
    public static final String UC9_ERR_NOT_VERIFIED =
            "فنڈ کی درخواست کے لیے آپ کی شناخت کی تصدیق ہونا ضروری ہے۔ تصدیق مکمل ہونے کے بعد دوبارہ آئیں۔";
    public static final String UC9_ERR_NO_EMPLOYER =
            "فنڈ کی درخواست اسی وقت دی جا سکتی ہے جب آپ کسی گھر میں کام کر رہی ہوں۔ فی الحال آپ کا کوئی فعال کام نہیں ہے۔";
    public static final String UC9_ERR_EVENT = "براہ کرم تقریب کی قسم منتخب کریں۔";
    public static final String UC9_ERR_AMOUNT = "رقم 0 سے زیادہ ہونی چاہیے اور صرف ہندسوں میں لکھیں، مثلاً 25000۔";
    public static final String UC9_ERR_AMOUNT_TOO_LARGE = "یہ رقم بہت زیادہ ہے۔ براہ کرم رقم دوبارہ دیکھ لیں۔";
    public static final String UC9_ERR_DATE = "براہ کرم تقریب کی تاریخ منتخب کریں۔";
    public static final String UC9_ERR_DATE_PAST = "تقریب کی تاریخ گزر چکی ہے۔ آج یا آئندہ کی تاریخ منتخب کریں۔";
    public static final String UC9_ERR_REASON = "براہ کرم وجہ لکھیں۔";
    public static final String UC9_ERR_REASON_TOO_LONG = "وجہ %d حروف سے زیادہ نہیں ہو سکتی۔";
    public static final String UC9_ERR_NO_EMPLOYER_CHOSEN = "کم از کم ایک آجر منتخب کریں۔";
    public static final String UC9_ERR_EMPLOYER_CHANGED =
            "آپ کے آجروں کی فہرست بدل گئی ہے۔ براہ کرم دوبارہ منتخب کریں۔";
    public static final String UC9_ERR_DATABASE = "ڈیٹا بیس سے رابطہ نہیں ہو سکا۔ دوبارہ کوشش کریں۔";
    public static final String UC9_NOTIFY_EMPLOYER = "Funding request from %s. Event: %s. "
            + "Amount needed: Rs %s. Event date: %s. Tracking number: %s.";

    // UC8 screen
    public static final String UC8_SEARCH_PROMPT = "Maid name or CNIC";
    public static final String UC8_SEARCH = "Search";
    public static final String UC8_NO_MAIDS = "No maid was found.";
    public static final String UC8_FAMILY_HEADING = "Family members";
    public static final String UC8_CONTACT_HEADING = "Emergency contacts";
    public static final String UC8_ADD = "Add";
    public static final String UC8_EDIT = "Edit";
    public static final String UC8_DELETE = "Delete";
    public static final String UC8_SAVE = "Save";
    public static final String UC8_CANCEL = "Cancel";
    public static final String UC8_COL_NAME = "Name";
    public static final String UC8_COL_RELATION = "Relation";
    public static final String UC8_COL_AGE = "Age";
    public static final String UC8_COL_CNIC = "CNIC / B-Form";
    public static final String UC8_COL_DEPENDENT = "Dependent";
    public static final String UC8_COL_SCHOOL = "School";
    public static final String UC8_COL_PHONE = "Phone";
    public static final String UC8_FIELD_DEPENDENT = "Is a dependent";
    public static final String UC8_ADD_FAMILY_TITLE = "Add family member";
    public static final String UC8_EDIT_FAMILY_TITLE = "Edit family member";
    public static final String UC8_ADD_CONTACT_TITLE = "Add emergency contact";
    public static final String UC8_CONFIRM_DELETE_FAMILY = "Delete family member %s?";
    public static final String UC8_CONFIRM_DELETE_CONTACT = "Delete emergency contact %s?";
    public static final String UC8_ERR_NOT_ALLOWED = "You do not have permission to maintain family records.";
    public static final String UC8_ERR_SEARCH = "Enter a name or CNIC to search for.";
    public static final String UC8_ERR_MAID_GONE = "This maid no longer exists. Please search again.";
    public static final String UC8_ERR_RECORD_GONE =
            "This record was already changed or deleted. The list has been refreshed.";
    public static final String UC8_ERR_NAME = "Name is required (at most 100 characters).";
    public static final String UC8_ERR_RELATION = "Relation is required (at most 50 characters).";
    public static final String UC8_ERR_AGE = "Age must be a whole number from 0 to 120.";
    public static final String UC8_ERR_ID_NUMBER = "CNIC / B-Form must contain digits only (at most 15).";
    public static final String UC8_ERR_PHONE = "Phone must be 11 digits starting with 03, for example 03001234567.";
    public static final String UC8_ERR_SCHOOL = "School must be 150 characters or fewer.";
    public static final String UC8_AUDIT_ADD_FAMILY = "Added family member %s (%s) to maid #%d";
    public static final String UC8_AUDIT_EDIT_FAMILY = "Edited family member #%d (%s) of maid #%d";
    public static final String UC8_AUDIT_DELETE_FAMILY = "Deleted family member #%d (%s) of maid #%d";
    public static final String UC8_AUDIT_ADD_CONTACT = "Added emergency contact %s (%s) to maid #%d";
    public static final String UC8_AUDIT_DELETE_CONTACT = "Deleted emergency contact #%d (%s) of maid #%d";
    public static final String UC8_NOTIFY_ADD_FAMILY = "A welfare officer added %s (%s) to your family records.";
    public static final String UC8_NOTIFY_EDIT_FAMILY = "A welfare officer updated %s in your family records.";
    public static final String UC8_NOTIFY_DELETE_FAMILY = "A welfare officer removed %s from your family records.";
    public static final String UC8_NOTIFY_ADD_CONTACT = "A welfare officer added %s (%s) as your emergency contact.";
    public static final String UC8_NOTIFY_DELETE_CONTACT = "A welfare officer removed %s from your emergency contacts.";

    // UC7 screen
    public static final String UC7_DAY_SLOT = "Day";
    public static final String UC7_MORNING = "Morning";
    public static final String UC7_AFTERNOON = "Afternoon";
    public static final String UC7_EVENING = "Evening";
    public static final String UC7_SAVE = "Save";
    public static final String UC7_HINT = "Tick the times you can work. Booked times stay ticked and cannot be changed.";
    public static final String UC7_BOOKED_TIP = "Booked by an employer";
    public static final String UC7_SAVED = "Your availability was saved.";
    public static final String UC7_ERR_NOT_ALLOWED = "Only maids can manage an availability schedule.";
    public static final String UC7_DAY_MON = "Monday";
    public static final String UC7_DAY_TUE = "Tuesday";
    public static final String UC7_DAY_WED = "Wednesday";
    public static final String UC7_DAY_THU = "Thursday";
    public static final String UC7_DAY_FRI = "Friday";
    public static final String UC7_DAY_SAT = "Saturday";
    public static final String UC7_DAY_SUN = "Sunday";

    // UC10 screen
    public static final String UC10_COL_MAID = "Maid";
    public static final String UC10_COL_EVENT = "Event";
    public static final String UC10_COL_NEEDED = "Needed (Rs)";
    public static final String UC10_COL_RECEIVED = "Received (Rs)";
    public static final String UC10_NO_REQUESTS = "No funding requests are waiting for your support.";
    public static final String UC10_OPEN = "View details";
    public static final String UC10_DETAIL_HEADING = "Funding request from %s";
    public static final String UC10_FIELD_MAID = "Maid";
    public static final String UC10_FIELD_EVENT = "Event";
    public static final String UC10_FIELD_REASON = "Reason";
    public static final String UC10_FIELD_NEEDED = "Amount needed (Rs)";
    public static final String UC10_FIELD_RECEIVED = "Amount received (Rs)";
    public static final String UC10_FIELD_REMAINING = "Remaining (Rs)";
    public static final String UC10_FIELD_BADGE = "Verified Trust Badge";
    public static final String UC10_FIELD_SERVICE = "Length of service";
    public static final String UC10_YES = "Yes";
    public static final String UC10_NO = "No";
    public static final String UC10_SERVICE_NEW = "Less than a month";
    public static final String UC10_SERVICE_FORMAT = "%d year(s), %d month(s)";
    public static final String UC10_AMOUNT_LABEL = "Your contribution (Rs)";
    public static final String UC10_NOTE_LABEL = "Note (optional)";
    public static final String UC10_BUTTON_CONTRIBUTE = "Contribute";
    public static final String UC10_CONFIRM = "Contribute Rs %s to %s's %s request?";
    public static final String UC10_ACK_HEADING = "Thank you for your contribution";
    public static final String UC10_ACK_NUMBER = "Acknowledgement number";
    public static final String UC10_ACK_TEXT = "You contributed Rs %s. Total received: Rs %s of Rs %s. Status: %s.";
    public static final String UC10_ACK_BACK = "Back to requests";
    public static final String UC10_STATUS_PENDING = "Pending";
    public static final String UC10_STATUS_PARTIAL = "Partially funded";
    public static final String UC10_STATUS_FUNDED = "Funded";
    public static final int UC10_MAX_NOTE = 500;
    public static final String UC10_ERR_NOT_ALLOWED = "Only hiring women can contribute to funding requests.";
    public static final String UC10_ERR_AMOUNT =
            "Enter an amount more than 0, with at most 2 decimal places, for example 2500 or 2500.50.";
    public static final String UC10_ERR_AMOUNT_TOO_MUCH = "The amount cannot be more than the remaining Rs %s.";
    public static final String UC10_ERR_NOTE_TOO_LONG = "The note must be %d characters or fewer.";
    public static final String UC10_ERR_NOT_AVAILABLE =
            "This request is no longer open for contributions. Please refresh the list.";
    public static final String UC10_ERR_CHANGED =
            "Other contributions were received meanwhile. Only Rs %s remains. Please enter the amount again.";
    public static final String UC10_NOTIFY_MAID = "%s contributed Rs %s to your %s funding request (%s). "
            + "Received so far: Rs %s of Rs %s. Status: %s.";

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

    /** UC7: the English name of a day. */
    public static String uc7DayName(WeekDay day) {
        return switch (day) {
            case MON -> UC7_DAY_MON;
            case TUE -> UC7_DAY_TUE;
            case WED -> UC7_DAY_WED;
            case THU -> UC7_DAY_THU;
            case FRI -> UC7_DAY_FRI;
            case SAT -> UC7_DAY_SAT;
            case SUN -> UC7_DAY_SUN;
        };
    }

    /** UC7: the English name of a part of the day. */
    public static String uc7TimeSlotName(TimeSlot slot) {
        return switch (slot) {
            case MORNING -> UC7_MORNING;
            case AFTERNOON -> UC7_AFTERNOON;
            case EVENING -> UC7_EVENING;
        };
    }

    /** UC10: the English name of a funding status. */
    public static String uc10StatusName(FundingStatus status) {
        return switch (status) {
            case Pending -> UC10_STATUS_PENDING;
            case PartiallyFunded -> UC10_STATUS_PARTIAL;
            case Funded -> UC10_STATUS_FUNDED;
        };
    }

    /** UC10: a length of service such as "2 year(s), 3 month(s)". */
    public static String uc10LengthOfService(java.time.Period length) {
        if (length.getYears() == 0 && length.getMonths() == 0) {
            return UC10_SERVICE_NEW;
        }
        return UC10_SERVICE_FORMAT.formatted(length.getYears(), length.getMonths());
    }

    /** UC9: the Urdu name of an event type, for the maid's screen. */
    public static String uc9EventName(EventType type) {
        return switch (type) {
            case WEDDING -> UC9_EVENT_WEDDING;
            case MEDICAL -> UC9_EVENT_MEDICAL;
            case FUNERAL -> UC9_EVENT_FUNERAL;
            case EDUCATION -> UC9_EVENT_EDUCATION;
            case OTHER -> UC9_EVENT_OTHER;
        };
    }
}
