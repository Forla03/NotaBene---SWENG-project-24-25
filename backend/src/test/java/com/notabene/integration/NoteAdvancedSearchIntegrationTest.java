package com.notabene.integration;

import com.notabene.entity.Folder;
import com.notabene.entity.FolderNote;
import com.notabene.entity.Note;
import com.notabene.model.FolderNoteId;
import com.notabene.model.Tag;
import com.notabene.model.User;
import com.notabene.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Note Advanced Search Integration Tests")
class NoteAdvancedSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderNoteRepository folderNoteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clear repositories
        noteRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();
        folderRepository.deleteAll();
        folderNoteRepository.deleteAll();

        // Create test user - check if already exists first
        Optional<User> existingUser = userRepository.findByEmail("test@example.com");
        if (existingUser.isPresent()) {
            testUser = existingUser.get();
        } else {
            testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword(passwordEncoder.encode("password123"));
            testUser = userRepository.save(testUser);
        }

        // For authentication in integration tests, we'll need to set up a proper token
        // This is a simplified approach - in real scenarios you'd authenticate properly
        authToken = "test-token-" + testUser.getId();
    }

    @Test
    @DisplayName("Should perform advanced search with query")
    void shouldPerformAdvancedSearchWithQuery() throws Exception {
        // Create test notes with tags
        Tag javaTag = new Tag();
        javaTag.setName("Java");
        javaTag.setCreatedBy(testUser.getId());
        tagRepository.save(javaTag);

        Tag tutorialTag = new Tag();
        tutorialTag.setName("Tutorial");
        tutorialTag.setCreatedBy(testUser.getId());
        tagRepository.save(tutorialTag);

        Note note1 = new Note();
        note1.setTitle("Java Programming Tutorial");
        note1.setContent("Complete guide to Java programming");
        note1.setUser(testUser);
        note1.getTags().add(javaTag);
        note1.getTags().add(tutorialTag);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Python Basics");
        note2.setContent("Introduction to Python");
        note2.setUser(testUser);
        noteRepository.save(note2);

        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Java Programming Tutorial"));
    }

    @Test
    @DisplayName("Should perform advanced search by tag")
    void shouldPerformAdvancedSearchByTag() throws Exception {
        // Create test tags
        Tag javaTag = new Tag();
        javaTag.setName("Java");
        javaTag.setCreatedBy(testUser.getId());
        tagRepository.save(javaTag);

        Tag pythonTag = new Tag();
        pythonTag.setName("Python");
        pythonTag.setCreatedBy(testUser.getId());
        tagRepository.save(pythonTag);

        // Create notes with different tags
        Note note1 = new Note();
        note1.setTitle("Java Tutorial");
        note1.setContent("Java programming guide");
        note1.setUser(testUser);
        note1.getTags().add(javaTag);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Python Tutorial");
        note2.setContent("Python programming guide");
        note2.setUser(testUser);
        note2.getTags().add(pythonTag);
        noteRepository.save(note2);

        mockMvc.perform(get("/api/notes/search/advanced")
                .param("tags", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Java Tutorial"));
    }

    @Test
    @DisplayName("Should perform advanced search by author")
    void shouldPerformAdvancedSearchByAuthor() throws Exception {
        // Create another user
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(otherUser);

        // Create notes by different users
        Note note1 = new Note();
        note1.setTitle("My Note");
        note1.setContent("Content by test user");
        note1.setUser(testUser);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Shared Note");
        note2.setContent("Content by other user");
        note2.setUser(otherUser);
        // Add testUser as reader so they can see this note
        note2.addReader(testUser.getId());
        noteRepository.save(note2);

        mockMvc.perform(get("/api/notes/search/advanced")
                .param("author", "otheruser")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Shared Note"));
    }

    @Test
    @DisplayName("Should perform advanced search by date range")
    void shouldPerformAdvancedSearchByDateRange() throws Exception {
        // Create notes at different times
        Note oldNote = new Note();
        oldNote.setTitle("Old Note");
        oldNote.setContent("Old content");
        oldNote.setUser(testUser);
        oldNote.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        noteRepository.save(oldNote);

        Note newNote = new Note();
        newNote.setTitle("New Note");
        newNote.setContent("New content");
        newNote.setUser(testUser);
        newNote.setCreatedAt(LocalDateTime.of(2025, 1, 15, 10, 0));
        noteRepository.save(newNote);

        mockMvc.perform(get("/api/notes/search/advanced")
                .param("createdAfter", "2025-01-01T00:00:00")
                .param("createdBefore", "2025-01-31T23:59:59")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("New Note"));
    }

    @Test
    @DisplayName("Should search notes within specific folder")
    void shouldSearchNotesWithinSpecificFolder() throws Exception {
        // Create a folder
        Folder folder = new Folder(testUser.getId(), "Programming");
        folderRepository.save(folder);

        // Create notes
        Note note1 = new Note();
        note1.setTitle("Java in Folder");
        note1.setContent("Java content");
        note1.setUser(testUser);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Java Outside Folder");
        note2.setContent("Java content");
        note2.setUser(testUser);
        noteRepository.save(note2);

        // Add note1 to folder
        FolderNote folderNote = new FolderNote(new FolderNoteId(folder.getId(), note1.getId()));
        folderNoteRepository.save(folderNote);

        mockMvc.perform(get("/api/folders/" + folder.getId() + "/notes/search")
                .param("query", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java in Folder"));
    }

    @Test
    @DisplayName("Should combine multiple search criteria")
    void shouldCombineMultipleSearchCriteria() throws Exception {
        // Create tags
        Tag javaTag = new Tag();
        javaTag.setName("Java");
        javaTag.setCreatedBy(testUser.getId());
        tagRepository.save(javaTag);

        Tag tutorialTag = new Tag();
        tutorialTag.setName("Tutorial");
        tutorialTag.setCreatedBy(testUser.getId());
        tagRepository.save(tutorialTag);

        // Create notes
        Note note1 = new Note();
        note1.setTitle("Java Programming Tutorial");
        note1.setContent("Advanced Java concepts");
        note1.setUser(testUser);
        note1.getTags().add(javaTag);
        note1.getTags().add(tutorialTag);
        noteRepository.save(note1);

        Note note2 = new Note();
        note2.setTitle("Python Programming Tutorial");
        note2.setContent("Basic Python concepts");
        note2.setUser(testUser);
        note2.getTags().add(tutorialTag);
        noteRepository.save(note2);

        // Search combining query and tag filter
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Programming")
                .param("tags", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Java Programming Tutorial"));
    }

    @Test
    @DisplayName("Should respect note permissions in advanced search")
    void shouldRespectNotePermissionsInAdvancedSearch() throws Exception {
        // Create another user
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(otherUser);

        // Create a private note by other user (testUser has no access)
        Note privateNote = new Note();
        privateNote.setTitle("Private Java Note");
        privateNote.setContent("Private Java content");
        privateNote.setUser(otherUser);
        noteRepository.save(privateNote);

        // Create a shared note (testUser has read access)
        Note sharedNote = new Note();
        sharedNote.setTitle("Shared Java Note");
        sharedNote.setContent("Shared Java content");
        sharedNote.setUser(otherUser);
        sharedNote.addReader(testUser.getId());
        noteRepository.save(sharedNote);

        // testUser should only see the shared note, not the private one
        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Shared Java Note"));
    }

    @Test
    @DisplayName("Should return empty results when no notes match criteria")
    void shouldReturnEmptyResultsWhenNoMatches() throws Exception {
        // Create a note that won't match
        Note note = new Note();
        note.setTitle("Python Tutorial");
        note.setContent("Python basics");
        note.setUser(testUser);
        noteRepository.save(note);

        mockMvc.perform(get("/api/notes/search/advanced")
                .param("query", "Java")
                .header("X-Auth-Token", authToken)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
