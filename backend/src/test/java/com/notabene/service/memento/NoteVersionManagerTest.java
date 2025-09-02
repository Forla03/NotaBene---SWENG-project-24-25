package com.notabene.service.memento;

import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.model.User;
import com.notabene.repository.NoteVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Note Version Manager Tests")
class NoteVersionManagerTest {

    @Mock
    private NoteVersionRepository noteVersionRepository;
    
    @InjectMocks
    private NoteVersionManager versionManager;
    
    private User testUser;
    private Note testNote;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testNote = new Note("Test Title", "Test Content", testUser);
        testNote.setId(1L);
        
        fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    }

    @Test
    @DisplayName("Should create version with correct version number")
    void shouldCreateVersionWithCorrectVersionNumber() {
        // Given
        when(noteVersionRepository.countByNoteId(1L)).thenReturn(2L);
        when(noteVersionRepository.save(any(NoteVersion.class))).thenAnswer(invocation -> {
            NoteVersion version = invocation.getArgument(0);
            version.setId(1L);
            version.setCreatedAt(LocalDateTime.now());
            return version;
        });
        
        // When
        NoteVersion version = versionManager.createVersion(testNote, testUser.getId());
        
        // Then
        assertNotNull(version);
        assertEquals(3, version.getVersionNumber()); // Should be count + 1
        assertEquals(1L, version.getNoteId());
        assertEquals("Test Title", version.getTitle());
        assertEquals("Test Content", version.getContent());
        assertEquals(testUser.getId(), version.getCreatedBy());
        assertNotNull(version.getCreatedAt());
    }

    @Test
    @DisplayName("Should create first version with version number 1")
    void shouldCreateFirstVersionWithVersionNumber1() {
        // Given
        when(noteVersionRepository.countByNoteId(1L)).thenReturn(0L);
        when(noteVersionRepository.save(any(NoteVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        versionManager.createVersion(testNote, testUser.getId());
        
        // Then
        ArgumentCaptor<NoteVersion> versionCaptor = ArgumentCaptor.forClass(NoteVersion.class);
        verify(noteVersionRepository).save(versionCaptor.capture());
        
        NoteVersion savedVersion = versionCaptor.getValue();
        assertEquals(1, savedVersion.getVersionNumber());
    }

    @Test
    @DisplayName("Should enforce maximum version limit")
    void shouldEnforceMaximumVersionLimit() {
        // Given
        int maxVersions = 10;
        when(noteVersionRepository.countByNoteId(1L)).thenReturn((long) maxVersions);
        
        // Create a list with exactly 10 versions (at the limit)
        List<NoteVersion> oldVersions = Arrays.asList(
            createMockVersion(1L, 1),
            createMockVersion(2L, 2),
            createMockVersion(3L, 3),
            createMockVersion(4L, 4),
            createMockVersion(5L, 5),
            createMockVersion(6L, 6),
            createMockVersion(7L, 7),
            createMockVersion(8L, 8),
            createMockVersion(9L, 9),
            createMockVersion(10L, 10)
        );
        
        when(noteVersionRepository.findByNoteIdOrderByVersionNumberAsc(1L))
            .thenReturn(oldVersions);
        when(noteVersionRepository.save(any(NoteVersion.class)))
            .thenAnswer(invocation -> {
                NoteVersion version = invocation.getArgument(0);
                version.setId(1L);
                version.setCreatedAt(LocalDateTime.now());
                return version;
            });
        
        // When
        versionManager.createVersion(testNote, testUser.getId());
        
        // Then
        verify(noteVersionRepository).deleteAll(anyList());
        verify(noteVersionRepository).save(any(NoteVersion.class));
    }

    @Test
    @DisplayName("Should preserve all note properties in version")
    void shouldPreserveAllNotePropertiesInVersion() {
        // Given
        testNote.getReaders().clear();
        testNote.getWriters().clear();
        testNote.getReaders().addAll(Arrays.asList(1L, 2L, 3L));
        testNote.getWriters().addAll(Arrays.asList(1L, 2L));
        testNote.setCreatedAt(fixedTime);
        testNote.setUpdatedAt(fixedTime.plusHours(1));
        
        when(noteVersionRepository.countByNoteId(1L)).thenReturn(0L);
        when(noteVersionRepository.save(any(NoteVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        versionManager.createVersion(testNote, testUser.getId());
        
        // Then
        ArgumentCaptor<NoteVersion> versionCaptor = ArgumentCaptor.forClass(NoteVersion.class);
        verify(noteVersionRepository).save(versionCaptor.capture());
        
        NoteVersion savedVersion = versionCaptor.getValue();
        assertEquals(Arrays.asList(1L, 2L, 3L), savedVersion.getReaders());
        assertEquals(Arrays.asList(1L, 2L), savedVersion.getWriters());
        assertEquals(fixedTime, savedVersion.getOriginalCreatedAt());
        assertEquals(fixedTime.plusHours(1), savedVersion.getOriginalUpdatedAt());
    }

    @Test
    @DisplayName("Should clean up old versions when exceeding limit")
    void shouldCleanUpOldVersionsWhenExceedingLimit() {
        // Given - We have reached the maximum (10 versions)
        when(noteVersionRepository.countByNoteId(1L)).thenReturn(10L);
        
        // Create a list with exactly 10 versions (at the limit)
        List<NoteVersion> existingVersions = Arrays.asList(
            createMockVersion(1L, 1),
            createMockVersion(2L, 2),
            createMockVersion(3L, 3),
            createMockVersion(4L, 4),
            createMockVersion(5L, 5),
            createMockVersion(6L, 6),
            createMockVersion(7L, 7),
            createMockVersion(8L, 8),
            createMockVersion(9L, 9),
            createMockVersion(10L, 10)
        );
        
        when(noteVersionRepository.findByNoteIdOrderByVersionNumberAsc(1L))
            .thenReturn(existingVersions);
        when(noteVersionRepository.save(any(NoteVersion.class)))
            .thenAnswer(invocation -> {
                NoteVersion version = invocation.getArgument(0);
                version.setId(1L);
                version.setCreatedAt(LocalDateTime.now());
                return version;
            });
        
        // When
        versionManager.createVersion(testNote, testUser.getId());
        
        // Then
        verify(noteVersionRepository).deleteAll(anyList());
        verify(noteVersionRepository).save(any(NoteVersion.class));
    }

    @Test
    @DisplayName("Should handle concurrent version creation safely")
    void shouldHandleConcurrentVersionCreationSafely() {
        // Given
        when(noteVersionRepository.countByNoteId(1L)).thenReturn(1L);
        when(noteVersionRepository.save(any(NoteVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        NoteVersion version1 = versionManager.createVersion(testNote, testUser.getId());
        NoteVersion version2 = versionManager.createVersion(testNote, testUser.getId());
        
        // Then
        assertNotNull(version1);
        assertNotNull(version2);
        verify(noteVersionRepository, times(2)).save(any(NoteVersion.class));
    }

    @Test
    @DisplayName("Should set creation metadata correctly")
    void shouldSetCreationMetadataCorrectly() {
        // Given
        Long editorUserId = 2L;
        when(noteVersionRepository.countByNoteId(1L)).thenReturn(0L);
        when(noteVersionRepository.save(any(NoteVersion.class))).thenAnswer(invocation -> {
            NoteVersion version = invocation.getArgument(0);
            version.setId(1L);
            version.setCreatedAt(LocalDateTime.now());
            return version;
        });
        
        // When
        NoteVersion result = versionManager.createVersion(testNote, editorUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(editorUserId, result.getCreatedBy());
        assertEquals(testNote.getCreatorId(), result.getNoteCreatorId());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should validate note before creating version")
    void shouldValidateNoteBeforeCreatingVersion() {
        // Given
        Note invalidNote = new Note();
        invalidNote.setId(null); // Invalid note
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            versionManager.createVersion(invalidNote, testUser.getId());
        });
        
        verify(noteVersionRepository, never()).save(any(NoteVersion.class));
    }

    @Test
    @DisplayName("Should not create version for null user ID")
    void shouldNotCreateVersionForNullUserId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            versionManager.createVersion(testNote, null);
        });
        
        verify(noteVersionRepository, never()).save(any(NoteVersion.class));
    }

    // Helper methods
    private NoteVersion createMockVersion(Long id, Integer versionNumber) {
        NoteVersion version = new NoteVersion();
        version.setId(id);
        version.setNoteId(testNote.getId());
        version.setVersionNumber(versionNumber);
        version.setTitle("Version " + versionNumber);
        version.setContent("Content " + versionNumber);
        version.setCreatedBy(testUser.getId());
        version.setCreatedAt(fixedTime.plusMinutes(versionNumber));
        return version;
    }
}
