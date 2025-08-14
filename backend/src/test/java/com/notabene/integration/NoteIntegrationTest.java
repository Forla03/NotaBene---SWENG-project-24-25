package com.notabene.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.UserRepository;
import com.notabene.config.TokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Note Integration Tests")
class NoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenStore tokenStore;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Pulisci il database
        noteRepository.deleteAll();
        userRepository.deleteAll();

        // Crea un utente di test con email unica
        String uniqueEmail = "testuser-" + UUID.randomUUID() + "@example.com";
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail(uniqueEmail);
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        // Genera token di autenticazione
        authToken = "test-token-" + UUID.randomUUID();
        tokenStore.store(authToken, testUser.getUsername());
    }

    @Test
    @DisplayName("Should create note successfully with authentication")
    void shouldCreateNoteSuccessfully() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Test Content");

        mockMvc.perform(post("/api/notes")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andDo(print()) // Stampa la risposta per debugging
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should get all notes successfully")
    void shouldGetAllNotesSuccessfully() throws Exception {
        // Crea una nota di test
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Note"));
    }

    @Test
    @DisplayName("Should get note by id successfully")
    void shouldGetNoteByIdSuccessfully() throws Exception {
        // Crea una nota di test
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note = noteRepository.save(note);

        mockMvc.perform(get("/api/notes/" + note.getId())
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note.getId()))
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    @Test
    @DisplayName("Should update note successfully")
    void shouldUpdateNoteSuccessfully() throws Exception {
        // Crea una nota di test
        Note note = new Note();
        note.setTitle("Old Title");
        note.setContent("Old Content");
        note.setUser(testUser);
        note = noteRepository.save(note);

        UpdateNoteRequest request = new UpdateNoteRequest("Updated Title", "Updated Content");

        mockMvc.perform(put("/api/notes/" + note.getId())
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));
    }

    @Test
    @DisplayName("Should delete note successfully")
    void shouldDeleteNoteSuccessfully() throws Exception {
        // Crea una nota di test
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note = noteRepository.save(note);

        mockMvc.perform(delete("/api/notes/" + note.getId())
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should search notes successfully")
    void shouldSearchNotesSuccessfully() throws Exception {
        // Crea alcune note di test
        Note note1 = new Note();
        note1.setTitle("Java Programming");
        note1.setContent("Content about Java");
        note1.setUser(testUser);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Python Guide");
        note2.setContent("Content about Python");
        note2.setUser(testUser);
        noteRepository.save(note2);

        mockMvc.perform(get("/api/notes/search")
                .param("q", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Java Programming"));
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided")
    void shouldReturn401WhenNoAuth() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Test Content");

        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when invalid token provided")
    void shouldReturn401WhenInvalidToken() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Test Content");

        mockMvc.perform(post("/api/notes")
                .header("X-Auth-Token", "invalid-token")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
