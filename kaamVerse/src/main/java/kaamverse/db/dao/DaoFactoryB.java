package kaamverse.db.dao;

public final class DaoFactoryB {

    private DaoFactoryB() {
    }

    public static WageBandDao wageBandDao() {
        return new JdbcWageBandDao();
    }

    public static MaidSearchDao maidSearchDao() {
        return new JdbcMaidSearchDao();
    }

    public static HireDao hireDao() {
        return new JdbcHireDao();
    }

    public static ReviewDao reviewDao() {
        return new JdbcReviewDao();
    }

    public static TrustBadgeDao trustBadgeDao() {
        return new JdbcTrustBadgeDao();
    }
}