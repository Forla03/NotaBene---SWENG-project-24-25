package com.notabene.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Version Entity Tests")
class NoteVersionTest {

    private Validator validator;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    }

    @Test
    @DisplayName("Should create valid note version")
    void shouldCreateValidNoteVersion() {
        // Given
        NoteVersion version = new NoteVersion();
        version.setNoteId(1L);
        version.setVersionNumber(1);
        version.setTitle("Test Title");
        version.setContent("Test Content");
        version.setCreatedBy(1L);
        version.setNoteCreatorId(1L);
        version.setCreatedAt(fixedTime);
        version.setOriginalCreatedAt(fixedTime);
        version.setOriginalUpdatedAt(fixedTime);
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(1L, version.getNoteId());
        assertEquals(1, version.getVersionNumber());
        assertEquals("Test Title", version.getTitle());
        assertEquals("Test Content", version.getContent());
        assertEquals(1L, version.getCreatedBy());
    }

    @Test
    @DisplayName("Should fail validation with null note ID")
    void shouldFailValidationWithNullNoteId() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setNoteId(null);
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Note ID cannot be null")));
    }

    @Test
    @DisplayName("Should fail validation with null version number")
    void shouldFailValidationWithNullVersionNumber() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setVersionNumber(null);
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Version number cannot be null")));
    }

    @Test
    @DisplayName("Should fail validation with negative version number")
    void shouldFailValidationWithNegativeVersionNumber() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setVersionNumber(-1);
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Version number must be positive")));
    }

    @Test
    @DisplayName("Should fail validation with blank title")
    void shouldFailValidationWithBlankTitle() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setTitle("");
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Title cannot be blank")));
    }

    @Test
    @DisplayName("Should fail validation with title too long")
    void shouldFailValidationWithTitleTooLong() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setTitle("a".repeat(256)); // Exceed 255 character limit
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Title cannot exceed 255 characters")));
    }

    @Test
    @DisplayName("Should fail validation with blank content")
    void shouldFailValidationWithBlankContent() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setContent("");
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Content cannot be blank")));
    }

    @Test
    @DisplayName("Should handle permission arrays correctly")
    void shouldHandlePermissionArraysCorrectly() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setReaders(Arrays.asList(1L, 2L, 3L));
        version.setWriters(Arrays.asList(1L, 2L));
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(Arrays.asList(1L, 2L, 3L), version.getReaders());
        assertEquals(Arrays.asList(1L, 2L), version.getWriters());
    }

    @Test
    @DisplayName("Should handle empty permission arrays")
    void shouldHandleEmptyPermissionArrays() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setReaders(Arrays.asList());
        version.setWriters(Arrays.asList());
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertTrue(violations.isEmpty());
        assertTrue(version.getReaders().isEmpty());
        assertTrue(version.getWriters().isEmpty());
    }

    @Test
    @DisplayName("Should set created at timestamp automatically")
    void shouldSetCreatedAtTimestampAutomatically() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setCreatedAt(null);
        
        // When
        version.onCreate(); // Simulate @PrePersist
        
        // Then
        assertNotNull(version.getCreatedAt());
        assertTrue(version.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should preserve original timestamps")
    void shouldPreserveOriginalTimestamps() {
        // Given
        NoteVersion version = createValidNoteVersion();
        LocalDateTime originalCreated = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime originalUpdated = LocalDateTime.of(2023, 1, 1, 11, 0);
        
        version.setOriginalCreatedAt(originalCreated);
        version.setOriginalUpdatedAt(originalUpdated);
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(originalCreated, version.getOriginalCreatedAt());
        assertEquals(originalUpdated, version.getOriginalUpdatedAt());
    }

    @Test
    @DisplayName("Should fail validation with null created by")
    void shouldFailValidationWithNullCreatedBy() {
        // Given
        NoteVersion version = createValidNoteVersion();
        version.setCreatedBy(null);
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Created by cannot be null")));
    }

    @Test
    @DisplayName("Should store version metadata correctly")
    void shouldStoreVersionMetadataCorrectly() {
        // Given
        NoteVersion version = new NoteVersion();
        version.setNoteId(100L);
        version.setVersionNumber(5);
        version.setTitle("Version 5 Title");
        version.setContent("Version 5 Content");
        version.setCreatedBy(2L);
        version.setNoteCreatorId(1L);
        version.setCreatedAt(fixedTime);
        version.setOriginalCreatedAt(fixedTime.minusDays(5));
        version.setOriginalUpdatedAt(fixedTime.minusHours(1));
        
        // When
        Set<ConstraintViolation<NoteVersion>> violations = validator.validate(version);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(100L, version.getNoteId());
        assertEquals(5, version.getVersionNumber());
        assertEquals(2L, version.getCreatedBy());
        assertEquals(1L, version.getNoteCreatorId());
        assertTrue(version.getOriginalCreatedAt().isBefore(version.getOriginalUpdatedAt()));
        assertTrue(version.getOriginalUpdatedAt().isBefore(version.getCreatedAt()));
    }

    // Helper method to create a valid NoteVersion
    private NoteVersion createValidNoteVersion() {
        NoteVersion version = new NoteVersion();
        version.setNoteId(1L);
        version.setVersionNumber(1);
        version.setTitle("Test Title");
        version.setContent("Test Content");
        version.setCreatedBy(1L);
        version.setNoteCreatorId(1L);
        version.setCreatedAt(fixedTime);
        version.setOriginalCreatedAt(fixedTime.minusHours(1));
        version.setOriginalUpdatedAt(fixedTime.minusMinutes(30));
        return version;
    }
}
