package com.notabene.entity;

import com.notabene.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Entity Tests")
class NoteTest {
    
    private Validator validator;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
    }
    
    @Test
    @DisplayName("Should create note with valid data and user")
    void shouldCreateNoteWithValidData() {
        Note note = new Note("Test Title", "Test Content", testUser);
        
        assertNotNull(note);
        assertEquals("Test Title", note.getTitle());
        assertEquals("Test Content", note.getContent());
        assertEquals(testUser, note.getUser());
    }
    
    @Test
    @DisplayName("Should fail validation with blank title")
    void shouldFailValidationWithBlankTitle() {
        Note note = new Note("", "Test Content", testUser);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankTitleViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Title cannot be blank"));
        assertTrue(hasBlankTitleViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with null title")
    void shouldFailValidationWithNullTitle() {
        Note note = new Note(null, "Test Content", testUser);
        
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
        Note note = new Note(longTitle, "Test Content", testUser);
        
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
        Note note = new Note(maxTitle, "Test Content", testUser);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should fail validation with blank content")
    void shouldFailValidationWithBlankContent() {
        Note note = new Note("Test Title", "", testUser);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertFalse(violations.isEmpty());
        
        boolean hasBlankContentViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Content cannot be blank"));
        assertTrue(hasBlankContentViolation);
    }
    
    @Test
    @DisplayName("Should fail validation with null content")
    void shouldFailValidationWithNullContent() {
        Note note = new Note("Test Title", null, testUser);
        
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
        Note note = new Note("Test Title", longContent, testUser);
        
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
        Note note = new Note("Test Title", maxContent, testUser);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    @DisplayName("Should pass validation with all valid data")
    void shouldPassValidationWithAllValidData() {
        Note note = new Note("Valid Title", "Valid Content", testUser);
        
        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        assertTrue(violations.isEmpty());
    }
    
    // =========================== PERMISSION TESTS ===========================
    // These tests validate the permission functionality of the Note entity
    
    @Test
    @DisplayName("Should create note with creator as default reader and writer")
    void shouldCreateNoteWithDefaultPermissions() {
        Note note = new Note("Permission Test", "Test Content", testUser);
        
        // Permission fields should be initialized automatically via @PrePersist
        assertNotNull(note.getCreatorId());
        assertEquals(testUser.getId(), note.getCreatorId());
        
        // The creator should automatically be a reader and writer
        assertNotNull(note.getReaders());
        assertNotNull(note.getWriters());
        assertTrue(note.getReaders().contains(testUser.getId()));
        assertTrue(note.getWriters().contains(testUser.getId()));
    }
    
    @Test
    @DisplayName("Should allow adding readers")
    void shouldAllowAddingReaders() {
        Note note = new Note("Shared Note", "Content to share", testUser);
        
        // Add a reader
        Long readerId = 2L;
        note.addReader(readerId);
        
        assertTrue(note.getReaders().contains(readerId));
        assertEquals(2, note.getReaders().size()); // Creator + new reader
    }
    
    @Test
    @DisplayName("Should allow adding writers")
    void shouldAllowAddingWriters() {
        Note note = new Note("Collaborative Note", "Content to edit", testUser);
        
        // Add a writer
        Long writerId = 3L;
        note.addWriter(writerId);
        
        assertTrue(note.getWriters().contains(writerId));
        assertEquals(2, note.getWriters().size()); // Creator + new writer
    }
    
    @Test
    @DisplayName("Should allow removing readers")
    void shouldAllowRemovingReaders() {
        Note note = new Note("Restricted Note", "Private content", testUser);
        
        // Add a reader
        Long readerId = 4L;
        note.addReader(readerId);
        assertEquals(2, note.getReaders().size());
        
        // Remove the reader
        note.removeReader(readerId);
        assertEquals(1, note.getReaders().size());
        
        // The creator should always remain a reader
        assertTrue(note.getReaders().contains(testUser.getId()));
    }
    
    @Test
    @DisplayName("Should allow removing writers")
    void shouldAllowRemovingWriters() {
        Note note = new Note("Controlled Note", "Protected content", testUser);
        
        // Add a writer
        Long writerId = 5L;
        note.addWriter(writerId);
        assertEquals(2, note.getWriters().size());
        
        // Remove the writer
        note.removeWriter(writerId);
        assertEquals(1, note.getWriters().size());
        
        // The creator should always remain a writer
        assertTrue(note.getWriters().contains(testUser.getId()));
    }
    
    @Test
    @DisplayName("Should handle multiple readers and writers")
    void shouldHandleMultiplePermissions() {
        Note note = new Note("Multi-user Note", "Shared content", testUser);
        
        // Add multiple readers
        List<Long> readers = Arrays.asList(10L, 11L, 12L);
        for (Long readerId : readers) {
            note.addReader(readerId);
        }
        
        // Add multiple writers
        List<Long> writers = Arrays.asList(10L, 11L);
        for (Long writerId : writers) {
            note.addWriter(writerId);
        }
        
        assertEquals(4, note.getReaders().size()); // Creator + 3 readers
        assertEquals(3, note.getWriters().size()); // Creator + 2 writers
        
        // Verify all permissions are present
        for (Long readerId : readers) {
            assertTrue(note.getReaders().contains(readerId));
        }
        for (Long writerId : writers) {
            assertTrue(note.getWriters().contains(writerId));
        }
    }
    
    @Test
    @DisplayName("Should check read permission correctly")
    void shouldCheckReadPermissionCorrectly() {
        Note note = new Note("Permission Check Note", "Test content", testUser);
        
        // Creator should have read permission
        assertTrue(note.hasReadPermission(testUser.getId()));
        
        // Other users should not have read permission initially
        assertFalse(note.hasReadPermission(999L));
        
        // Add read permission for another user
        Long readerId = 100L;
        note.addReader(readerId);
        assertTrue(note.hasReadPermission(readerId));
    }
    
    @Test
    @DisplayName("Should check write permission correctly")
    void shouldCheckWritePermissionCorrectly() {
        Note note = new Note("Permission Check Note", "Test content", testUser);
        
        // Creator should have write permission
        assertTrue(note.hasWritePermission(testUser.getId()));
        
        // Other users should not have write permission initially
        assertFalse(note.hasWritePermission(999L));
        
        // Add write permission for another user
        Long writerId = 100L;
        note.addWriter(writerId);
        assertTrue(note.hasWritePermission(writerId));
    }
    
    @Test
    @DisplayName("Should validate creator ID is not null")
    void shouldValidateCreatorIdNotNull() {
        Note note = new Note("Creator Validation", "Test content", testUser);
        
        assertNotNull(note.getCreatorId());
        assertEquals(testUser.getId(), note.getCreatorId());
    }
    
    @Test
    @DisplayName("Should handle permission array operations correctly")
    void shouldHandlePermissionArrayOperations() {
        Note note = new Note("Array Test", "Permission array test", testUser);
        
        // Test initial state
        List<Long> initialReaders = note.getReaders();
        assertNotNull(initialReaders);
        assertEquals(1, initialReaders.size()); // Just the creator
        
        // Test adding elements
        note.addReader(100L);
        note.addReader(101L);
        note.addReader(102L);
        assertEquals(4, note.getReaders().size());
        
        // Test removing elements
        note.removeReader(100L);
        note.removeReader(102L);
        assertEquals(2, note.getReaders().size());
        assertTrue(note.getReaders().contains(testUser.getId()));
        assertTrue(note.getReaders().contains(101L));
    }
}
