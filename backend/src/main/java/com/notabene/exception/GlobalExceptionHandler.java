package com.notabene.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
        log.error("Database error occurred: ", ex);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Database Error");
        errorDetails.put("message", "An error occurred while accessing the database");
        errorDetails.put("debugMessage", ex.getMessage());
        errorDetails.put("debugCause", ex.getCause() != null ? ex.getCause().getMessage() : "Unknown");
        errorDetails.put("debugType", ex.getClass().getSimpleName());
        
        // Additional SQL error information if available
        if (ex.getCause() != null) {
            errorDetails.put("sqlState", extractSqlState(ex));
            errorDetails.put("sqlErrorCode", extractSqlErrorCode(ex));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoteNotFoundException(NoteNotFoundException ex) {
        log.error("Note not found: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(UnauthorizedNoteAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedNoteAccessException(UnauthorizedNoteAccessException ex) {
        log.error("Unauthorized note access: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed", 
                HttpStatus.BAD_REQUEST.value(), 
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", "An unexpected error occurred");
        errorDetails.put("debugMessage", ex.getMessage());
        errorDetails.put("debugCause", ex.getCause() != null ? ex.getCause().getMessage() : "Unknown");
        errorDetails.put("debugType", ex.getClass().getSimpleName());
        errorDetails.put("debugStackTrace", getShortStackTrace(ex));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    private String extractSqlState(DataAccessException ex) {
        try {
            if (ex.getCause() != null && ex.getCause().getClass().getName().contains("SQLException")) {
                return ex.getCause().getClass().getMethod("getSQLState").invoke(ex.getCause()).toString();
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }

    private String extractSqlErrorCode(DataAccessException ex) {
        try {
            if (ex.getCause() != null && ex.getCause().getClass().getName().contains("SQLException")) {
                return ex.getCause().getClass().getMethod("getErrorCode").invoke(ex.getCause()).toString();
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }

    private String getShortStackTrace(Exception ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();
        int maxElements = Math.min(5, stackTrace.length);
        
        for (int i = 0; i < maxElements; i++) {
            sb.append(stackTrace[i].toString());
            if (i < maxElements - 1) {
                sb.append(" -> ");
            }
        }
        
        if (stackTrace.length > maxElements) {
            sb.append(" ... (").append(stackTrace.length - maxElements).append(" more)");
        }
        
        return sb.toString();
    }
}
