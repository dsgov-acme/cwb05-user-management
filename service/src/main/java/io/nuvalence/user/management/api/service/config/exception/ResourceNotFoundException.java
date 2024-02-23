package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom Resource Not Found Exception.
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -2046541892043854529L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
