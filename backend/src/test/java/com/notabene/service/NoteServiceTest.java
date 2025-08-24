package com.notabene.service;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.dto.NotePermissionsResponse;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Note Service Tests")
class NoteServiceTest {
    
    @Mock
    private NoteRepository noteRepository;
    
    @Mock
    private AuthenticationService authenticationService;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private NoteService noteService;
    
    private Note sampleNote;
    private User testUser;
    private CreateNoteRequest validCreateRequest;
    private UpdateNoteRequest validUpdateRequest;
    
    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        
        // Create sample note with user and permissions
        sampleNote = new Note("Test Note", "Test Content", testUser);
        sampleNote.setId(1L);
        sampleNote.setCreatedAt(LocalDateTime.now());
        sampleNote.setUpdatedAt(LocalDateTime.now());
        // Initialize permissions for test user
        sampleNote.setCreatorId(testUser.getId());
        sampleNote.addReader(testUser.getId());
        sampleNote.addWriter(testUser.getId());
        
        validCreateRequest = new CreateNoteRequest("Test Note", "Test Content");
        validUpdateRequest = new UpdateNoteRequest("Updated Title", "Updated Content");
        
        // Mock current user for all tests (lenient to avoid unnecessary stubbing issues)
        lenient().when(authenticationService.getCurrentUser()).thenReturn(testUser);
    }
    
    @Test
    @DisplayName("Should create note successfully with current user")
    void shouldCreateNoteSuccessfully() {
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.createNote(validCreateRequest);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getTitle());
        assertEquals("Test Content", result.getContent());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should get all notes for current user only")
    void shouldGetAllNotesSuccessfully() {
        List<Note> createdNotes = Arrays.asList(sampleNote);
        List<Note> sharedNotes = Arrays.asList(); // Empty shared notes for this test
        
        when(noteRepository.findByCreatorId(testUser.getId())).thenReturn(createdNotes);
        when(noteRepository.findSharedWithUser(testUser.getId())).thenReturn(sharedNotes);
        
        List<NoteResponse> result = noteService.getAllNotes();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Note", result.get(0).getTitle());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByCreatorId(testUser.getId());
        verify(noteRepository).findSharedWithUser(testUser.getId());
    }
    
    @Test
    @DisplayName("Should get paginated notes for current user only")
    void shouldGetPaginatedNotesSuccessfully() {
        List<Note> notes = Arrays.asList(sampleNote);
        Page<Note> notePage = new PageImpl<>(notes);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(noteRepository.findByUserOrderByCreatedAtDesc(testUser, pageable)).thenReturn(notePage);
        
        List<NoteResponse> result = noteService.getAllNotesPaginated(0, 10);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByUserOrderByCreatedAtDesc(testUser, pageable);
    }
    
    @Test
    @DisplayName("Should get note by id for current user only")
    void shouldGetNoteByIdSuccessfully() {
        when(noteRepository.findByIdWithReadPermission(1L, testUser.getId())).thenReturn(Optional.of(sampleNote));
        
        NoteResponse result = noteService.getNoteById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getTitle());
        assertEquals("Test Content", result.getContent());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithReadPermission(1L, testUser.getId());
    }
    
    @Test
    @DisplayName("Should throw exception when note not found or no read permission")
    void shouldThrowExceptionWhenNoteNotFoundById() {
        when(noteRepository.findByIdWithReadPermission(999L, testUser.getId())).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(999L)
        );
        
        assertEquals("Note not found with id: 999 for current user", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithReadPermission(999L, testUser.getId());
    }
    
    @Test
    @DisplayName("Should update note for current user only")
    void shouldUpdateNoteSuccessfully() {
        Note updatedNote = new Note("Updated Title", "Updated Content", testUser);
        updatedNote.setId(1L);
        updatedNote.setCreatedAt(sampleNote.getCreatedAt());
        updatedNote.setUpdatedAt(LocalDateTime.now());
        // Set permissions for updated note
        updatedNote.setCreatorId(testUser.getId());
        updatedNote.addReader(testUser.getId());
        updatedNote.addWriter(testUser.getId());
        
        when(noteRepository.findByIdWithWritePermission(1L, testUser.getId())).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);
        
        NoteResponse result = noteService.updateNote(1L, validUpdateRequest);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithWritePermission(1L, testUser.getId());
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should update note partially for current user")
    void shouldUpdateNotePartially() {
        UpdateNoteRequest partialUpdate = new UpdateNoteRequest(null, "Updated Content Only");
        
        when(noteRepository.findByIdWithWritePermission(1L, testUser.getId())).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.updateNote(1L, partialUpdate);
        
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle()); // Should remain unchanged
        assertEquals("Updated Content Only", result.getContent()); // Should be updated
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithWritePermission(1L, testUser.getId());
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should ignore blank strings in update")
    void shouldIgnoreBlankStringsInUpdate() {
        UpdateNoteRequest updateWithBlanks = new UpdateNoteRequest("", "   ");
        
        when(noteRepository.findByIdWithWritePermission(1L, testUser.getId())).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.updateNote(1L, updateWithBlanks);
        
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle()); // Should remain unchanged
        assertEquals("Test Content", result.getContent()); // Should remain unchanged
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithWritePermission(1L, testUser.getId());
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating note without write permission")
    void shouldThrowExceptionWhenUpdatingNonExistentNote() {
        when(noteRepository.findByIdWithWritePermission(999L, testUser.getId())).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.updateNote(999L, validUpdateRequest)
        );
        
        assertEquals("Note not found with id: 999 for current user or user has no write permission", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithWritePermission(999L, testUser.getId());
        verify(noteRepository, never()).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should delete note for note owner only")
    void shouldDeleteNoteSuccessfully() {
        // Set the sample note to be owned by the test user
        sampleNote.setCreatorId(testUser.getId());
        when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
        doNothing().when(noteRepository).delete(sampleNote);
        
        assertDoesNotThrow(() -> noteService.deleteNote(1L));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(1L);
        verify(noteRepository).delete(sampleNote);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting note that doesn't exist")
    void shouldThrowExceptionWhenDeletingNonExistentNote() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.deleteNote(999L)
        );
        
        assertEquals("Note not found", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(999L);
        verify(noteRepository, never()).delete(any(Note.class));
    }
    
    @Test
    @DisplayName("Should search notes for current user only")
    void shouldSearchNotesSuccessfully() {
        List<Note> searchResults = Arrays.asList(sampleNote);
        when(noteRepository.searchNotesByUser(testUser, "test")).thenReturn(searchResults);
        
        List<NoteResponse> result = noteService.searchNotes("test");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Note", result.get(0).getTitle());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).searchNotesByUser(testUser, "test");
    }
    
    @Test
    @DisplayName("Should return empty list when search finds no results for current user")
    void shouldReturnEmptyListWhenSearchFindsNoResults() {
        when(noteRepository.searchNotesByUser(testUser, "nonexistent")).thenReturn(Arrays.asList());
        
        List<NoteResponse> result = noteService.searchNotes("nonexistent");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).searchNotesByUser(testUser, "nonexistent");
    }
    
    @Test
    @DisplayName("Should prevent access to notes from different users")
    void shouldPreventAccessToNotesFromDifferentUsers() {
        // Create another user's note
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        
        Note otherUserNote = new Note("Other User Note", "Other Content", otherUser);
        otherUserNote.setId(2L);
        
        // Try to access other user's note - should not find it
        when(noteRepository.findByIdWithReadPermission(2L, testUser.getId())).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(2L)
        );
        
        assertEquals("Note not found with id: 2 for current user", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithReadPermission(2L, testUser.getId());
    }
    
    // =========================== PERMISSION TESTS ===========================
    // These tests validate the permission functionality of the NoteService
    
    @Test
    @DisplayName("Should add reader permission successfully")
    void shouldAddReaderPermissionSuccessfully() {
        Long noteId = 1L;
        Long userId = 2L;
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.addReaderPermission(noteId, userId));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should add writer permission successfully")
    void shouldAddWriterPermissionSuccessfully() {
        Long noteId = 1L;
        Long userId = 2L;
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.addWriterPermission(noteId, userId));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should remove reader permission successfully")
    void shouldRemoveReaderPermissionSuccessfully() {
        Long noteId = 1L;
        Long userId = 2L;
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.removeReaderPermission(noteId, userId));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should remove writer permission successfully")
    void shouldRemoveWriterPermissionSuccessfully() {
        Long noteId = 1L;
        Long userId = 2L;
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.removeWriterPermission(noteId, userId));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should add reader permission by username successfully")
    void shouldAddReaderPermissionByUsernameSuccessfully() {
        Long noteId = 1L;
        String username = "otheruser";
        
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(username);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.addReaderByUsername(noteId, username));
        
        verify(userRepository).findByUsername(username);
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should add writer permission by username successfully")
    void shouldAddWriterPermissionByUsernameSuccessfully() {
        Long noteId = 1L;
        String username = "otheruser";
        
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(username);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.addWriterByUsername(noteId, username));
        
        verify(userRepository).findByUsername(username);
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should remove reader permission by username successfully")
    void shouldRemoveReaderPermissionByUsernameSuccessfully() {
        Long noteId = 1L;
        String username = "otheruser";
        
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(username);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.removeReaderByUsername(noteId, username));
        
        verify(userRepository).findByUsername(username);
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should remove writer permission by username successfully")
    void shouldRemoveWriterPermissionByUsernameSuccessfully() {
        Long noteId = 1L;
        String username = "otheruser";
        
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername(username);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        assertDoesNotThrow(() -> noteService.removeWriterByUsername(noteId, username));
        
        verify(userRepository).findByUsername(username);
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should get note permissions successfully")
    void shouldGetNotePermissionsSuccessfully() {
        Long noteId = 1L;
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        
        NotePermissionsResponse response = noteService.getNotePermissions(noteId);
        
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getCreatorId());
        assertEquals(noteId, response.getNoteId());
        assertNotNull(response.getReaders());
        assertNotNull(response.getWriters());
        // Verify that usernames are returned instead of IDs
        assertTrue(response.getReaders().contains(testUser.getUsername()));
        assertTrue(response.getWriters().contains(testUser.getUsername()));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
        verify(userRepository, atLeastOnce()).findById(testUser.getId());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found for permission by username")
    void shouldThrowExceptionWhenUserNotFoundForPermission() {
        Long noteId = 1L;
        String nonExistentUsername = "nonexistent";
        
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> noteService.addReaderByUsername(noteId, nonExistentUsername)
        );
        
        assertEquals("User not found: " + nonExistentUsername, exception.getMessage());
        verify(userRepository).findByUsername(nonExistentUsername);
    }
    
    @Test
    @DisplayName("Should throw exception when trying to remove creator's permissions")
    void shouldThrowExceptionWhenRemovingCreatorPermissions() {
        Long noteId = 1L;
        Long creatorId = testUser.getId();
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(sampleNote));
        
        IllegalArgumentException readerException = assertThrows(
                IllegalArgumentException.class,
                () -> noteService.removeReaderPermission(noteId, creatorId)
        );
        
        IllegalArgumentException writerException = assertThrows(
                IllegalArgumentException.class,
                () -> noteService.removeWriterPermission(noteId, creatorId)
        );
        
        assertEquals("Cannot remove creator's permissions", readerException.getMessage());
        assertEquals("Cannot remove creator's permissions", writerException.getMessage());
        
        verify(authenticationService, times(2)).getCurrentUser();
        verify(noteRepository, times(2)).findById(noteId);
    }
    
    @Test
    @DisplayName("Should deny access when user has no permission")
    void shouldDenyAccessWhenNoPermission() {
        Long noteId = 1L;
        
        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNotePermissions(noteId)
        );
        
        assertEquals("Note not found", exception.getMessage());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findById(noteId);
    }

    // ========================= SELF-REMOVAL TESTS =========================

    @Test
    @DisplayName("Should allow user to remove himself from shared note")
    void shouldAllowUserToRemoveHimselfFromSharedNote() {
        Long noteId = 1L;
        
        // Create a note shared with the current user
        Note sharedNote = new Note();
        sharedNote.setId(noteId);
        sharedNote.setTitle("Shared Note");
        sharedNote.setContent("Shared Content");
        sharedNote.setCreatorId(2L); // Different creator
        sharedNote.setReaders(new ArrayList<>(List.of(2L, testUser.getId())));
        sharedNote.setWriters(new ArrayList<>(List.of(2L, testUser.getId())));
        
        when(noteRepository.findByIdWithReadPermission(noteId, testUser.getId()))
                .thenReturn(Optional.of(sharedNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sharedNote);
        
        assertDoesNotThrow(() -> noteService.leaveSharedNote(noteId));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithReadPermission(noteId, testUser.getId());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should not allow note creator to remove himself from own note")
    void shouldNotAllowNoteCreatorToRemoveHimselfFromOwnNote() {
        Long noteId = 1L;
        
        when(noteRepository.findByIdWithReadPermission(noteId, testUser.getId()))
                .thenReturn(Optional.of(sampleNote));
        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> noteService.leaveSharedNote(noteId)
        );
        
        assertEquals("Note creators cannot remove themselves from their own notes", exception.getMessage());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithReadPermission(noteId, testUser.getId());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    @DisplayName("Should throw exception when user tries to leave note without permission")
    void shouldThrowExceptionWhenUserTriesToLeaveNoteWithoutPermission() {
        Long noteId = 1L;
        
        when(noteRepository.findByIdWithReadPermission(noteId, testUser.getId()))
                .thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.leaveSharedNote(noteId)
        );
        
        assertEquals("Note not found with id: " + noteId + " for current user", exception.getMessage());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdWithReadPermission(noteId, testUser.getId());
        verify(noteRepository, never()).save(any(Note.class));
    }
}
