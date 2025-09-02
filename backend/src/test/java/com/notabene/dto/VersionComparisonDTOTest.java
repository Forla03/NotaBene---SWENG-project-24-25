package com.notabene.dto;

import com.notabene.entity.NoteVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Version Comparison DTO Tests")
class VersionComparisonDTOTest {

    private NoteVersion version1;
    private NoteVersion version2;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        
        version1 = createNoteVersion(1, "Original Title", "Original Content");
        version2 = createNoteVersion(2, "Updated Title", "Updated Content");
    }

    @Test
    @DisplayName("Should detect title changes")
    void shouldDetectTitleChanges() {
        // Given
        version2.setTitle("Different Title");
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasTitleChanged());
        assertEquals("Original Title", comparison.getOldVersion().getTitle());
        assertEquals("Different Title", comparison.getNewVersion().getTitle());
    }

    @Test
    @DisplayName("Should detect content changes")
    void shouldDetectContentChanges() {
        // Given
        version2.setContent("Different Content");
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasContentChanged());
        assertEquals("Original Content", comparison.getOldVersion().getContent());
        assertEquals("Different Content", comparison.getNewVersion().getContent());
    }

    @Test
    @DisplayName("Should detect when no changes occurred")
    void shouldDetectWhenNoChangesOccurred() {
        // Given
        version2.setTitle(version1.getTitle());
        version2.setContent(version1.getContent());
        version2.setReaders(version1.getReaders());
        version2.setWriters(version1.getWriters());
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertFalse(comparison.hasTitleChanged());
        assertFalse(comparison.hasContentChanged());
        assertFalse(comparison.hasPermissionChanged());
        assertFalse(comparison.hasAnyChanges());
    }

    @Test
    @DisplayName("Should detect permission changes")
    void shouldDetectPermissionChanges() {
        // Given
        version1.setReaders(Arrays.asList(1L, 2L));
        version2.setReaders(Arrays.asList(1L, 2L, 3L)); // Added reader
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasPermissionChanged());
        assertTrue(comparison.hasAnyChanges());
    }

    @Test
    @DisplayName("Should detect writers permission changes")
    void shouldDetectWritersPermissionChanges() {
        // Given
        version1.setWriters(Arrays.asList(1L, 2L));
        version2.setWriters(Arrays.asList(1L)); // Removed writer
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasPermissionChanged());
        assertTrue(comparison.hasAnyChanges());
    }

    @Test
    @DisplayName("Should provide summary of changes")
    void shouldProvideSummaryOfChanges() {
        // Given
        version2.setTitle("New Title");
        version2.setContent("New Content");
        version2.setReaders(Arrays.asList(1L, 2L, 3L));
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasAnyChanges());
        assertTrue(comparison.hasTitleChanged());
        assertTrue(comparison.hasContentChanged());
        assertTrue(comparison.hasPermissionChanged());
        
        var summary = comparison.getChangeSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("title"));
        assertTrue(summary.contains("content"));
        assertTrue(summary.contains("permissions"));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // Given
        version1.setReaders(null);
        version2.setReaders(Arrays.asList(1L));
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasPermissionChanged());
        assertDoesNotThrow(() -> comparison.getChangeSummary());
    }

    @Test
    @DisplayName("Should compare version numbers correctly")
    void shouldCompareVersionNumbersCorrectly() {
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertEquals(1, comparison.getOldVersion().getVersionNumber());
        assertEquals(2, comparison.getNewVersion().getVersionNumber());
        assertTrue(comparison.isNewerVersion());
    }

    @Test
    @DisplayName("Should detect reverse comparison")
    void shouldDetectReverseComparison() {
        // Given - comparing newer to older
        VersionComparisonDTO comparison = new VersionComparisonDTO(version2, version1);
        
        // When & Then
        assertFalse(comparison.isNewerVersion());
        assertEquals(2, comparison.getOldVersion().getVersionNumber());
        assertEquals(1, comparison.getNewVersion().getVersionNumber());
    }

    @Test
    @DisplayName("Should calculate time difference between versions")
    void shouldCalculateTimeDifferenceBetweenVersions() {
        // Given
        version1.setCreatedAt(fixedTime);
        version2.setCreatedAt(fixedTime.plusHours(2));
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertEquals(2 * 60, comparison.getTimeDifferenceInMinutes()); // 2 hours = 120 minutes
    }

    @Test
    @DisplayName("Should provide diff statistics")
    void shouldProvideDiffStatistics() {
        // Given
        version1.setContent("Line 1\nLine 2\nLine 3");
        version2.setContent("Line 1\nModified Line 2\nLine 3\nLine 4");
        
        // When
        VersionComparisonDTO comparison = new VersionComparisonDTO(version1, version2);
        
        // Then
        assertTrue(comparison.hasContentChanged());
        var stats = comparison.getDiffStatistics();
        assertNotNull(stats);
        assertTrue(stats.getLinesAdded() > 0);
        assertTrue(stats.getLinesModified() > 0);
    }

    // Helper method
    private NoteVersion createNoteVersion(int versionNumber, String title, String content) {
        NoteVersion version = new NoteVersion();
        version.setNoteId(1L);
        version.setVersionNumber(versionNumber);
        version.setTitle(title);
        version.setContent(content);
        version.setCreatedBy(1L);
        version.setNoteCreatorId(1L);
        version.setCreatedAt(fixedTime.plusMinutes(versionNumber));
        version.setReaders(Arrays.asList(1L));
        version.setWriters(Arrays.asList(1L));
        return version;
    }
}
