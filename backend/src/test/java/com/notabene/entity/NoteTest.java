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
        Note note = new Note("Test Title", "Test Content", 3);
        
        assertNotNull(note);
        assertEquals("Test Title", note.getTitle());
        assertEquals("Test Content", note.getContent());
        assertEquals(3, note.getPriority());
    }
    
    @Test
    @DisplayName("Should create note with default priority when null")
    void shouldCreateNoteWithDefaultPriorityWhenNull() {
        Note note = new Note("Test Title", "Test Content", null);
        
        assertEquals(1, note.getPriority());
    }
    
    @Test
    @DisplayName("Should fail validation with blank title")
    void shouldFailValidationWithBlankTitle() {
        Note note = new Note("", "Test Content", 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankTitleViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Title cannot be blank"));
        assertTrue(hasBlankTitleViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with null title")
    void shouldFailValidationWithNullTitle() {
        Note note = new Note(null, "Test Content", 1);
        
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
        Note note = new Note(longTitle, "Test Content", 1);
        
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
        Note note = new Note(maxTitle, "Test Content", 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should fail validation with blank content")
    void shouldFailValidationWithBlankContent() {
        Note note = new Note("Test Title", "", 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankContentViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot be blank"));
        assertTrue(hasBlankContentViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with null content")
    void shouldFailValidationWithNullContent() {
        Note note = new Note("Test Title", null, 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankContentViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot be blank"));
        assertTrue(hasBlankContentViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with content too long")
    void shouldFailValidationWithContentTooLong() {
        String longContent = "a".repeat(10001); // 10001 characters
        Note note = new Note("Test Title", longContent, 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasSizeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot exceed 10000 characters"));
        assertTrue(hasSizeViolation);
    }
    
    @Test
    @DisplayName("Should pass validation with content at max length")
    void shouldPassValidationWithContentAtMaxLength() {
        String maxContent = "a".repeat(10000); // exactly 10000 characters
        Note note = new Note("Test Title", maxContent, 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should fail validation with priority below minimum")
    void shouldFailValidationWithPriorityBelowMinimum() {
        Note note = new Note("Test Title", "Test Content", 0);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasMinViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Priority must be at least 1"));
        assertTrue(hasMinViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with priority above maximum")
    void shouldFailValidationWithPriorityAboveMaximum() {
        Note note = new Note("Test Title", "Test Content", 6);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasMaxViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Priority cannot exceed 5"));
        assertTrue(hasMaxViolation);
    }
    
    @Test
    @DisplayName("Should pass validation with priority at minimum")
    void shouldPassValidationWithPriorityAtMinimum() {
        Note note = new Note("Test Title", "Test Content", 1);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should pass validation with priority at maximum")
    void shouldPassValidationWithPriorityAtMaximum() {
        Note note = new Note("Test Title", "Test Content", 5);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should fail validation with null priority")
    void shouldFailValidationWithNullPriority() {
        Note note = new Note("Test Title", "Test Content", 1);
        note.setPriority(null); // Set to null after creation
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasNotNullViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Priority cannot be null"));
        assertTrue(hasNotNullViolation);
    }
    
    @Test
    @DisplayName("Should pass validation with all valid data")
    void shouldPassValidationWithAllValidData() {
        Note note = new Note("Valid Title", "Valid Content", 3);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
}
