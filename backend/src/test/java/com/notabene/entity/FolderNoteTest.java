package com.notabene.entity;

import com.notabene.model.FolderNoteId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Folder Note Entity Tests")
class FolderNoteTest {

    private Validator validator;
    private static final Long TEST_FOLDER_ID = 1L;
    private static final Long TEST_NOTE_ID = 100L;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create folder note with valid composite key")
    void shouldCreateFolderNoteWithValidCompositeKey() {
        // Given
        FolderNoteId id = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        FolderNote folderNote = new FolderNote(id);
        
        // When
        Set<ConstraintViolation<FolderNote>> violations = validator.validate(folderNote);
        
        // Then
        assertTrue(violations.isEmpty());
        assertNotNull(folderNote.getId());
        assertEquals(TEST_FOLDER_ID, folderNote.getId().getFolderId());
        assertEquals(TEST_NOTE_ID, folderNote.getId().getNoteId());
    }

    @Test
    @DisplayName("Should create folder note with default constructor")
    void shouldCreateFolderNoteWithDefaultConstructor() {
        // Given & When
        FolderNote folderNote = new FolderNote();
        
        // Then
        assertNotNull(folderNote);
        assertNull(folderNote.getId());
    }

    @Test
    @DisplayName("Should create folder note id with valid data")
    void shouldCreateFolderNoteIdWithValidData() {
        // Given & When
        FolderNoteId id = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        
        // Then
        assertEquals(TEST_FOLDER_ID, id.getFolderId());
        assertEquals(TEST_NOTE_ID, id.getNoteId());
    }

    @Test
    @DisplayName("Should create folder note id with default constructor")
    void shouldCreateFolderNoteIdWithDefaultConstructor() {
        // Given & When
        FolderNoteId id = new FolderNoteId();
        
        // Then
        assertNotNull(id);
        assertNull(id.getFolderId());
        assertNull(id.getNoteId());
    }

    @Test
    @DisplayName("Should handle folder note id equality correctly")
    void shouldHandleFolderNoteIdEqualityCorrectly() {
        // Given
        FolderNoteId id1 = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        FolderNoteId id2 = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        FolderNoteId id3 = new FolderNoteId(TEST_FOLDER_ID, 999L);
        
        // When & Then
        assertEquals(id1, id2); // Same values should be equal
        assertNotEquals(id1, id3); // Different note ID should not be equal
        assertEquals(id1, id1); // Self equality
    }

    @Test
    @DisplayName("Should handle folder note id hash code correctly")
    void shouldHandleFolderNoteIdHashCodeCorrectly() {
        // Given
        FolderNoteId id1 = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        FolderNoteId id2 = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        FolderNoteId id3 = new FolderNoteId(TEST_FOLDER_ID, 999L);
        
        // When & Then
        assertEquals(id1.hashCode(), id2.hashCode()); // Same values should have same hash
        assertNotEquals(id1.hashCode(), id3.hashCode()); // Different values should have different hash
    }

    @Test
    @DisplayName("Should handle null values in folder note id equality")
    void shouldHandleNullValuesInFolderNoteIdEquality() {
        // Given
        FolderNoteId id1 = new FolderNoteId(null, null);
        FolderNoteId id2 = new FolderNoteId(null, null);
        FolderNoteId id3 = new FolderNoteId(TEST_FOLDER_ID, null);
        FolderNoteId id4 = new FolderNoteId(null, TEST_NOTE_ID);
        
        // When & Then
        assertEquals(id1, id2); // Both null should be equal
        assertNotEquals(id1, id3); // Different null patterns should not be equal
        assertNotEquals(id1, id4); // Different null patterns should not be equal
        assertNotEquals(id3, id4); // Different null patterns should not be equal
    }

    @Test
    @DisplayName("Should not equal null or different class")
    void shouldNotEqualNullOrDifferentClass() {
        // Given
        FolderNoteId id = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        
        // When & Then
        assertNotEquals(id, null);
        assertNotEquals(id, "Not a FolderNoteId");
        assertNotEquals(id, new Object());
    }

