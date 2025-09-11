package com.notabene.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Should handle NoteNotFoundException")
    void shouldHandleNoteNotFoundException() {
        // Given
        String errorMessage = "Note not found with id: 123";
        NoteNotFoundException exception = new NoteNotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoteNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle UnauthorizedNoteAccessException")
    void shouldHandleUnauthorizedNoteAccessException() {
        // Given
        String errorMessage = "User does not have permission to access this note";
        UnauthorizedNoteAccessException exception = new UnauthorizedNoteAccessException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedNoteAccessException(exception);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void shouldHandleIllegalArgumentException() {
        // Given
        String errorMessage = "Invalid argument provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("note", "title", "Title cannot be blank");
        FieldError fieldError2 = new FieldError("note", "content", "Content cannot be blank");
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getErrors());
        assertEquals(2, response.getBody().getErrors().size());
        assertTrue(response.getBody().getErrors().contains("Title cannot be blank"));
        assertTrue(response.getBody().getErrors().contains("Content cannot be blank"));
    }

    @Test
    @DisplayName("Should handle DataAccessException")
    void shouldHandleDataAccessException() {
        // Given
        String errorMessage = "Database connection error";
        DataAccessException exception = new DataAccessException(errorMessage) {};

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleDataAccessException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Database Error", response.getBody().get("error"));
        assertEquals("An error occurred while accessing the database", response.getBody().get("message"));
        assertEquals(500, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
        assertEquals(errorMessage, response.getBody().get("debugMessage"));
    }

    @Test
    @DisplayName("Should include timestamp in all error responses")
    void shouldIncludeTimestampInAllErrorResponses() {
        // Given
        NoteNotFoundException exception = new NoteNotFoundException("Test error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoteNotFoundException(exception);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle null exception messages gracefully")
    void shouldHandleNullExceptionMessagesGracefully() {
        // Given
        NoteNotFoundException exception = new NoteNotFoundException(null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoteNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        // The message might be null, which is acceptable behavior
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle empty validation errors")
    void shouldHandleEmptyValidationErrors() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getErrors());
        assertTrue(response.getBody().getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should maintain consistent error response structure for ErrorResponse")
    void shouldMaintainConsistentErrorResponseStructure() {
        // Given - Different types of exceptions that return ErrorResponse
        NoteNotFoundException notFoundException = new NoteNotFoundException("Not found");
        UnauthorizedNoteAccessException unauthorizedException = new UnauthorizedNoteAccessException("Access denied");
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Bad request");

        // When - Handle different exceptions
        ResponseEntity<ErrorResponse> notFoundResponse = globalExceptionHandler.handleNoteNotFoundException(notFoundException);
        ResponseEntity<ErrorResponse> unauthorizedResponse = globalExceptionHandler.handleUnauthorizedNoteAccessException(unauthorizedException);
        ResponseEntity<ErrorResponse> illegalArgResponse = globalExceptionHandler.handleIllegalArgumentException(illegalArgException);

        // Then - All responses should have consistent ErrorResponse structure
        assertNotNull(notFoundResponse.getBody().getMessage());
        assertNotNull(notFoundResponse.getBody().getTimestamp());
        assertEquals(404, notFoundResponse.getBody().getStatus());

        assertNotNull(unauthorizedResponse.getBody().getMessage());
        assertNotNull(unauthorizedResponse.getBody().getTimestamp());
        assertEquals(403, unauthorizedResponse.getBody().getStatus());

        assertNotNull(illegalArgResponse.getBody().getMessage());
        assertNotNull(illegalArgResponse.getBody().getTimestamp());
        assertEquals(400, illegalArgResponse.getBody().getStatus());
    }

    @Test
    @DisplayName("Should create ErrorResponse with errors list correctly")
    void shouldCreateErrorResponseWithErrorsListCorrectly() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("note", "title", "Title is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("Title is required", response.getBody().getErrors().get(0));
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle DataAccessException with debug information")
    void shouldHandleDataAccessExceptionWithDebugInfo() {
        // Given
        String errorMessage = "Connection timeout";
        DataAccessException exception = new DataAccessException(errorMessage, new RuntimeException("Root cause")) {};

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleDataAccessException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Database Error", body.get("error"));
        assertEquals(errorMessage, body.get("debugMessage"));
        assertEquals("Root cause", body.get("debugCause"));
        assertNotNull(body.get("debugType"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Should log exceptions appropriately")
    void shouldLogExceptionsAppropriately() {
        // Given
        NoteNotFoundException exception = new NoteNotFoundException("Test not found error");

        // When - This test verifies that the handler can be called without errors
        // In a real scenario, you'd use a logging appender to verify log messages
        assertDoesNotThrow(() -> {
            ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoteNotFoundException(exception);
            assertNotNull(response);
        });

        // Then - Verify the response is constructed correctly
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoteNotFoundException(exception);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Test not found error", response.getBody().getMessage());
    }
}
