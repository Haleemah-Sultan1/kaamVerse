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
}
