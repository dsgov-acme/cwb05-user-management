package io.nuvalence.user.management.api.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

class GlobalErrorHandlerTest {
    private GlobalErrorHandler globalErrorHandler;

    @BeforeEach
    public void setUp() {
        globalErrorHandler = new GlobalErrorHandler();
    }

    @Test
    void testHandleExceptionConstraintViolationException() {
        String inputMessage = "must be either [ABC, DEF]";
        MethodArgumentNotValidException mockedException =
                Mockito.mock(MethodArgumentNotValidException.class);
        FieldError fieldError = new FieldError("User", "username", inputMessage);

        Mockito.when(mockedException.getFieldErrorCount()).thenReturn(1);
        Mockito.when(mockedException.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<GlobalErrorHandler.ErrorResponse> response =
                globalErrorHandler.handleException(mockedException);

        String expectedOutputMessage = "'username': must be either [ABC, DEF]";

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().getMessages().size());
        assertEquals(expectedOutputMessage, response.getBody().getMessages().get(0));
    }

    @Test
    void testHandleExceptionConstraintViolationException_ReturnPlainMessage() {
        String expectedMessage = "must be either [ABC, DEF]";
        MethodArgumentNotValidException mockedException =
                Mockito.mock(MethodArgumentNotValidException.class);

        Mockito.when(mockedException.getFieldErrorCount()).thenReturn(0);
        Mockito.when(mockedException.getMessage()).thenReturn(expectedMessage);

        ResponseEntity<GlobalErrorHandler.ErrorResponse> response =
                globalErrorHandler.handleException(mockedException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().getMessages().size());
        assertEquals(expectedMessage, response.getBody().getMessages().get(0));
    }
}
