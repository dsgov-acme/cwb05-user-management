package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;

/**
 * Handles any thrown exceptions.
 */
@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    /**
     * Error Response Class.
     */
    @AllArgsConstructor
    @Getter
    public class ErrorResponse {
        private List<String> messages;

        public ErrorResponse(String message) {
            this.messages = Collections.singletonList(message);
        }
    }

    /**
     * Handles all other exceptions.
     * 
     * @param e Exception to capture.
     * @return Internal server error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("An unexpected error occurred: ", e);

        return ResponseEntity.internalServerError()
                .body(
                        new ErrorResponse(
                                "Internal server error. Please contact the system administrator."));
    }

    /**
     * Return a forbidden request if a ForbiddenException is thrown.
     * @param e Forbidden exception.
     * @return Forbidden request.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleException(AccessDeniedException e) {
        log.warn("User does not have permission: ", e);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Return a bad request if a ConstraintViolationException is thrown.
     * @param e ConstraintViolationException exception.
     * @return Bad request.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleException(ConstraintViolationException e) {
        return ResponseEntity.badRequest()
                .body(
                        e.getConstraintViolations().isEmpty()
                                ? new ErrorResponse(e.getMessage())
                                : new ErrorResponse(
                                        e.getConstraintViolations().stream()
                                                .map(
                                                        violation ->
                                                                String.format(
                                                                        "'%s': %s",
                                                                        violation.getPropertyPath(),
                                                                        violation.getMessage()))
                                                .toList()));
    }

    /**
     * Return Bad request if MethodArgumentNotValidException is thrown in the code.
     * @param e exception
     * @return ResponseEntity for HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(
                        e.getFieldErrorCount() == 0
                                ? new ErrorResponse(e.getMessage())
                                : new ErrorResponse(
                                        e.getFieldErrors().stream()
                                                .map(
                                                        fieldError ->
                                                                String.format(
                                                                        "'%s': %s",
                                                                        fieldError.getField(),
                                                                        fieldError
                                                                                .getDefaultMessage()))
                                                .toList()));
    }

    /**
     * Handles business logic.
     *
     * @param e Business logic exception.
     * @return Forbidden request.
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleException(BusinessLogicException e) {
        log.warn("A business logic error has occurred: ", e);

        return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Return a forbidden request if a ForbiddenException is thrown.
     *
     * @param e Forbidden exception.
     * @return Forbidden request.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleException(ForbiddenException e) {
        log.warn("User does not have permission: ", e);

        return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Return a not found request if a NotFoundException is thrown.
     *
     * @param e NotFoundException exception.
     * @return NotFound request.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(NotFoundException e) {
        log.warn("Resource not found: {}", e);

        return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
    }
}
