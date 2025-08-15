package com.notabene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.dto.NotePermissionsResponse;
import com.notabene.service.NoteService;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.exception.UnauthorizedNoteAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = {NoteController.class},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.notabene.config.TokenAuthenticationFilter.class,
            com.notabene.config.TokenStore.class
        }
    )
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.security.enabled=false",
    "management.security.enabled=false"
})
@DisplayName("Note Controller Tests")
class NoteControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private NoteService noteService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private NoteResponse sampleNoteResponse;
    private CreateNoteRequest validCreateRequest;
    private UpdateNoteRequest validUpdateRequest;
    
    @BeforeEach
    void setUp() {
        sampleNoteResponse = new NoteResponse();
        sampleNoteResponse.setId(1L);
        sampleNoteResponse.setTitle("Test Note");
        sampleNoteResponse.setContent("Test Content");
        sampleNoteResponse.setCreatedAt(LocalDateTime.now());
        sampleNoteResponse.setUpdatedAt(LocalDateTime.now());
        
        validCreateRequest = new CreateNoteRequest("Test Note", "Test Content");
        validUpdateRequest = new UpdateNoteRequest("Updated Title", "Updated Content");
    }
    
    @Test
    @DisplayName("POST /api/notes - Should create note successfully")
    void shouldCreateNoteSuccessfully() throws Exception {
        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(sampleNoteResponse);
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));
        
        verify(noteService).createNote(any(CreateNoteRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/notes - Should fail with blank title")
    void shouldFailWithBlankTitle() throws Exception {
        CreateNoteRequest invalidRequest = new CreateNoteRequest("", "Test Content");
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
        
        verify(noteService, never()).createNote(any(CreateNoteRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/notes - Should fail with blank content")
    void shouldFailWithBlankContent() throws Exception {
        CreateNoteRequest invalidRequest = new CreateNoteRequest("Test Title", "");
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
        
        verify(noteService, never()).createNote(any(CreateNoteRequest.class));
    }
    
    @Test
    @DisplayName("GET /api/notes - Should get all notes successfully")
    void shouldGetAllNotesSuccessfully() throws Exception {
        List<NoteResponse> notes = Arrays.asList(sampleNoteResponse);
        when(noteService.getAllNotes()).thenReturn(notes);
        
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Note"));
        
        verify(noteService).getAllNotes();
    }
    
    @Test
    @DisplayName("GET /api/notes with pagination - Should get paginated notes")
    void shouldGetPaginatedNotes() throws Exception {
        List<NoteResponse> notes = Arrays.asList(sampleNoteResponse);
        when(noteService.getAllNotesPaginated(0, 10)).thenReturn(notes);
        
        mockMvc.perform(get("/api/notes")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
        
        verify(noteService).getAllNotesPaginated(0, 10);
        verify(noteService, never()).getAllNotes();
    }
    
    @Test
    @DisplayName("GET /api/notes/{id} - Should get note by id successfully")
    void shouldGetNoteByIdSuccessfully() throws Exception {
        when(noteService.getNoteById(1L)).thenReturn(sampleNoteResponse);
        
        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));
        
        verify(noteService).getNoteById(1L);
    }
    
    @Test
    @DisplayName("GET /api/notes/{id} - Should return 404 when note not found")
    void shouldReturn404WhenNoteNotFound() throws Exception {
        when(noteService.getNoteById(999L)).thenThrow(new NoteNotFoundException("Note not found with id: 999"));
        
        mockMvc.perform(get("/api/notes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Note not found with id: 999"))
                .andExpect(jsonPath("$.status").value(404));
        
        verify(noteService).getNoteById(999L);
    }
    
    @Test
    @DisplayName("PUT /api/notes/{id} - Should update note successfully")
    void shouldUpdateNoteSuccessfully() throws Exception {
        NoteResponse updatedResponse = new NoteResponse();
        updatedResponse.setId(1L);
        updatedResponse.setTitle("Updated Title");
        updatedResponse.setContent("Updated Content");
        
        when(noteService.updateNote(eq(1L), any(UpdateNoteRequest.class))).thenReturn(updatedResponse);
        
        mockMvc.perform(put("/api/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
        
        verify(noteService).updateNote(eq(1L), any(UpdateNoteRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/notes/{id} - Should return 404 when updating non-existent note")
    void shouldReturn404WhenUpdatingNonExistentNote() throws Exception {
        when(noteService.updateNote(eq(999L), any(UpdateNoteRequest.class)))
                .thenThrow(new NoteNotFoundException("Note not found with id: 999"));
        
        mockMvc.perform(put("/api/notes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Note not found with id: 999"));
        
        verify(noteService).updateNote(eq(999L), any(UpdateNoteRequest.class));
    }
    
    @Test
    @DisplayName("DELETE /api/notes/{id} - Should delete note successfully")
    void shouldDeleteNoteSuccessfully() throws Exception {
        doNothing().when(noteService).deleteNote(1L);
        
        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());
        
        verify(noteService).deleteNote(1L);
    }
    
    @Test
    @DisplayName("DELETE /api/notes/{id} - Should return 404 when deleting non-existent note")
    void shouldReturn404WhenDeletingNonExistentNote() throws Exception {
        doThrow(new NoteNotFoundException("Note not found with id: 999"))
                .when(noteService).deleteNote(999L);
        
        mockMvc.perform(delete("/api/notes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Note not found with id: 999"));
        
        verify(noteService).deleteNote(999L);
    }
    
    @Test
    @DisplayName("GET /api/notes/search - Should search notes successfully")
    void shouldSearchNotesSuccessfully() throws Exception {
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotes("test")).thenReturn(searchResults);
        
        mockMvc.perform(get("/api/notes/search")
                .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Note"));
        
        verify(noteService).searchNotes("test");
    }
    
    
}
