package kaamverse.bl.controller;

import kaamverse.bl.model.RegistrationField;

/** Input the user must correct. The message is ready to show on screen. */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final RegistrationField field;

    public ValidationException(String message) {
        this(null, message);
    }

    public ValidationException(RegistrationField field, String message) {
        super(message);
        this.field = field;
    }

    /** The field to correct, or null if the problem is not one text field. */
    public RegistrationField getField() {
        return field;
    }
}
