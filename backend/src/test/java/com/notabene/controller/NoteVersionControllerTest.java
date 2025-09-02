package com.notabene.controller;

import com.notabene.entity.NoteVersion;
import com.notabene.service.NoteVersioningService;
import com.notabene.service.AuthenticationService;
import com.notabene.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteVersionController.class)
class NoteVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteVersioningService noteVersioningService;

    @MockBean
    private AuthenticationService authenticationService;

    
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_NOTE_ID = 1L;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername("testuser");
        when(authenticationService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    void shouldGetVersionHistory() throws Exception {
        // Given
        NoteVersion version1 = createTestVersion(1L, 1, "Version 1", "Content 1");
        NoteVersion version2 = createTestVersion(2L, 2, "Version 2", "Content 2");
        List<NoteVersion> versions = Arrays.asList(version2, version1); // Ordered by version desc

        when(noteVersioningService.getVersionHistory(TEST_NOTE_ID)).thenReturn(versions);

        // When & Then
        mockMvc.perform(get("/api/notes/{noteId}/versions", TEST_NOTE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].versionNumber").value(2))
                .andExpect(jsonPath("$[0].title").value("Version 2"))
                .andExpect(jsonPath("$[1].versionNumber").value(1))
                .andExpect(jsonPath("$[1].title").value("Version 1"));
    }

    @Test
    void shouldGetSpecificVersion() throws Exception {
        // Given
        NoteVersion version = createTestVersion(1L, 2, "Version 2", "Content 2");
        when(noteVersioningService.getVersion(TEST_NOTE_ID, 2)).thenReturn(Optional.of(version));

        // When & Then
        mockMvc.perform(get("/api/notes/{noteId}/versions/{versionNumber}", TEST_NOTE_ID, 2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.title").value("Version 2"))
                .andExpect(jsonPath("$.content").value("Content 2"));
    }

    @Test
    void shouldReturn404WhenVersionNotFound() throws Exception {
        // Given
        when(noteVersioningService.getVersion(TEST_NOTE_ID, 999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/notes/{noteId}/versions/{versionNumber}", TEST_NOTE_ID, 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRestoreToVersion() throws Exception {
        // Given
        when(noteVersioningService.restoreToVersion(TEST_NOTE_ID, 2, TEST_USER_ID))
                .thenReturn(createTestNote());

        // When & Then
        mockMvc.perform(post("/api/notes/{noteId}/versions/{versionNumber}/restore", TEST_NOTE_ID, 2))
                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturn401WhenNoAuthenticationContext() throws Exception {
        // Given
        when(authenticationService.getCurrentUser()).thenThrow(new IllegalStateException("No authenticated user found"));

        // When & Then
        mockMvc.perform(get("/api/notes/{noteId}/versions", TEST_NOTE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleSecurityExceptionWhenRestoringVersion() throws Exception {
        // Given
        when(noteVersioningService.restoreToVersion(TEST_NOTE_ID, 2, TEST_USER_ID))
                .thenThrow(new SecurityException("User does not have permission to edit this note"));

        // When & Then
        mockMvc.perform(post("/api/notes/{noteId}/versions/{versionNumber}/restore", TEST_NOTE_ID, 2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWhenVersionNotFound() throws Exception {
        // Given
        when(noteVersioningService.restoreToVersion(TEST_NOTE_ID, 999, TEST_USER_ID))
                .thenThrow(new IllegalArgumentException("Version not found: 999"));

        // When & Then
        mockMvc.perform(post("/api/notes/{noteId}/versions/{versionNumber}/restore", TEST_NOTE_ID, 999))
                .andExpect(status().isNotFound());
    }

    // Helper methods
    private NoteVersion createTestVersion(Long id, Integer versionNumber, String title, String content) {
        NoteVersion version = new NoteVersion();
        version.setId(id);
        version.setNoteId(TEST_NOTE_ID);
        version.setVersionNumber(versionNumber);
        version.setTitle(title);
        version.setContent(content);
        version.setCreatedBy(TEST_USER_ID);
        version.setNoteCreatorId(TEST_USER_ID);
        version.setCreatedAt(LocalDateTime.now());
        version.setOriginalCreatedAt(LocalDateTime.now().minusDays(1));
        version.setOriginalUpdatedAt(LocalDateTime.now().minusDays(1));
        return version;
    }

    private com.notabene.entity.Note createTestNote() {
        com.notabene.entity.Note note = new com.notabene.entity.Note();
        note.setId(TEST_NOTE_ID);
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setCreatorId(TEST_USER_ID);
        return note;
    }
}
