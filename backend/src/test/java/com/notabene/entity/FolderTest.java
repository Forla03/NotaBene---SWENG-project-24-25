package com.notabene.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Folder Entity Tests")
class FolderTest {

    private Validator validator;
    private static final Long TEST_USER_ID = 1L;
    private static final String VALID_FOLDER_NAME = "Test Folder";

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create folder with valid data")
    void shouldCreateFolderWithValidData() {
        // Given
        Folder folder = new Folder(TEST_USER_ID, VALID_FOLDER_NAME);
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty()); // Should pass since JPA constraints don't show up in Bean Validation
        assertEquals(TEST_USER_ID, folder.getOwnerId());
        assertEquals(VALID_FOLDER_NAME, folder.getName());
        assertNull(folder.getId()); // Should be null before persistence
        assertNull(folder.getCreatedAt()); // Should be null before @PrePersist
    }

    @Test
    @DisplayName("Should create folder with default constructor")
    void shouldCreateFolderWithDefaultConstructor() {
        // Given & When
        Folder folder = new Folder();
        
        // Then
        assertNotNull(folder);
        assertNull(folder.getId());
        assertNull(folder.getOwnerId());
        assertNull(folder.getName());
        assertNull(folder.getCreatedAt());
    }

    @Test
    @DisplayName("Should accept null values in fields (validation happens at JPA level)")
    void shouldAcceptNullValuesInFields() {
        // Given - Since there are no Bean Validation annotations,
        // null values will be accepted at the entity level
        // JPA constraints (nullable=false) are enforced at persistence time
        Folder folder = new Folder();
        folder.setName(null);
        folder.setOwnerId(null);
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty()); // Bean validation passes, JPA would fail at persistence
        assertNull(folder.getOwnerId());
        assertNull(folder.getName());
    }

    @Test
    @DisplayName("Should accept empty and blank names (validation happens at service level)")
    void shouldAcceptEmptyAndBlankNames() {
        // Given - No Bean Validation annotations on name field
        Folder folder1 = new Folder(TEST_USER_ID, "");
        Folder folder2 = new Folder(TEST_USER_ID, "   ");
        
        // When
        Set<ConstraintViolation<Folder>> violations1 = validator.validate(folder1);
        Set<ConstraintViolation<Folder>> violations2 = validator.validate(folder2);
        
        // Then
        assertTrue(violations1.isEmpty()); // Bean validation passes
        assertTrue(violations2.isEmpty()); // Bean validation passes
        assertEquals("", folder1.getName());
        assertEquals("   ", folder2.getName());
    }

    @Test
    @DisplayName("Should accept name longer than database limit (validation happens at JPA level)")
    void shouldAcceptNameLongerThanDatabaseLimit() {
        // Given - Create a name longer than 120 characters (database limit)
        String longName = "a".repeat(121);
        Folder folder = new Folder(TEST_USER_ID, longName);
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty()); // Bean validation passes, JPA would fail at persistence
        assertEquals(longName, folder.getName());
        assertEquals(121, folder.getName().length());
    }

    @Test
    @DisplayName("Should accept name at database limit")
    void shouldAcceptNameAtDatabaseLimit() {
        // Given - Create a name with exactly 120 characters (database limit)
        String maxLengthName = "a".repeat(120);
        Folder folder = new Folder(TEST_USER_ID, maxLengthName);
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(maxLengthName, folder.getName());
        assertEquals(120, folder.getName().length());
    }

    @Test
    @DisplayName("Should set and get ID correctly")
    void shouldSetAndGetIdCorrectly() {
        // Given
        Folder folder = new Folder(TEST_USER_ID, VALID_FOLDER_NAME);
        Long expectedId = 100L;
        
        // When
        folder.setId(expectedId);
        
        // Then
        assertEquals(expectedId, folder.getId());
    }

    @Test
    @DisplayName("Should set and get owner ID correctly")
    void shouldSetAndGetOwnerIdCorrectly() {
        // Given
        Folder folder = new Folder();
        Long expectedOwnerId = 999L;
        
        // When
        folder.setOwnerId(expectedOwnerId);
        
        // Then
        assertEquals(expectedOwnerId, folder.getOwnerId());
    }

    @Test
    @DisplayName("Should set and get name correctly")
    void shouldSetAndGetNameCorrectly() {
        // Given
        Folder folder = new Folder();
        String expectedName = "Updated Folder Name";
        
        // When
        folder.setName(expectedName);
        
        // Then
        assertEquals(expectedName, folder.getName());
    }

    @Test
    @DisplayName("Should handle created at timestamp field")
    void shouldHandleCreatedAtTimestampField() {
        // Given
        Folder folder = new Folder(TEST_USER_ID, VALID_FOLDER_NAME);
        
        // When - Initially should be null (before persistence)
        assertNull(folder.getCreatedAt());
        
        // Then - We can't test the actual @CreationTimestamp behavior without a full JPA context
        // but we can verify the field exists and would be set by Hibernate during persistence
        // The @CreationTimestamp annotation ensures this field is populated automatically
        
        // Note: There's no setter for createdAt as it should only be set by @CreationTimestamp
        // This is correct behavior - timestamp should only be set by Hibernate
    }

    @Test
    @DisplayName("Should create multiple folders for different owners")
    void shouldCreateMultipleFoldersForDifferentOwners() {
        // Given
        Long owner1 = 1L;
        Long owner2 = 2L;
        String folderName = "Shared Name"; // Same name but different owners should be OK
        
        Folder folder1 = new Folder(owner1, folderName);
        Folder folder2 = new Folder(owner2, folderName);
        
        // When
        Set<ConstraintViolation<Folder>> violations1 = validator.validate(folder1);
        Set<ConstraintViolation<Folder>> violations2 = validator.validate(folder2);
        
        // Then
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertEquals(owner1, folder1.getOwnerId());
        assertEquals(owner2, folder2.getOwnerId());
        assertEquals(folderName, folder1.getName());
        assertEquals(folderName, folder2.getName());
    }

    @Test
    @DisplayName("Should handle special characters in folder name")
    void shouldHandleSpecialCharactersInFolderName() {
        // Given
        String specialName = "Folder-Name_With@Special#Characters!123";
        Folder folder = new Folder(TEST_USER_ID, specialName);
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(specialName, folder.getName());
    }

    @Test
    @DisplayName("Should handle unicode characters in folder name")
    void shouldHandleUnicodeCharactersInFolderName() {
        // Given
        String unicodeName = "Cartella 📁 Progetti 🚀 العمل 中文";
        Folder folder = new Folder(TEST_USER_ID, unicodeName);
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(unicodeName, folder.getName());
    }

    @Test
    @DisplayName("Should handle constructor with parameters")
    void shouldHandleConstructorWithParameters() {
        // Given
        Long ownerId = 123L;
        String name = "My Test Folder";
        
        // When
        Folder folder = new Folder(ownerId, name);
        
        // Then
        assertEquals(ownerId, folder.getOwnerId());
        assertEquals(name, folder.getName());
        assertNull(folder.getId());
        assertNull(folder.getCreatedAt());
    }

    @Test
    @DisplayName("Should equal folders with same property values")
    void shouldCompareFoldersWithSamePropertyValues() {
        // Given
        Folder folder1 = new Folder(TEST_USER_ID, VALID_FOLDER_NAME);
        Folder folder2 = new Folder(TEST_USER_ID, VALID_FOLDER_NAME);
        
        // When & Then
        // Note: Since Folder doesn't override equals/hashCode, this tests object identity
        // In a real scenario, you might want to implement equals/hashCode based on owner+name
        assertNotEquals(folder1, folder2); // Different objects
        
        // But they should have the same property values
        assertEquals(folder1.getOwnerId(), folder2.getOwnerId());
        assertEquals(folder1.getName(), folder2.getName());
    }

    @Test
    @DisplayName("Should handle edge case with minimum valid data")
    void shouldHandleEdgeCaseWithMinimumValidData() {
        // Given - minimum valid data
        Folder folder = new Folder(1L, "A"); // Shortest possible valid name
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty());
        assertEquals(1L, folder.getOwnerId());
        assertEquals("A", folder.getName());
    }

    @Test
    @DisplayName("Should demonstrate JPA constraints vs Bean Validation")
    void shouldDemonstrateJpaConstraintsVsBeanValidation() {
        // Given - This test documents the difference between JPA and Bean Validation
        Folder folder = new Folder();
        // Setting values that would violate JPA constraints but not Bean Validation
        folder.setOwnerId(null); // JPA: nullable=false would fail at persistence
        folder.setName(null);    // JPA: nullable=false would fail at persistence
        
        // When
        Set<ConstraintViolation<Folder>> violations = validator.validate(folder);
        
        // Then
        assertTrue(violations.isEmpty()); // Bean Validation passes
        
        // This demonstrates that:
        // 1. JPA constraints (@Column(nullable=false)) are enforced at persistence time
        // 2. Bean Validation constraints (@NotNull, @NotBlank, etc.) are enforced at validation time
        // 3. This entity relies on JPA constraints and service-level validation
        assertNull(folder.getOwnerId());
        assertNull(folder.getName());
    }

    @Test
    @DisplayName("Should handle extreme values")
    void shouldHandleExtremeValues() {
        // Given
        Long maxOwnerId = Long.MAX_VALUE;
        Long minOwnerId = Long.MIN_VALUE;
        String emptyName = "";
        String veryLongName = "x".repeat(1000);
        
        // When
        Folder folder1 = new Folder(maxOwnerId, VALID_FOLDER_NAME);
        Folder folder2 = new Folder(minOwnerId, VALID_FOLDER_NAME);
        Folder folder3 = new Folder(TEST_USER_ID, emptyName);
        Folder folder4 = new Folder(TEST_USER_ID, veryLongName);
        
        // Then - All should be valid at Bean Validation level
        assertTrue(validator.validate(folder1).isEmpty());
        assertTrue(validator.validate(folder2).isEmpty());
        assertTrue(validator.validate(folder3).isEmpty());
        assertTrue(validator.validate(folder4).isEmpty());
        
        assertEquals(maxOwnerId, folder1.getOwnerId());
        assertEquals(minOwnerId, folder2.getOwnerId());
        assertEquals(emptyName, folder3.getName());
        assertEquals(veryLongName, folder4.getName());
    }
}
