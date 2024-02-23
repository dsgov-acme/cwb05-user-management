package io.nuvalence.user.management.api.service.config;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.InternalServerException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.config.exception.TokenAuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Custom REST Exception handler.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    // String constant that is pre-prepended to every message.
    private static final String ERROR_MESSAGE_PREFIX = "There was an error processing the request:";

    /**
     * Handles HttpServerErrorException and InternalServerException.
     *
     * @param ex Exception to be handled
     * @return a response entity with correct code.
     */
    @ExceptionHandler({HttpServerErrorException.class, InternalServerException.class})
    public ResponseEntity<Object> handleServiceException(Exception ex) {
        log.error(ERROR_MESSAGE_PREFIX, ex);
        return new ResponseEntity<>(
                new ApiError(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles IllegalArgumentException.
     *
     * @param ex Exception to be handled
     * @return a response entity with correct code.
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error(ERROR_MESSAGE_PREFIX, ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalStateException.
     *
     * @param ex Exception to be handled
     * @return a response entity with correct code.
     */
    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        log.error("{} {}", ERROR_MESSAGE_PREFIX, ex.getMessage());
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ResourceNotFoundException.
     *
     * @param ex Exception to be handled
     * @return a response entity with correct code.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(Exception ex) {
        log.error(ERROR_MESSAGE_PREFIX, ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles BusinessLogicException.
     *
     * @param ex Exception to be handled
     * @return a response entity with correct code.
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<Object> handleBusinessLogicException(Exception ex) {
        log.error(ERROR_MESSAGE_PREFIX, ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.CONFLICT);
    }

    /**
     * Handles TokenAuthorizationException.
     *
     * @param ex Exception to be handled
     * @return a response entity with correct code.
     */
    @ExceptionHandler(TokenAuthorizationException.class)
    public ResponseEntity<Object> handleTokenAuthorizationException(Exception ex) {
        log.error(ERROR_MESSAGE_PREFIX, ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.FORBIDDEN);
    }
}
