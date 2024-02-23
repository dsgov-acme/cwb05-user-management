package io.nuvalence.user.management.api.service.config.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.config.ApiError;
import io.nuvalence.user.management.api.service.config.RestExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RestExceptionHandlerTest {

    private RestExceptionHandler restExceptionHandler;

    private String testMessage = "Test exception message";

    @BeforeEach
    public void setUp() {
        restExceptionHandler = new RestExceptionHandler();
    }

    @Test
    void handleInternalServerExceptionTest() {
        InternalServerException exception = new InternalServerException(testMessage);

        ResponseEntity<Object> result = restExceptionHandler.handleServiceException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());

        ApiError apiError = (ApiError) result.getBody();
        assertEquals(testMessage, apiError.getMessage());
    }

    @Test
    void handleIllegalArgumentExceptionTest() {
        IllegalArgumentException exception = new IllegalArgumentException(testMessage);

        ResponseEntity<Object> result =
                restExceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        ApiError apiError = (ApiError) result.getBody();
        assertEquals(testMessage, apiError.getMessage());
    }

    @Test
    void handleIllegalStateExceptionTest() {
        IllegalStateException exception = new IllegalStateException(testMessage);

        ResponseEntity<Object> result = restExceptionHandler.handleIllegalStateException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        ApiError apiError = (ApiError) result.getBody();
        assertEquals(testMessage, apiError.getMessage());
    }

    @Test
    void handleResourceNotFoundExceptionTest() {
        ResourceNotFoundException exception = new ResourceNotFoundException(testMessage);

        ResponseEntity<Object> result = restExceptionHandler.handleNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

        ApiError apiError = (ApiError) result.getBody();
        assertEquals(testMessage, apiError.getMessage());
    }

    @Test
    void handleBusinessLogicExceptionTest() {
        BusinessLogicException exception = new BusinessLogicException(testMessage);

        ResponseEntity<Object> result =
                restExceptionHandler.handleBusinessLogicException(exception);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());

        ApiError apiError = (ApiError) result.getBody();
        assertEquals(testMessage, apiError.getMessage());
    }

    @Test
    void handleTokenAuthorizationExceptionTest() {
        TokenAuthorizationException exception = new TokenAuthorizationException(testMessage);

        ResponseEntity<Object> result =
                restExceptionHandler.handleTokenAuthorizationException(exception);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());

        ApiError apiError = (ApiError) result.getBody();
        assertEquals(testMessage, apiError.getMessage());
    }
}
