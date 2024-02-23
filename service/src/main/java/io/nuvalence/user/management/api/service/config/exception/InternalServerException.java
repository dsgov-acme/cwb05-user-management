package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom InternalServer Exception class.
 */
public class InternalServerException extends RuntimeException {
    private static final long serialVersionUID = -8203245258347642340L;

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
