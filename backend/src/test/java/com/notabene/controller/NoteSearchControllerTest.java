package com.notabene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.SearchNotesRequest;
import com.notabene.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@DisplayName("Note Search Controller Tests")
class NoteSearchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private NoteService noteService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private NoteResponse sampleNoteResponse;
    
    @BeforeEach
    void setUp() {
        sampleNoteResponse = new NoteResponse();
        sampleNoteResponse.setId(1L);
        sampleNoteResponse.setTitle("Java Programming");
        sampleNoteResponse.setContent("Learn Java basics");
        sampleNoteResponse.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        sampleNoteResponse.setUpdatedAt(LocalDateTime.of(2025, 1, 5, 10, 0));
        sampleNoteResponse.setCreatorId(1L);
    }
    
    @Test
    @DisplayName("GET /api/notes/search/advanced - Should search notes with simple query")
    void shouldSearchNotesWithSimpleQuery() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesAdvanced(any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Programming"));
    }
    
    @Test
    @DisplayName("GET /api/notes/search/advanced - Should search notes with multiple parameters")
    void shouldSearchNotesWithMultipleParameters() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesAdvanced(any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Java")
                .param("tags", "Programming,Tutorial")
                .param("author", "testuser")
                .param("createdAfter", "2025-01-01T00:00:00")
                .param("createdBefore", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    @DisplayName("GET /api/notes/search/advanced - Should search notes in specific folder")
    void shouldSearchNotesInFolder() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesAdvanced(any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Java")
                .param("folderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    @DisplayName("POST /api/notes/search/advanced - Should search notes with POST request body")
    void shouldSearchNotesWithPostRequest() throws Exception {
        // Given
        SearchNotesRequest request = new SearchNotesRequest();
        request.setQuery("Java");
        request.setTags(Arrays.asList("Programming"));
        request.setAuthor("testuser");
        
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesAdvanced(any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(post("/api/notes/search/advanced")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Programming"));
    }
    
    @Test
    @DisplayName("GET /api/notes/search/advanced - Should return empty array when no results")
    void shouldReturnEmptyArrayWhenNoResults() throws Exception {
        // Given
        when(noteService.searchNotesAdvanced(any(SearchNotesRequest.class)))
                .thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    @DisplayName("GET /api/notes/search/advanced - Should handle date range parameters")
    void shouldHandleDateRangeParameters() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesAdvanced(any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("createdAfter", "2025-01-01T00:00:00")
                .param("createdBefore", "2025-01-31T23:59:59")
                .param("updatedAfter", "2025-01-01T00:00:00")
                .param("updatedBefore", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    @DisplayName("GET /api/notes/search/advanced - Should handle invalid date format")
    void shouldHandleInvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("createdAfter", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("GET /api/folders/{folderId}/notes/search - Should search notes within folder")
    void shouldSearchNotesWithinFolder() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(any(Long.class), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(get("/api/folders/1/notes/search")
                .param("query", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
    
    @Test
    @DisplayName("POST /api/folders/{folderId}/notes/search - Should search notes within folder with POST")
    void shouldSearchNotesWithinFolderWithPost() throws Exception {
        // Given
        SearchNotesRequest request = new SearchNotesRequest();
        request.setQuery("Java");
        
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(any(Long.class), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);
        
        // When & Then
        mockMvc.perform(post("/api/folders/1/notes/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
