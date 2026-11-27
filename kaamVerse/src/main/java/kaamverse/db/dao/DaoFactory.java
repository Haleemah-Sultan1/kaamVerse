package kaamverse.db.dao;

/**
 * GoF Factory (simple factory form): the one place that picks DAO classes.
 * GRASP Protected Variations: callers depend on the interfaces only.
 */
public final class DaoFactory {

    private DaoFactory() {
    }

    public static OfficerDao officerDao() {
        return new JdbcOfficerDao();
    }

    public static MaidDao maidDao() {
        return new JdbcMaidDao();
    }

    public static HiringWomanDao hiringWomanDao() {
        return new JdbcHiringWomanDao();
    }

    public static NotificationDao notificationDao() {
        return new JdbcNotificationDao();
    }

    public static AuditLogDao auditLogDao() {
        return new JdbcAuditLogDao();
    }

    public static LocalityDao localityDao() {
        return new JdbcLocalityDao();
    }

    public static StreetDao streetDao() {
        return new JdbcStreetDao();
    }

    public static ServiceDao serviceDao() {
        return new JdbcServiceDao();
    }

    public static VerificationRequestDao verificationRequestDao() {
        return new JdbcVerificationRequestDao();
    }

    public static FundingRequestDao fundingRequestDao() {
        return new JdbcFundingRequestDao();
    }

    public static FundingRecipientDao fundingRecipientDao() {
        return new JdbcFundingRecipientDao();
    }

    public static ContributionDao contributionDao() {
        return new JdbcContributionDao();
    }

    public static FamilyMemberDao familyMemberDao() {
        return new JdbcFamilyMemberDao();
    }

    public static EmergencyContactDao emergencyContactDao() {
        return new JdbcEmergencyContactDao();
    }

    public static AvailabilitySlotDao availabilitySlotDao() {
        return new JdbcAvailabilitySlotDao();
    }
}
