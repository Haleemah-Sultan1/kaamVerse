package kaamverse.common;

/**
 * Thrown by the DAO layer for any database failure.
 * GRASP Low Coupling: upper layers never see SQLException.
 * Unchecked, so it can travel up to the UI, which shows a friendly alert.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
