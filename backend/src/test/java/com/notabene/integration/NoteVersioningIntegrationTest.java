package com.notabene.integration;

import com.notabene.entity.Note;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.UserRepository;
import com.notabene.service.NoteVersioningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Note Versioning Integration Tests")
class NoteVersioningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteVersioningService noteVersioningService;

    private User testUser;
    private Note testNote;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // Create test note
        testNote = new Note("Original Title", "Original Content", testUser);
        testNote = noteRepository.save(testNote);

        // Mock authentication (simplified for testing)
        authToken = "test-token-" + testUser.getId();
    }

    @Test
    @DisplayName("Should create version when updating note via API")
    void shouldCreateVersionWhenUpdatingNoteViaAPI() throws Exception {
        // Given
        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Title");
        updateRequest.put("content", "Updated Content");

        // When
        mockMvc.perform(put("/api/notes/{id}", testNote.getId())
                .header("X-Auth-Token", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        // Then
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(1, versions.size());
        assertEquals("Original Title", versions.get(0).getTitle());
        assertEquals("Original Content", versions.get(0).getContent());
    }

    @Test
    @DisplayName("Should retrieve version history via API")
    void shouldRetrieveVersionHistoryViaAPI() throws Exception {
        // Given - create some versions
        noteVersioningService.updateNoteWithVersioning(
            testNote.getId(), "Version 1", "Content 1", testUser.getId());
        noteVersioningService.updateNoteWithVersioning(
            testNote.getId(), "Version 2", "Content 2", testUser.getId());

        // When
        mockMvc.perform(get("/api/notes/{id}/versions", testNote.getId())
                .header("X-Auth-Token", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Version 1")) // Most recent first
                .andExpect(jsonPath("$[1].title").value("Original Title"));
    }

    @Test
    @DisplayName("Should restore note to specific version via API")
    void shouldRestoreNoteToSpecificVersionViaAPI() throws Exception {
        // Given - create a version
        noteVersioningService.updateNoteWithVersioning(
            testNote.getId(), "Modified Title", "Modified Content", testUser.getId());
        
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        var originalVersion = versions.get(0); // Should be the original

        // When
        mockMvc.perform(post("/api/notes/{id}/restore/{versionNumber}", 
                testNote.getId(), originalVersion.getVersionNumber())
                .header("X-Auth-Token", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Title"))
                .andExpect(jsonPath("$.content").value("Original Content"));

        // Then - verify restoration created a new version
        var updatedVersions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(2, updatedVersions.size()); // Original backup + restoration backup
    }

    @Test
    @DisplayName("Should compare versions via API")
    void shouldCompareVersionsViaAPI() throws Exception {
        // Given - create versions
        noteVersioningService.updateNoteWithVersioning(
            testNote.getId(), "Version 1", "Content 1", testUser.getId());
        noteVersioningService.updateNoteWithVersioning(
            testNote.getId(), "Version 2", "Content 2", testUser.getId());

        // When
        mockMvc.perform(get("/api/notes/{id}/versions/compare", testNote.getId())
                .param("oldVersion", "1")
                .param("newVersion", "2")
                .header("X-Auth-Token", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasTitleChanged").value(true))
                .andExpect(jsonPath("$.hasContentChanged").value(true))
                .andExpect(jsonPath("$.oldVersion.title").value("Original Title"))
                .andExpect(jsonPath("$.newVersion.title").value("Version 1"));
    }

    @Test
    @DisplayName("Should not create version if content unchanged")
    void shouldNotCreateVersionIfContentUnchanged() throws Exception {
        // Given
        Map<String, String> sameContent = new HashMap<>();
        sameContent.put("title", testNote.getTitle());
        sameContent.put("content", testNote.getContent());

        // When
        mockMvc.perform(put("/api/notes/{id}", testNote.getId())
                .header("X-Auth-Token", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sameContent)))
                .andExpect(status().isOk());

        // Then
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(0, versions.size()); // No version should be created
    }

    @Test
    @DisplayName("Should enforce version limit")
    void shouldEnforceVersionLimit() throws Exception {
        // Given - create more than the maximum allowed versions
        for (int i = 1; i <= 12; i++) {
            noteVersioningService.updateNoteWithVersioning(
                testNote.getId(), "Title " + i, "Content " + i, testUser.getId());
        }

        // When
        var versions = noteVersioningService.getVersionHistory(testNote.getId());

        // Then - should not exceed maximum (typically 10)
        assertTrue(versions.size() <= 10);
    }

    @Test
    @DisplayName("Should require write permission to create versions")
    void shouldRequireWritePermissionToCreateVersions() throws Exception {
        // Given - another user without write permission
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);

        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("title", "Unauthorized Update");
        updateRequest.put("content", "Unauthorized Content");

        // When
        mockMvc.perform(put("/api/notes/{id}", testNote.getId())
                .header("X-Auth-Token", "test-token-" + otherUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // Then
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(0, versions.size());
    }

    @Test
    @DisplayName("Should allow collaborators to create versions")
    void shouldAllowCollaboratorsToCreateVersions() throws Exception {
        // Given - add another user as writer
        User collaborator = new User();
        collaborator.setUsername("collaborator");
        collaborator.setEmail("collaborator@example.com");
        collaborator.setPassword("password");
        collaborator = userRepository.save(collaborator);

        testNote.addWriter(collaborator.getId());
        noteRepository.save(testNote);

        Map<String, String> updateRequest = new HashMap<>();
        updateRequest.put("title", "Collaborator Update");
        updateRequest.put("content", "Collaborator Content");

        // When
        mockMvc.perform(put("/api/notes/{id}", testNote.getId())
                .header("X-Auth-Token", "test-token-" + collaborator.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Collaborator Update"));

        // Then
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(1, versions.size());
        assertEquals(collaborator.getId(), versions.get(0).getCreatedBy());
    }

    @Test
    @DisplayName("Should handle concurrent version creation")
    void shouldHandleConcurrentVersionCreation() throws Exception {
        // This test would require more complex setup for true concurrency testing
        // For now, we'll test sequential rapid updates
        
        Map<String, String> update1 = new HashMap<>();
        update1.put("title", "Rapid Update 1");
        update1.put("content", "Content 1");

        Map<String, String> update2 = new HashMap<>();
        update2.put("title", "Rapid Update 2");
        update2.put("content", "Content 2");

        // When - rapid sequential updates
        mockMvc.perform(put("/api/notes/{id}", testNote.getId())
                .header("X-Auth-Token", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/notes/{id}", testNote.getId())
                .header("X-Auth-Token", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isOk());

        // Then
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(2, versions.size());
    }
}
