package com.notabene.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Note Integration Tests")
class NoteIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should create and retrieve note")
    void shouldCreateAndRetrieveNote() throws Exception {
        CreateNoteRequest createRequest = new CreateNoteRequest("Integration Test", "This is an integration test note");
        
        // Create note
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test"))
                .andExpect(jsonPath("$.content").value("This is an integration test note"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
    
    @Test
    @DisplayName("Should get all notes")
    void shouldGetAllNotes() throws Exception {
        // Create test data
        Note note1 = new Note("Note 1", "Content 1");
        Note note2 = new Note("Note 2", "Content 2");
        noteRepository.save(note1);
        noteRepository.save(note2);
        
        // Get all notes
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Note 2"))
                .andExpect(jsonPath("$[1].title").value("Note 1"));
    }
    
    @Test
    @DisplayName("Should update note")
    void shouldUpdateNote() throws Exception {
        // Create initial note
        Note note = new Note("Original Title", "Original Content");
        note = noteRepository.save(note);
        
        UpdateNoteRequest updateRequest = new UpdateNoteRequest("Updated Title", "Updated Content");
        
        // Update note
        mockMvc.perform(put("/api/notes/" + note.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }
    
    @Test
    @DisplayName("Should delete note")
    void shouldDeleteNote() throws Exception {
        // Create note to delete
        Note note = new Note("To Delete", "This note will be deleted");
        note = noteRepository.save(note);
        
        // Delete note
        mockMvc.perform(delete("/api/notes/" + note.getId()))
                .andExpect(status().isNoContent());
        
        // Verify note is deleted
        mockMvc.perform(get("/api/notes/" + note.getId()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent note")
    void shouldReturn404ForNonExistentNote() throws Exception {
        mockMvc.perform(get("/api/notes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Note not found with id: 999"));
    }
    
    @Test
    @DisplayName("Should handle validation errors")
    void shouldHandleValidationErrors() throws Exception {
        CreateNoteRequest invalidRequest = new CreateNoteRequest("", ""); // Empty title and content
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }
    
    @Test
    @DisplayName("Should reject note with content exceeding 280 characters")
    void shouldRejectNoteWithContentExceeding280Characters() throws Exception {
        String longContent = "a".repeat(281); // 281 characters - exceeds limit
        CreateNoteRequest invalidRequest = new CreateNoteRequest("Valid Title", longContent);
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("Should handle partial updates")
    void shouldHandlePartialUpdates() throws Exception {
        // Create initial note
        Note note = new Note("Original Title", "Original Content");
        note = noteRepository.save(note);
        
        // Update only content
        UpdateNoteRequest partialUpdate = new UpdateNoteRequest(null, "Updated Content Only");
        
        mockMvc.perform(put("/api/notes/" + note.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Title")) // Title unchanged
                .andExpect(jsonPath("$.content").value("Updated Content Only"));
    }
    
    @Test
    @DisplayName("Should handle concurrent operations")
    void shouldHandleConcurrentOperations() throws Exception {
        // Create multiple notes
        CreateNoteRequest request1 = new CreateNoteRequest("Concurrent Note 1", "Content 1");
        CreateNoteRequest request2 = new CreateNoteRequest("Concurrent Note 2", "Content 2");
        CreateNoteRequest request3 = new CreateNoteRequest("Concurrent Note 3", "Content 3");
        
        // Create notes concurrently (simulated)
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());
        
        // Verify all notes were created
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
    
    @Test
    @DisplayName("Should handle health check")
    void shouldHandleHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("NotaBene backend is running"));
    }
    
    @Test
    @DisplayName("Should search notes")
    void shouldSearchNotes() throws Exception {
        // Create test notes
        Note note1 = new Note("Java Programming", "Learning Java basics");
        Note note2 = new Note("Spring Boot", "Creating REST APIs");
        Note note3 = new Note("Database Design", "SQL fundamentals");
        
        noteRepository.save(note1);
        noteRepository.save(note2);
        noteRepository.save(note3);
        
        // Search for notes containing "Java"
        mockMvc.perform(get("/api/notes/search?q=Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Programming"));
    }
}
