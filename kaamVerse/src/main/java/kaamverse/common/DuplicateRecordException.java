package kaamverse.common;

/** A unique value, such as a phone number or CNIC, is already in use. */
public class DuplicateRecordException extends DatabaseException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
