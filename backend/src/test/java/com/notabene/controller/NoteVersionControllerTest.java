package com.notabene.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.service.AuthenticationService;
import com.notabene.service.NoteService;
import com.notabene.service.NoteVersioningService;
import com.notabene.service.TextDiffService;

@WebMvcTest(
    controllers = { NoteVersionController.class },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.notabene.config.TokenAuthenticationFilter.class,
            com.notabene.config.TokenStore.class
        }
    ),
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.notabene.exception.GlobalExceptionHandler.class)
@ActiveProfiles("test")
class NoteVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteVersioningService noteVersioningService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    private NoteService noteService;

    @MockBean
    private TextDiffService textDiffService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_NOTE_ID = 1L;

    private User testUser;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername("testuser");
        when(authenticationService.getCurrentUser()).thenReturn(testUser);

        testNote = new Note();
        testNote.setId(TEST_NOTE_ID);
        testNote.setTitle("Test Note");
        testNote.setContent("Test Content");
        // controller checks read permission via repository
        when(noteRepository.findByIdWithReadPermission(TEST_NOTE_ID, TEST_USER_ID))
                .thenReturn(Optional.of(testNote));
    }

    @Test
    void shouldGetVersionHistory_emptyListOk() throws Exception {
        // Controller: GET /api/notes/{noteId}/versions -> List<NoteVersionDTO>
        // Non facciamo assunzioni sulla struttura della DTO: restituiamo lista vuota.
        when(noteVersioningService.getVersionHistoryWithUsernames(TEST_NOTE_ID))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notes/{noteId}/versions", TEST_NOTE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetSpecificVersion() throws Exception {
        NoteVersion version = createTestVersion(1L, 2, "Version 2", "Content 2");
        when(noteVersioningService.getVersion(TEST_NOTE_ID, 2)).thenReturn(Optional.of(version));

        mockMvc.perform(get("/api/notes/{noteId}/versions/{versionNumber}", TEST_NOTE_ID, 2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.versionNumber").value(2))
                .andExpect(jsonPath("$.title").value("Version 2"))
                .andExpect(jsonPath("$.content").value("Content 2"));
    }

    @Test
    void shouldReturn404WhenVersionNotFound() throws Exception {
        when(noteVersioningService.getVersion(TEST_NOTE_ID, 999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notes/{noteId}/versions/{versionNumber}", TEST_NOTE_ID, 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenUserCannotReadNote() throws Exception {
        // Nessun permesso di lettura -> repository restituisce empty
        when(noteRepository.findByIdWithReadPermission(TEST_NOTE_ID, TEST_USER_ID))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notes/{noteId}/versions/{versionNumber}", TEST_NOTE_ID, 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRestoreToVersion() throws Exception {
        // Il controller: POST /{versionNumber}/restore -> usa authSvc, service e noteService.convertToNoteResponse
        Note restored = createTestNote();
        when(noteVersioningService.restoreToVersion(TEST_NOTE_ID, 2, TEST_USER_ID)).thenReturn(restored);
        // Restituiamo un mock di DTO per evitare dipendenze sulla struttura
        com.notabene.dto.NoteResponse dto = org.mockito.Mockito.mock(com.notabene.dto.NoteResponse.class);
        when(noteService.convertToNoteResponse(restored, TEST_USER_ID)).thenReturn(dto);

        mockMvc.perform(post("/api/notes/{noteId}/versions/{versionNumber}/restore", TEST_NOTE_ID, 2))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenNoAuthenticationContext_onRestore() throws Exception {
        when(authenticationService.getCurrentUser()).thenThrow(new IllegalStateException("No authenticated user found"));

        mockMvc.perform(post("/api/notes/{noteId}/versions/{versionNumber}/restore", TEST_NOTE_ID, 2))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleSecurityExceptionWhenRestoringVersion() throws Exception {
        when(noteVersioningService.restoreToVersion(TEST_NOTE_ID, 2, TEST_USER_ID))
                .thenThrow(new SecurityException("User does not have permission to edit this note"));

        mockMvc.perform(post("/api/notes/{noteId}/versions/{versionNumber}/restore", TEST_NOTE_ID, 2))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWhenVersionNotFound_onRestore() throws Exception {
        when(noteVersioningService.restoreToVersion(TEST_NOTE_ID, 999, TEST_USER_ID))
                .thenThrow(new IllegalArgumentException("Version not found: 999"));

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

    private Note createTestNote() {
        Note note = new Note();
        note.setId(TEST_NOTE_ID);
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setCreatorId(TEST_USER_ID);
        return note;
    }
}



