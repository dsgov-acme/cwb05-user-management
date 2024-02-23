package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom exception for token authorization issues.
 */
public class TokenAuthorizationException extends RuntimeException {
    private static final long serialVersionUID = 4684155367349689729L;

    public TokenAuthorizationException(String message) {
        super(message);
    }
}
