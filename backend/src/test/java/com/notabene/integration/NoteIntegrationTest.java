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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
    @DisplayName("Should create, read, update and delete note - full CRUD integration")
    void shouldPerformFullCrudOperations() throws Exception {
        // CREATE
        CreateNoteRequest createRequest = new CreateNoteRequest("Integration Test", "Test Content", 3);
        
        String response = mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.priority").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Extract ID from response
        Long noteId = Long.valueOf(objectMapper.readTree(response).get("id").asLong());
        
        // READ by ID
        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value("Integration Test"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.priority").value(3));
        
        // READ all notes
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(noteId));
        
        // UPDATE
        UpdateNoteRequest updateRequest = new UpdateNoteRequest("Updated Title", "Updated Content", 5);
        
        mockMvc.perform(put("/api/notes/" + noteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"))
                .andExpect(jsonPath("$.priority").value(5));
        
        // Verify update persisted
        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"))
                .andExpect(jsonPath("$.priority").value(5));
        
        // DELETE
        mockMvc.perform(delete("/api/notes/" + noteId))
                .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/api/notes/" + noteId))
                .andExpect(status().isNotFound());
        
        // Verify note not in list
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @DisplayName("Should handle search functionality correctly")
    void shouldHandleSearchFunctionalityCorrectly() throws Exception {
        // Create test notes
        Note note1 = new Note("Java Programming", "Learning Spring Boot", 1);
        Note note2 = new Note("Python Tutorial", "Data Science with Python", 2);
        Note note3 = new Note("Database Design", "MySQL and PostgreSQL", 3);
        
        noteRepository.saveAll(java.util.Arrays.asList(note1, note2, note3));
        
        // Search by title content
        mockMvc.perform(get("/api/notes/search")
                .param("q", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Programming"));
        
        // Search by content
        mockMvc.perform(get("/api/notes/search")
                .param("q", "Python"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Python Tutorial"));
        
        // Search case insensitive
        mockMvc.perform(get("/api/notes/search")
                .param("q", "database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Database Design"));
        
        // Search with no results
        mockMvc.perform(get("/api/notes/search")
                .param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @DisplayName("Should filter notes by priority correctly")
    void shouldFilterNotesByPriorityCorrectly() throws Exception {
        // Create notes with different priorities
        Note lowPriority = new Note("Low Priority Task", "Not urgent", 1);
        Note mediumPriority = new Note("Medium Priority Task", "Somewhat important", 3);
        Note highPriority = new Note("High Priority Task", "Very urgent", 5);
        
        noteRepository.saveAll(java.util.Arrays.asList(lowPriority, mediumPriority, highPriority));
        
        // Get high priority notes
        mockMvc.perform(get("/api/notes/priority/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("High Priority Task"));
        
        // Get medium priority notes
        mockMvc.perform(get("/api/notes/priority/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Medium Priority Task"));
        
        // Get low priority notes
        mockMvc.perform(get("/api/notes/priority/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Low Priority Task"));
        
        // Get notes with non-existent priority
        mockMvc.perform(get("/api/notes/priority/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @DisplayName("Should handle validation errors correctly in integration")
    void shouldHandleValidationErrorsCorrectlyInIntegration() throws Exception {
        // Test multiple validation errors
        CreateNoteRequest invalidRequest = new CreateNoteRequest("", "", 10);
        
        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(3)); // title, content, priority
    }
    
    @Test
    @DisplayName("Should handle partial updates correctly in integration")
    void shouldHandlePartialUpdatesCorrectlyInIntegration() throws Exception {
        // Create a note
        Note note = new Note("Original Title", "Original Content", 1);
        Note savedNote = noteRepository.save(note);
        
        // Update only title
        UpdateNoteRequest titleUpdate = new UpdateNoteRequest("New Title", null, null);
        
        mockMvc.perform(put("/api/notes/" + savedNote.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(titleUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.content").value("Original Content"))
                .andExpect(jsonPath("$.priority").value(1));
        
        // Update only priority
        UpdateNoteRequest priorityUpdate = new UpdateNoteRequest(null, null, 4);
        
        mockMvc.perform(put("/api/notes/" + savedNote.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(priorityUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.content").value("Original Content"))
                .andExpect(jsonPath("$.priority").value(4));
    }
}
