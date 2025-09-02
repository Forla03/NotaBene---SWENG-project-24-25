package com.notabene.service;

import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.NoteVersionRepository;
import com.notabene.service.memento.NoteVersionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Note Versioning Service Tests")
class NoteVersioningServiceTest {

    @Mock
    private NoteRepository noteRepository;
    
    @Mock
    private NoteVersionRepository noteVersionRepository;
    
    @Mock
    private NoteVersionManager versionManager;
    
    @InjectMocks
    private NoteVersioningService noteVersioningService;
    
    private User testUser;
    private Note testNote;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testNote = new Note("Original Title", "Original Content", testUser);
        testNote.setId(1L);
        
        fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    }

    @Test
    @DisplayName("Should create version when note is edited")
    void shouldCreateVersionWhenNoteEdited() {
        // Given
        String newTitle = "Updated Title";
        String newContent = "Updated Content";
        
        // Create a mock NoteVersion that the versionManager should return
        NoteVersion mockVersion = new NoteVersion();
        mockVersion.setVersionNumber(2);
        mockVersion.setNoteId(1L);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(versionManager.createVersion(any(Note.class), any(Long.class))).thenReturn(mockVersion);
        
        // When
        noteVersioningService.updateNoteWithVersioning(1L, newTitle, newContent, testUser.getId());
        
        // Then
        verify(versionManager, times(1)).createVersion(eq(testNote), eq(testUser.getId()));
        verify(noteRepository, times(1)).save(testNote);
        assertEquals(newTitle, testNote.getTitle());
        assertEquals(newContent, testNote.getContent());
        assertNull(testNote.getCurrentVersionPointer()); // Should be null for live version
    }

    @Test
    @DisplayName("Should not create version if note content hasn't changed")
    void shouldNotCreateVersionIfContentUnchanged() {
        // Given
        String sameTitle = testNote.getTitle();
        String sameContent = testNote.getContent();
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        
        // When
        noteVersioningService.updateNoteWithVersioning(1L, sameTitle, sameContent, testUser.getId());
        
        // Then
        verify(versionManager, never()).createVersion(any(Note.class), any(Long.class));
        verify(noteRepository, never()).save(testNote);
    }

    @Test
    @DisplayName("Should retrieve all versions for a note")
    void shouldRetrieveAllVersionsForNote() {
        // Given
        NoteVersion version1 = createNoteVersion(1L, "Version 1", "Content 1");
        NoteVersion version2 = createNoteVersion(2L, "Version 2", "Content 2");
        // List should be ordered by version number descending (most recent first)
        List<NoteVersion> versions = Arrays.asList(version2, version1);
        
        when(noteVersionRepository.findByNoteIdOrderByVersionNumberDesc(1L))
            .thenReturn(versions);
        
        // When
        List<NoteVersion> result = noteVersioningService.getVersionHistory(1L);
        
        // Then
        assertEquals(2, result.size());
        assertEquals("Version 2", result.get(0).getTitle()); // Most recent first
        assertEquals("Version 1", result.get(1).getTitle());
        verify(noteVersionRepository, times(1)).findByNoteIdOrderByVersionNumberDesc(1L);
    }

    @Test
    @DisplayName("Should restore note to specific version")
    void shouldRestoreNoteToSpecificVersion() {
        // Given
        NoteVersion targetVersion = createNoteVersion(1L, "Previous Title", "Previous Content");
        targetVersion.setVersionNumber(2);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteVersionRepository.findByNoteIdAndVersionNumber(1L, 2))
            .thenReturn(Optional.of(targetVersion));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        
        // When
        noteVersioningService.restoreToVersion(1L, 2, testUser.getId());
        
        // Then
        verify(versionManager, times(1)).createVersion(eq(testNote), eq(testUser.getId()));
        verify(noteRepository, times(1)).save(testNote);
        assertEquals("Previous Title", testNote.getTitle());
        assertEquals("Previous Content", testNote.getContent());
    }

    @Test
    @DisplayName("Should throw exception when restoring to non-existent version")
    void shouldThrowExceptionWhenRestoringToNonExistentVersion() {
        // Given
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteVersionRepository.findByNoteIdAndVersionNumber(1L, 999))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            noteVersioningService.restoreToVersion(1L, 999, testUser.getId());
        });
        
        verify(versionManager, never()).createVersion(any(Note.class), any(Long.class));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    @DisplayName("Should limit number of versions per note")
    void shouldLimitNumberOfVersionsPerNote() {
        // Given
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(versionManager.createVersion(any(Note.class), any(Long.class)))
            .thenReturn(createNoteVersion(1L, "New Title", "New Content"));
        
        // When
        noteVersioningService.updateNoteWithVersioning(1L, "New Title", "New Content", testUser.getId());
        
        // Then
        verify(versionManager, times(1)).createVersion(eq(testNote), eq(testUser.getId()));
        verify(noteRepository, times(1)).save(testNote);
        assertEquals("New Title", testNote.getTitle());
        assertEquals("New Content", testNote.getContent());
    }

    @Test
    @DisplayName("Should check if user has permission to create version")
    void shouldCheckUserPermissionToCreateVersion() {
        // Given
        Long unauthorizedUserId = 999L;
        testNote.setCreatorId(1L);
        testNote.getWriters().clear();
        testNote.getWriters().add(1L); // Only creator has write permission
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        
        // When & Then
        assertThrows(SecurityException.class, () -> {
            noteVersioningService.updateNoteWithVersioning(1L, "New Title", "New Content", unauthorizedUserId);
        });
        
        verify(versionManager, never()).createVersion(any(Note.class), any(Long.class));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    @DisplayName("Should allow version creation for users with write permission")
    void shouldAllowVersionCreationForUsersWithWritePermission() {
        // Given
        Long collaboratorUserId = 2L;
        testNote.getWriters().add(collaboratorUserId);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        
        // When
        noteVersioningService.updateNoteWithVersioning(1L, "New Title", "New Content", collaboratorUserId);
        
        // Then
        verify(versionManager, times(1)).createVersion(eq(testNote), eq(collaboratorUserId));
        verify(noteRepository, times(1)).save(testNote);
    }



    // Helper methods
    private NoteVersion createNoteVersion(Long versionId, String title, String content) {
        NoteVersion version = new NoteVersion();
        version.setId(versionId);
        version.setNoteId(1L);
        version.setTitle(title);
        version.setContent(content);
        version.setCreatedBy(testUser.getId());
        version.setCreatedAt(fixedTime);
        return version;
    }
}
