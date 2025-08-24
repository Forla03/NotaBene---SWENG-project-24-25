package com.notabene.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.dto.AddPermissionRequest;
import com.notabene.dto.RemovePermissionRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private User otherUser;
    private String authToken;
    private String otherAuthToken;

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

        // Crea un secondo utente per i test di permessi
        String otherUniqueEmail = "otheruser-" + UUID.randomUUID() + "@example.com";
        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail(otherUniqueEmail);
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser = userRepository.save(otherUser);

        // Genera token di autenticazione
        authToken = "test-token-" + UUID.randomUUID();
        tokenStore.store(authToken, testUser.getUsername());
        
        otherAuthToken = "other-token-" + UUID.randomUUID();
        tokenStore.store(otherAuthToken, otherUser.getUsername());
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

    @Test
    @DisplayName("Should return permission flags correctly for owner")
    void shouldReturnPermissionFlagsForOwner() throws Exception {
        // Crea una nota di test
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        // Verifica che i flag di permesso siano corretti per il proprietario
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isOwner").value(true))
                .andExpect(jsonPath("$[0].canEdit").value(true))
                .andExpect(jsonPath("$[0].canDelete").value(true))
                .andExpect(jsonPath("$[0].canShare").value(true))
                .andDo(print());
    }

    @Test
    @DisplayName("Should return permission flags correctly for writer")
    void shouldReturnPermissionFlagsForWriter() throws Exception {
        // Crea una nota di test con l'altro utente come writer
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setWriters(new ArrayList<>(List.of(otherUser.getId())));
        note = noteRepository.save(note);

        // Verifica che i flag di permesso siano corretti per il writer
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isOwner").value(false))
                .andExpect(jsonPath("$[0].canEdit").value(true))
                .andExpect(jsonPath("$[0].canDelete").value(false))
                .andExpect(jsonPath("$[0].canShare").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("Should return permission flags correctly for reader")
    void shouldReturnPermissionFlagsForReader() throws Exception {
        // Crea una nota di test con l'altro utente come reader
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(otherUser.getId())));
        note = noteRepository.save(note);

        // Verifica che i flag di permesso siano corretti per il reader
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isOwner").value(false))
                .andExpect(jsonPath("$[0].canEdit").value(false))
                .andExpect(jsonPath("$[0].canDelete").value(false))
                .andExpect(jsonPath("$[0].canShare").value(false))
                .andDo(print());
    }

    // Permission Tests
    @Test
    @DisplayName("Should get note permissions")
    void shouldGetNotePermissions() throws Exception {
        // Crea una nota con permessi
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(otherUser.getId())));
        note.setWriters(new ArrayList<>(List.of(otherUser.getId())));
        note = noteRepository.save(note);

        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note_id").value(note.getId()))
                .andExpect(jsonPath("$.creator_id").value(testUser.getId()))
                .andExpect(jsonPath("$.readers").isArray())
                .andExpect(jsonPath("$.readers[0]").value(otherUser.getUsername()))
                .andExpect(jsonPath("$.writers").isArray())
                .andExpect(jsonPath("$.writers[0]").value(otherUser.getUsername()));
    }
    
    @Test
    @DisplayName("Should add reader permission to note")
    void shouldAddReaderPermission() throws Exception {
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        AddPermissionRequest request = new AddPermissionRequest(otherUser.getUsername());

        mockMvc.perform(post("/api/notes/" + note.getId() + "/permissions/readers")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
                
        // Verifica che il permesso sia stato aggiunto
        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readers").isArray())
                .andExpect(jsonPath("$.readers[1]").value(otherUser.getId()));
    }

    @Test
    @DisplayName("Should add writer permission to note")
    void shouldAddWriterPermission() throws Exception {
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        AddPermissionRequest request = new AddPermissionRequest(otherUser.getUsername());

        mockMvc.perform(post("/api/notes/" + note.getId() + "/permissions/writers")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
                
        // Verifica che il permesso sia stato aggiunto
        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.writers").isArray())
                .andExpect(jsonPath("$.writers[1]").value(otherUser.getId()));
    }

    @Test
    @DisplayName("Should remove reader permission from note")
    void shouldRemoveReaderPermission() throws Exception {
        // Crea una nota con permessi
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        RemovePermissionRequest request = new RemovePermissionRequest(otherUser.getUsername());

        mockMvc.perform(delete("/api/notes/" + note.getId() + "/permissions/readers")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
                
        // Verifica che il permesso sia stato rimosso
        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readers").isArray())
                .andExpect(jsonPath("$.readers.length()").value(1))
                .andExpect(jsonPath("$.readers[0]").value(testUser.getId()));
    }

    @Test
    @DisplayName("Should remove writer permission from note")
    void shouldRemoveWriterPermission() throws Exception {
        // Crea una nota con permessi
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setWriters(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        RemovePermissionRequest request = new RemovePermissionRequest(otherUser.getUsername());

        mockMvc.perform(delete("/api/notes/" + note.getId() + "/permissions/writers")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
                
        // Verifica che il permesso sia stato rimosso
        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.writers").isArray())
                .andExpect(jsonPath("$.writers.length()").value(1))
                .andExpect(jsonPath("$.writers[0]").value(testUser.getId()));
    }

    @Test
    @DisplayName("Should add reader permission using backward compatibility endpoint")
    void shouldAddReaderPermissionBackwardCompatibility() throws Exception {
        // Crea una nota
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        AddPermissionRequest request = new AddPermissionRequest(otherUser.getUsername());

        mockMvc.perform(post("/api/notes/" + note.getId() + "/readers")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
                
        // Verifica che il permesso sia stato aggiunto
        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readers").isArray())
                .andExpect(jsonPath("$.readers[1]").value(otherUser.getId()));
    }

    @Test
    @DisplayName("Should deny access to note without permission")
    void shouldDenyAccessWithoutPermission() throws Exception {
        // Crea una nota senza permessi per otherUser
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        mockMvc.perform(get("/api/notes/" + note.getId())
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should allow access to shared note for reader")
    void shouldAllowAccessToSharedNoteForReader() throws Exception {
        // Crea una nota condivisa con otherUser come reader
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        mockMvc.perform(get("/api/notes/" + note.getId())
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    @Test
    @DisplayName("Should allow write access to shared note for writer")
    void shouldAllowWriteAccessToSharedNoteForWriter() throws Exception {
        // Crea una nota condivisa con otherUser come writer
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setWriters(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        UpdateNoteRequest updateRequest = new UpdateNoteRequest("Updated Title", "Updated Content");

        mockMvc.perform(put("/api/notes/" + note.getId())
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("Should get notes shared with user")
    void shouldGetNotesSharedWithUser() throws Exception {
        // Crea due note: una propria e una condivisa
        Note ownNote = new Note();
        ownNote.setTitle("Own Note");
        ownNote.setContent("Own Content");
        ownNote.setUser(otherUser);
        ownNote.setCreatorId(otherUser.getId());
        noteRepository.save(ownNote);

        Note sharedNote = new Note();
        sharedNote.setTitle("Shared Note");
        sharedNote.setContent("Shared Content");
        sharedNote.setUser(testUser);
        sharedNote.setCreatorId(testUser.getId());
        sharedNote.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        noteRepository.save(sharedNote);

        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should fail to add permission for non-existent user")
    void shouldFailToAddPermissionForNonExistentUser() throws Exception {
        // Crea una nota
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        AddPermissionRequest request = new AddPermissionRequest("nonexistentuser");

        mockMvc.perform(post("/api/notes/" + note.getId() + "/permissions/readers")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should prevent non-owner from managing permissions")
    void shouldPreventNonOwnerFromManagingPermissions() throws Exception {
        // Crea una nota come testUser
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        AddPermissionRequest request = new AddPermissionRequest(testUser.getUsername());

        // otherUser tenta di aggiungere permessi
        mockMvc.perform(post("/api/notes/" + note.getId() + "/permissions/readers")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should deny write access to reader-only user")
    void shouldDenyWriteAccessToReaderOnlyUser() throws Exception {
        // Crea una nota condivisa solo come reader
        Note note = new Note();
        note.setTitle("Test Note");
        note.setContent("Test Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        UpdateNoteRequest updateRequest = new UpdateNoteRequest("Hacked Title", "Hacked Content");

        mockMvc.perform(put("/api/notes/" + note.getId())
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ========================= SELF-REMOVAL TESTS =========================

    @Test
    @DisplayName("Should allow user to remove himself from shared note as reader")
    void shouldAllowUserToRemoveHimselfFromSharedNoteAsReader() throws Exception {
        // Crea una nota condivisa con otherUser come reader
        Note note = new Note();
        note.setTitle("Shared Note");
        note.setContent("Shared Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        // Verifica che otherUser possa vedere la nota
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(note.getId()));

        // otherUser si rimuove dalla nota
        mockMvc.perform(delete("/api/notes/" + note.getId() + "/leave")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isOk());

        // Verifica che otherUser non veda più la nota
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Verifica che la nota sia ancora visibile al creatore
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should allow user to remove himself from shared note as writer")
    void shouldAllowUserToRemoveHimselfFromSharedNoteAsWriter() throws Exception {
        // Crea una nota condivisa con otherUser come writer
        Note note = new Note();
        note.setTitle("Collaborative Note");
        note.setContent("Editable Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note.setWriters(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        // Verifica che otherUser possa vedere e modificare la nota
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].canEdit").value(true));

        // otherUser si rimuove dalla nota
        mockMvc.perform(delete("/api/notes/" + note.getId() + "/leave")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isOk());

        // Verifica che otherUser non veda più la nota
        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should not allow note creator to remove himself from own note")
    void shouldNotAllowNoteCreatorToRemoveHimselfFromOwnNote() throws Exception {
        // Crea una nota
        Note note = new Note();
        note.setTitle("Creator Note");
        note.setContent("Creator Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        // Il creatore tenta di rimuoversi dalla propria nota
        mockMvc.perform(delete("/api/notes/" + note.getId() + "/leave")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Note creators cannot remove themselves from their own notes"));
    }

    @Test
    @DisplayName("Should not allow user to remove himself from note where he has no permissions")
    void shouldNotAllowUserToRemoveHimselfFromNoteWithoutPermissions() throws Exception {
        // Crea una nota privata (solo per testUser)
        Note note = new Note();
        note.setTitle("Private Note");
        note.setContent("Private Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note = noteRepository.save(note);

        // otherUser tenta di rimuoversi dalla nota (ma non ha permessi)
        mockMvc.perform(delete("/api/notes/" + note.getId() + "/leave")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should remove user from both readers and writers arrays when leaving note")
    void shouldRemoveUserFromBothArraysWhenLeavingNote() throws Exception {
        // Crea una nota condivisa con otherUser come reader e writer
        Note note = new Note();
        note.setTitle("Multi-permission Note");
        note.setContent("Content");
        note.setUser(testUser);
        note.setCreatorId(testUser.getId());
        note.setReaders(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note.setWriters(new ArrayList<>(List.of(testUser.getId(), otherUser.getId())));
        note = noteRepository.save(note);

        // otherUser si rimuove dalla nota
        mockMvc.perform(delete("/api/notes/" + note.getId() + "/leave")
                .header("X-Auth-Token", otherAuthToken)
                .header("Authorization", "Bearer " + otherAuthToken)
                .with(csrf()))
                .andExpect(status().isOk());

        // Verifica che otherUser sia stato rimosso da entrambi gli array
        mockMvc.perform(get("/api/notes/" + note.getId() + "/permissions")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readers", hasSize(1)))
                .andExpect(jsonPath("$.readers[0]").value(testUser.getId()))
                .andExpect(jsonPath("$.writers", hasSize(1)))
                .andExpect(jsonPath("$.writers[0]").value(testUser.getId()));
    }
}
