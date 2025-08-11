package com.notabene.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Entity Tests")
class NoteTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create note with valid data")
    void shouldCreateNoteWithValidData() {
        Note note = new Note("Test Title", "Test Content");
        
        assertNotNull(note);
        assertEquals("Test Title", note.getTitle());
        assertEquals("Test Content", note.getContent());
    }
    
    @Test
    @DisplayName("Should fail validation with blank title")
    void shouldFailValidationWithBlankTitle() {
        Note note = new Note("", "Test Content");
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankTitleViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Title cannot be blank"));
        assertTrue(hasBlankTitleViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with null title")
    void shouldFailValidationWithNullTitle() {
        Note note = new Note(null, "Test Content");
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankTitleViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Title cannot be blank"));
        assertTrue(hasBlankTitleViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with title too long")
    void shouldFailValidationWithTitleTooLong() {
        String longTitle = "a".repeat(256); // 256 characters
        Note note = new Note(longTitle, "Test Content");
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasSizeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Title cannot exceed 255 characters"));
        assertTrue(hasSizeViolation);
    }
    
    @Test
    @DisplayName("Should pass validation with title at max length")
    void shouldPassValidationWithTitleAtMaxLength() {
        String maxTitle = "a".repeat(255); // exactly 255 characters
        Note note = new Note(maxTitle, "Test Content");
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should fail validation with blank content")
    void shouldFailValidationWithBlankContent() {
        Note note = new Note("Test Title", "");
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankContentViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot be blank"));
        assertTrue(hasBlankContentViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with null content")
    void shouldFailValidationWithNullContent() {
        Note note = new Note("Test Title", null);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankContentViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot be blank"));
        assertTrue(hasBlankContentViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with content too long")
    void shouldFailValidationWithContentTooLong() {
        String longContent = "a".repeat(281); // 281 characters
        Note note = new Note("Test Title", longContent);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasSizeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot exceed 280 characters"));
        assertTrue(hasSizeViolation);
    }
    
    @Test
    @DisplayName("Should pass validation with content at max length")
    void shouldPassValidationWithContentAtMaxLength() {
        String maxContent = "a".repeat(280); // exactly 280 characters
        Note note = new Note("Test Title", maxContent);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should pass validation with all valid data")
    void shouldPassValidationWithAllValidData() {
        Note note = new Note("Valid Title", "Valid Content");
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
}
