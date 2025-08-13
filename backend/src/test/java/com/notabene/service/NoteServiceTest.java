package com.notabene.service;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Note Service Tests")
class NoteServiceTest {
    
    @Mock
    private NoteRepository noteRepository;
    
    @Mock
    private AuthenticationService authenticationService;
    
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
        
        // Create sample note with user
        sampleNote = new Note("Test Note", "Test Content", testUser);
        sampleNote.setId(1L);
        sampleNote.setCreatedAt(LocalDateTime.now());
        sampleNote.setUpdatedAt(LocalDateTime.now());
        
        validCreateRequest = new CreateNoteRequest("Test Note", "Test Content");
        validUpdateRequest = new UpdateNoteRequest("Updated Title", "Updated Content");
        
        // Mock current user for all tests
        when(authenticationService.getCurrentUser()).thenReturn(testUser);
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
        List<Note> notes = Arrays.asList(sampleNote);
        when(noteRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(notes);
        
        List<NoteResponse> result = noteService.getAllNotes();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Note", result.get(0).getTitle());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByUserOrderByCreatedAtDesc(testUser);
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
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(sampleNote));
        
        NoteResponse result = noteService.getNoteById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getTitle());
        assertEquals("Test Content", result.getContent());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(1L, testUser);
    }
    
    @Test
    @DisplayName("Should throw exception when note not found for current user")
    void shouldThrowExceptionWhenNoteNotFoundById() {
        when(noteRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(999L)
        );
        
        assertEquals("Note not found with id: 999 for current user", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(999L, testUser);
    }
    
    @Test
    @DisplayName("Should update note for current user only")
    void shouldUpdateNoteSuccessfully() {
        Note updatedNote = new Note("Updated Title", "Updated Content", testUser);
        updatedNote.setId(1L);
        updatedNote.setCreatedAt(sampleNote.getCreatedAt());
        updatedNote.setUpdatedAt(LocalDateTime.now());
        
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);
        
        NoteResponse result = noteService.updateNote(1L, validUpdateRequest);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should update note partially for current user")
    void shouldUpdateNotePartially() {
        UpdateNoteRequest partialUpdate = new UpdateNoteRequest(null, "Updated Content Only");
        
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.updateNote(1L, partialUpdate);
        
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle()); // Should remain unchanged
        assertEquals("Updated Content Only", result.getContent()); // Should be updated
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should ignore blank strings in update")
    void shouldIgnoreBlankStringsInUpdate() {
        UpdateNoteRequest updateWithBlanks = new UpdateNoteRequest("", "   ");
        
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.updateNote(1L, updateWithBlanks);
        
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle()); // Should remain unchanged
        assertEquals("Test Content", result.getContent()); // Should remain unchanged
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating note not owned by current user")
    void shouldThrowExceptionWhenUpdatingNonExistentNote() {
        when(noteRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.updateNote(999L, validUpdateRequest)
        );
        
        assertEquals("Note not found with id: 999 for current user", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(999L, testUser);
        verify(noteRepository, never()).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should delete note for current user only")
    void shouldDeleteNoteSuccessfully() {
        when(noteRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(sampleNote));
        doNothing().when(noteRepository).delete(sampleNote);
        
        assertDoesNotThrow(() -> noteService.deleteNote(1L));
        
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(1L, testUser);
        verify(noteRepository).delete(sampleNote);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting note not owned by current user")
    void shouldThrowExceptionWhenDeletingNonExistentNote() {
        when(noteRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.deleteNote(999L)
        );
        
        assertEquals("Note not found with id: 999 for current user", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(999L, testUser);
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
        when(noteRepository.findByIdAndUser(2L, testUser)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(2L)
        );
        
        assertEquals("Note not found with id: 2 for current user", exception.getMessage());
        verify(authenticationService).getCurrentUser();
        verify(noteRepository).findByIdAndUser(2L, testUser);
    }
}
