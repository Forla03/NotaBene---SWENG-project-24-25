package com.notabene.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.beans.TypeMismatchException;
import java.time.format.DateTimeParseException;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(assignableTypes = com.notabene.controller.NoteController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

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
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {        
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", "Errore inatteso: " + ex.getMessage());
        errorDetails.put("debugMessage", ex.getMessage());
        errorDetails.put("debugCause", ex.getCause() != null ? ex.getCause().getMessage() : "Unknown");
        errorDetails.put("debugType", ex.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    TypeMismatchException.class,
    ConversionFailedException.class,
    DateTimeParseException.class
    })
    public ResponseEntity<ErrorResponse> handleTypeMismatch(Exception ex, WebRequest request) {

        String message = "Invalid request parameter";

        if (ex instanceof MethodArgumentTypeMismatchException matme) {
            String param = matme.getName();
            String required = matme.getRequiredType() != null
                    ? matme.getRequiredType().getSimpleName()
                    : "unknown";
            String value = String.valueOf(matme.getValue());
            message = "Invalid value for parameter '%s': expected %s, got '%s'"
                    .formatted(param, required, value);
        } else if (ex.getCause() instanceof DateTimeParseException dtpe) {
            message = "Invalid date/time format: " + dtpe.getParsedString();
        } else if (ex instanceof DateTimeParseException dtpe) {
            message = "Invalid date/time format: " + dtpe.getParsedString();
        }

        ErrorResponse body = new ErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                List.of(message)
        );
        return ResponseEntity.badRequest().body(body);
    }
}
