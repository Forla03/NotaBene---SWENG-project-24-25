package com.notabene.integration;

import com.notabene.config.TestTokenConfig;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestTokenConfig.class})
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

    private Note testNote;
    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up
        noteRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        authToken = "test-token-" + testUser.getId();

        // Create test note
        testNote = new Note();
        testNote.setTitle("Original Title");
        testNote.setContent("Original Content");
        testNote.setCreatorId(testUser.getId());
        testNote.setUser(testUser);
        testNote = noteRepository.save(testNote);
    }

    @Test
    @DisplayName("Should create version when note is updated")
    void shouldCreateVersionWhenNoteIsUpdated() throws Exception {
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
        assertEquals(2, versions.size()); // Current + 1 stored version
        assertEquals("Updated Title", versions.get(0).getTitle()); // Current version first
        assertEquals("Original Title", versions.get(1).getTitle()); // Stored version
        assertEquals("Original Content", versions.get(1).getContent());
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
                .andExpect(jsonPath("$", hasSize(3))) // Current + 2 stored versions
                .andExpect(jsonPath("$[0].title").value("Version 2")) // Most recent first (current)
                .andExpect(jsonPath("$[1].title").value("Version 1")) // Previous version
                .andExpect(jsonPath("$[2].title").value("Original Title")); // Original version
    }

    @Test
    @DisplayName("Should restore note to specific version via API")
    void shouldRestoreNoteToSpecificVersionViaAPI() throws Exception {
        // Given - create a version
        noteVersioningService.updateNoteWithVersioning(
            testNote.getId(), "Modified Title", "Modified Content", testUser.getId());
        
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        var storedVersion = versions.get(1); // Get the first stored version (version 1)

        // When
        mockMvc.perform(post("/api/notes/{id}/versions/{versionNumber}/restore", 
                testNote.getId(), storedVersion.getVersionNumber())
                .header("X-Auth-Token", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Title"))
                .andExpect(jsonPath("$.content").value("Original Content"));

        // Then - verify restoration created a new version
        var updatedVersions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(3, updatedVersions.size()); // Current + original backup + restoration backup
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
                .andExpect(jsonPath("$.hasChanges").value(true))
                .andExpect(jsonPath("$.leftVersion.title").value("Original Title"))
                .andExpect(jsonPath("$.rightVersion.title").value("Version 1"));
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
        assertEquals(1, versions.size()); // Only current version, no stored versions
        assertEquals("Original Title", versions.get(0).getTitle());
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

        // Then - should not exceed maximum (10 stored + 1 current = 11 total)
        assertTrue(versions.size() <= 11);
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
                .andExpect(status().isNotFound());

        // Then
        var versions = noteVersioningService.getVersionHistory(testNote.getId());
        assertEquals(1, versions.size()); // Only current version, no additional versions created
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
        assertEquals(2, versions.size()); // Current + 1 stored version
        assertEquals(collaborator.getId(), versions.get(1).getCreatedBy()); // Check stored version
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
        assertEquals(3, versions.size()); // Current + 2 stored versions
    }
}