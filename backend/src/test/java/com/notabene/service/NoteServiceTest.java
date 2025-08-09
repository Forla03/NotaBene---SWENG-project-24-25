package com.notabene.service;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
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
    
    @InjectMocks
    private NoteService noteService;
    
    private Note sampleNote;
    private CreateNoteRequest validCreateRequest;
    private UpdateNoteRequest validUpdateRequest;
    
    @BeforeEach
    void setUp() {
        sampleNote = new Note("Test Note", "Test Content", 1);
        sampleNote.setId(1L);
        sampleNote.setCreatedAt(LocalDateTime.now());
        sampleNote.setUpdatedAt(LocalDateTime.now());
        
        validCreateRequest = new CreateNoteRequest("Test Note", "Test Content", 1);
        validUpdateRequest = new UpdateNoteRequest("Updated Title", "Updated Content", 2);
    }
    
    @Test
    @DisplayName("Should create note successfully")
    void shouldCreateNoteSuccessfully() {
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.createNote(validCreateRequest);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals(1, result.getPriority());
        
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should get all notes successfully")
    void shouldGetAllNotesSuccessfully() {
        List<Note> notes = Arrays.asList(sampleNote);
        when(noteRepository.findAllByOrderByCreatedAtDesc()).thenReturn(notes);
        
        List<NoteResponse> result = noteService.getAllNotes();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Note", result.get(0).getTitle());
        
        verify(noteRepository).findAllByOrderByCreatedAtDesc();
    }
    
    @Test
    @DisplayName("Should get paginated notes successfully")
    void shouldGetPaginatedNotesSuccessfully() {
        List<Note> notes = Arrays.asList(sampleNote);
        Page<Note> notePage = new PageImpl<>(notes);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(noteRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(notePage);
        
        List<NoteResponse> result = noteService.getAllNotesPaginated(0, 10);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        
        verify(noteRepository).findAllByOrderByCreatedAtDesc(pageable);
    }
    
    @Test
    @DisplayName("Should get note by id successfully")
    void shouldGetNoteByIdSuccessfully() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
        
        NoteResponse result = noteService.getNoteById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getTitle());
        assertEquals("Test Content", result.getContent());
        
        verify(noteRepository).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when note not found by id")
    void shouldThrowExceptionWhenNoteNotFoundById() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.getNoteById(999L)
        );
        
        assertEquals("Note not found with id: 999", exception.getMessage());
        verify(noteRepository).findById(999L);
    }
    
    @Test
    @DisplayName("Should update note successfully")
    void shouldUpdateNoteSuccessfully() {
        Note updatedNote = new Note("Updated Title", "Updated Content", 2);
        updatedNote.setId(1L);
        updatedNote.setCreatedAt(sampleNote.getCreatedAt());
        updatedNote.setUpdatedAt(LocalDateTime.now());
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);
        
        NoteResponse result = noteService.updateNote(1L, validUpdateRequest);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Content", result.getContent());
        assertEquals(2, result.getPriority());
        
        verify(noteRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should update note partially")
    void shouldUpdateNotePartially() {
        UpdateNoteRequest partialUpdate = new UpdateNoteRequest(null, "Updated Content Only", null);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.updateNote(1L, partialUpdate);
        
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle()); // Should remain unchanged
        assertEquals("Updated Content Only", result.getContent()); // Should be updated
        assertEquals(1, result.getPriority()); // Should remain unchanged
        
        verify(noteRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should ignore blank strings in update")
    void shouldIgnoreBlankStringsInUpdate() {
        UpdateNoteRequest updateWithBlanks = new UpdateNoteRequest("", "   ", 2);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(sampleNote));
        when(noteRepository.save(any(Note.class))).thenReturn(sampleNote);
        
        NoteResponse result = noteService.updateNote(1L, updateWithBlanks);
        
        assertNotNull(result);
        assertEquals("Test Note", result.getTitle()); // Should remain unchanged
        assertEquals("Test Content", result.getContent()); // Should remain unchanged
        assertEquals(2, result.getPriority()); // Should be updated
        
        verify(noteRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent note")
    void shouldThrowExceptionWhenUpdatingNonExistentNote() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.updateNote(999L, validUpdateRequest)
        );
        
        assertEquals("Note not found with id: 999", exception.getMessage());
        verify(noteRepository).findById(999L);
        verify(noteRepository, never()).save(any(Note.class));
    }
    
    @Test
    @DisplayName("Should delete note successfully")
    void shouldDeleteNoteSuccessfully() {
        when(noteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(noteRepository).deleteById(1L);
        
        assertDoesNotThrow(() -> noteService.deleteNote(1L));
        
        verify(noteRepository).existsById(1L);
        verify(noteRepository).deleteById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent note")
    void shouldThrowExceptionWhenDeletingNonExistentNote() {
        when(noteRepository.existsById(999L)).thenReturn(false);
        
        NoteNotFoundException exception = assertThrows(
                NoteNotFoundException.class,
                () -> noteService.deleteNote(999L)
        );
        
        assertEquals("Note not found with id: 999", exception.getMessage());
        verify(noteRepository).existsById(999L);
        verify(noteRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("Should search notes successfully")
    void shouldSearchNotesSuccessfully() {
        List<Note> searchResults = Arrays.asList(sampleNote);
        when(noteRepository.searchNotes("test")).thenReturn(searchResults);
        
        List<NoteResponse> result = noteService.searchNotes("test");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Test Note", result.get(0).getTitle());
        
        verify(noteRepository).searchNotes("test");
    }
    
    @Test
    @DisplayName("Should get notes by priority successfully")
    void shouldGetNotesByPrioritySuccessfully() {
        List<Note> priorityNotes = Arrays.asList(sampleNote);
        when(noteRepository.findByPriority(1)).thenReturn(priorityNotes);
        
        List<NoteResponse> result = noteService.getNotesByPriority(1);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPriority());
        
        verify(noteRepository).findByPriority(1);
    }
    
    @Test
    @DisplayName("Should return empty list when no notes found by priority")
    void shouldReturnEmptyListWhenNoNotesFoundByPriority() {
        when(noteRepository.findByPriority(5)).thenReturn(Arrays.asList());
        
        List<NoteResponse> result = noteService.getNotesByPriority(5);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(noteRepository).findByPriority(5);
    }
    
    @Test
    @DisplayName("Should return empty list when search finds no results")
    void shouldReturnEmptyListWhenSearchFindsNoResults() {
        when(noteRepository.searchNotes("nonexistent")).thenReturn(Arrays.asList());
        
        List<NoteResponse> result = noteService.searchNotes("nonexistent");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(noteRepository).searchNotes("nonexistent");
    }
}
