package com.notabene.exception;

/**
 * Exception thrown when a user tries to access a note without proper permissions
 */
public class UnauthorizedNoteAccessException extends RuntimeException {
    
    public UnauthorizedNoteAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedNoteAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
