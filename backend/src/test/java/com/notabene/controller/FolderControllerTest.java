package com.notabene.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.config.TokenAuthenticationFilter;
import com.notabene.dto.FolderDtos.CreateFolderRequest;
import com.notabene.dto.FolderDtos.FolderDetail;
import com.notabene.dto.FolderDtos.FolderNoteRef;
import com.notabene.dto.FolderDtos.FolderSummary;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.SearchNotesRequest;
import com.notabene.service.FolderService;
import com.notabene.service.NoteService;

import jakarta.annotation.Resource;

@WebMvcTest(value = FolderController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TokenAuthenticationFilter.class))
class FolderControllerTest {

    @Resource MockMvc mvc;
    @Resource ObjectMapper om;

    @MockBean FolderService service;
    @MockBean NoteService noteService;

    private NoteResponse sampleNoteResponse;

    @BeforeEach
    void setUp() {
        sampleNoteResponse = new NoteResponse();
        sampleNoteResponse.setId(1L);
        sampleNoteResponse.setTitle("Java Programming in Folder");
        sampleNoteResponse.setContent("Learn Java basics");
        sampleNoteResponse.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        sampleNoteResponse.setUpdatedAt(LocalDateTime.of(2025, 1, 5, 10, 0));
        sampleNoteResponse.setCreatorId(1L);
    }

    @Test
    @WithMockUser
    void list_returns_folders() throws Exception {
        when(service.listMyFolders()).thenReturn(List.of(
                new FolderSummary(1L,"Ideas"),
                new FolderSummary(2L,"Work")
        ));

        mvc.perform(get("/api/folders"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].name").value("Ideas"));
    }

    @Test
    @WithMockUser
    void create_returns_201() throws Exception {
        var body = new CreateFolderRequest("Ideas");
        when(service.createFolder(any())).thenReturn(new FolderSummary(10L,"Ideas"));

        mvc.perform(post("/api/folders")
             .with(csrf())
             .contentType(MediaType.APPLICATION_JSON)
             .content(om.writeValueAsString(body)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    void get_folder_includes_note_ids() throws Exception {
        when(service.getFolder(9L)).thenReturn(
            new FolderDetail(9L,"Ideas", List.of(new FolderNoteRef(77L)))
        );

        mvc.perform(get("/api/folders/9"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.notes[0].id").value(77));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/folders/{folderId}/notes/search - Should search notes in folder")
    void shouldSearchNotesInFolder() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);

        // When & Then
        mvc.perform(get("/api/folders/1/notes/search")
                .param("query", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Programming in Folder"));

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/folders/{folderId}/notes/search - Should search with multiple parameters")
    void shouldSearchNotesInFolderWithMultipleParameters() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);

        // When & Then
        mvc.perform(get("/api/folders/1/notes/search")
                .param("query", "Java")
                .param("tags", "Programming,Tutorial")
                .param("author", "testuser")
                .param("createdAfter", "2025-01-01T00:00:00")
                .param("createdBefore", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/folders/{folderId}/notes/search - Should search with POST request")
    void shouldSearchNotesInFolderWithPost() throws Exception {
        // Given
        SearchNotesRequest request = new SearchNotesRequest();
        request.setQuery("Java");
        request.setTags(Arrays.asList("Programming"));
        request.setAuthor("testuser");

        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);

        // When & Then
        mvc.perform(post("/api/folders/1/notes/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Programming in Folder"));

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/folders/{folderId}/search - Should search with fallback endpoint")
    void shouldSearchWithFallbackEndpoint() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);

        // When & Then
        mvc.perform(get("/api/folders/1/search")
                .param("query", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/folders/{folderId}/search - Should search with fallback POST endpoint")
    void shouldSearchWithFallbackPostEndpoint() throws Exception {
        // Given
        SearchNotesRequest request = new SearchNotesRequest();
        request.setQuery("Java");

        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);

        // When & Then
        mvc.perform(post("/api/folders/1/search")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/folders/{folderId}/notes/search - Should return empty array when no results")
    void shouldReturnEmptyArrayWhenNoResults() throws Exception {
        // Given
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(Arrays.asList());

        // When & Then
        mvc.perform(get("/api/folders/1/notes/search")
                .param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/folders/{folderId}/notes/search - Should handle date range parameters")
    void shouldHandleDateRangeParametersInFolderSearch() throws Exception {
        // Given
        List<NoteResponse> searchResults = Arrays.asList(sampleNoteResponse);
        when(noteService.searchNotesInFolder(eq(1L), any(SearchNotesRequest.class)))
                .thenReturn(searchResults);

        // When & Then
        mvc.perform(get("/api/folders/1/notes/search")
                .param("createdAfter", "2025-01-01T00:00:00")
                .param("createdBefore", "2025-01-31T23:59:59")
                .param("updatedAfter", "2025-01-01T00:00:00")
                .param("updatedBefore", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(noteService).searchNotesInFolder(eq(1L), any(SearchNotesRequest.class));
    }
}
