package kaamverse.db.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import kaamverse.common.DatabaseException;

/**
 * Runs several DAO calls as one transaction, and hands DAOs their connections.
 * GRASP High Cohesion: commit and rollback are handled here only.
 *
 * <pre>
 * TransactionManager.run(() -> {
 *     firstDao.delete(id);
 *     secondDao.insert(item);
 * });
 * </pre>
 *
 * Every DAO call inside the block (on the same thread) shares one connection.
 * A block inside another block joins the outer transaction.
 */
public final class TransactionManager {

    private static final ThreadLocal<Transaction> CURRENT = new ThreadLocal<>();

    private static final class Transaction {
        private final Connection connection;
        private boolean rollbackOnly;

        private Transaction(Connection connection) {
            this.connection = connection;
        }
    }

    private TransactionManager() {
    }

    /** Runs the work in a transaction: all of it is saved, or none of it. */
    public static void run(Runnable work) {
        call(() -> {
            work.run();
            return null;
        });
    }

    /** Same as run, for work that returns a value. */
    public static <T> T call(Supplier<T> work) {
        Transaction outer = CURRENT.get();
        if (outer != null) {
            try {
                return work.get();
            } catch (RuntimeException | Error e) {
                outer.rollbackOnly = true;
                throw e;
            }
        }
        try (Connection connection = DBConnection.open()) {
            connection.setAutoCommit(false);
            Transaction transaction = new Transaction(connection);
            CURRENT.set(transaction);
            boolean committed = false;
            try {
                T result = work.get();
                if (transaction.rollbackOnly) {
                    throw new DatabaseException("Transaction rolled back because an inner step failed");
                }
                connection.commit();
                committed = true;
                return result;
            } finally {
                CURRENT.remove();
                if (!committed) {
                    rollback(connection);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("The transaction could not be completed", e);
        }
    }

    /**
     * Used by the DAOs. Outside a transaction this is a new connection; inside
     * one it is the shared connection, and closing it does nothing.
     */
    static Connection connection() {
        Transaction transaction = CURRENT.get();
        if (transaction == null) {
            return DBConnection.open();
        }
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        return null;
                    }
                    try {
                        return method.invoke(transaction.connection, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    /** A failed rollback must not hide the error that caused it. */
    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            System.err.println("Rollback failed: " + e.getMessage());
        }
    }
}
