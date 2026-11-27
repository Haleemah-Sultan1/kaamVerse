package kaamverse.db.dao;

/**
 * GoF Factory for Person B's DAOs. Kept apart from DaoFactory so that two
 * people never edit the same file. GRASP Protected Variations: callers
 * depend on the interfaces only.
 */
public final class DaoFactoryB {

    private DaoFactoryB() {
    }

    public static WageBandDao wageBandDao() {
        return new JdbcWageBandDao();
    }
}