    @Test
    @DisplayName("Should validate folder note relationship")
    void shouldValidateFolderNoteRelationship() {
        // Given
        FolderNoteId id1 = new FolderNoteId(1L, 100L);
        FolderNoteId id2 = new FolderNoteId(1L, 200L); // Same folder, different note
        FolderNoteId id3 = new FolderNoteId(2L, 100L); // Different folder, same note
        
        FolderNote folderNote1 = new FolderNote(id1);
        FolderNote folderNote2 = new FolderNote(id2);
        FolderNote folderNote3 = new FolderNote(id3);
        
        // When
        Set<ConstraintViolation<FolderNote>> violations1 = validator.validate(folderNote1);
        Set<ConstraintViolation<FolderNote>> violations2 = validator.validate(folderNote2);
        Set<ConstraintViolation<FolderNote>> violations3 = validator.validate(folderNote3);
        
        // Then
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertTrue(violations3.isEmpty());
        
        // Each relationship should be distinct
        assertNotEquals(folderNote1.getId(), folderNote2.getId());
        assertNotEquals(folderNote1.getId(), folderNote3.getId());
        assertNotEquals(folderNote2.getId(), folderNote3.getId());
    }

    @Test
    @DisplayName("Should handle large ID values")
    void shouldHandleLargeIdValues() {
        // Given
        Long largeFolderId = Long.MAX_VALUE;
        Long largeNoteId = Long.MAX_VALUE - 1;
        
        FolderNoteId id = new FolderNoteId(largeFolderId, largeNoteId);
        FolderNote folderNote = new FolderNote(id);
        
        // When
        Set<ConstraintViolation<FolderNote>> violations = validator.validate(folderNote);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(largeFolderId, folderNote.getId().getFolderId());
        assertEquals(largeNoteId, folderNote.getId().getNoteId());
    }

    @Test
    @DisplayName("Should handle zero ID values")
    void shouldHandleZeroIdValues() {
        // Given
        FolderNoteId id = new FolderNoteId(0L, 0L);
        FolderNote folderNote = new FolderNote(id);
        
        // When
        Set<ConstraintViolation<FolderNote>> violations = validator.validate(folderNote);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(0L, folderNote.getId().getFolderId());
        assertEquals(0L, folderNote.getId().getNoteId());
    }

    @Test
    @DisplayName("Should be serializable")
    void shouldBeSerializable() {
        // Given
        FolderNoteId id = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        
        // When & Then
        // FolderNoteId implements Serializable, so it should be serializable
        assertTrue(id instanceof java.io.Serializable);
        
        // The actual serialization test would require more setup,
        // but we can verify it implements the interface correctly
        assertDoesNotThrow(() -> {
            // This ensures the serialization-related methods don't throw
            id.hashCode();
            id.equals(id);
        });
    }

    @Test
    @DisplayName("Should maintain referential integrity")
    void shouldMaintainReferentialIntegrity() {
        // Given
        FolderNoteId id = new FolderNoteId(TEST_FOLDER_ID, TEST_NOTE_ID);
        FolderNote folderNote1 = new FolderNote(id);
        FolderNote folderNote2 = new FolderNote(id);
        
        // When & Then
        // Both folder notes reference the same ID object
        assertSame(id, folderNote1.getId());
        assertSame(id, folderNote2.getId());
        
        // But the folder note objects themselves are different
        assertNotSame(folderNote1, folderNote2);
        
        // However, their IDs are equal
        assertEquals(folderNote1.getId(), folderNote2.getId());
    }

    @Test
    @DisplayName("Should handle edge case combinations")
    void shouldHandleEdgeCaseCombinations() {
        // Given - Test various edge case combinations
        FolderNoteId[] testIds = {
            new FolderNoteId(1L, 1L),           // Same values
            new FolderNoteId(Long.MIN_VALUE, Long.MAX_VALUE), // Extreme values
            new FolderNoteId(Long.MAX_VALUE, Long.MIN_VALUE), // Reversed extreme values
            new FolderNoteId(999999999L, 1L),   // Large folder ID, small note ID
            new FolderNoteId(1L, 999999999L)    // Small folder ID, large note ID
        };
        
        // When & Then
        for (FolderNoteId id : testIds) {
            FolderNote folderNote = new FolderNote(id);
            Set<ConstraintViolation<FolderNote>> violations = validator.validate(folderNote);
            
            assertTrue(violations.isEmpty(), 
                "Validation should pass for ID: " + id.getFolderId() + ", " + id.getNoteId());
            assertEquals(id, folderNote.getId());
        }
    }
}
