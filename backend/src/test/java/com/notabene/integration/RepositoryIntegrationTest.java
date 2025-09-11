package com.notabene.integration;

import com.notabene.entity.Folder;
import com.notabene.entity.FolderNote;
import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.model.FolderNoteId;
import com.notabene.model.Tag;
import com.notabene.model.User;
import com.notabene.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for all repositories.
 * Tests the complete data layer including relationships and complex queries.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Repository Integration Tests")
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private NoteVersionRepository noteVersionRepository;
    
    @Autowired
    private FolderRepository folderRepository;
    
    @Autowired
    private FolderNoteRepository folderNoteRepository;
    
    @Autowired
    private TagRepository tagRepository;

    private User testUser1;
    private User testUser2;
    private Note testNote1;
    private Note testNote2;
    private Folder testFolder;
    private Tag testTag1;
    private Tag testTag2;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = new User();
        testUser1.setUsername("testuser1");
        testUser1.setEmail("test1@example.com");
        testUser1.setPassword("password123");
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("password456");
        testUser2 = userRepository.save(testUser2);

        // Create test folder
        testFolder = new Folder();
        testFolder.setName("Test Folder");
        testFolder.setOwnerId(testUser1.getId());
        testFolder = folderRepository.save(testFolder);

        // Create test tags
        testTag1 = new Tag();
        testTag1.setName("Java");
        testTag1.setCreatedBy(testUser1.getId());
        testTag1 = tagRepository.save(testTag1);

        testTag2 = new Tag();
        testTag2.setName("Spring");
        testTag2.setCreatedBy(testUser1.getId());
        testTag2 = tagRepository.save(testTag2);

        // Create test notes
        testNote1 = new Note("Java Tutorial", "Learn Java programming", testUser1);
        testNote1.setReaders(Arrays.asList(testUser1.getId(), testUser2.getId()));
        testNote1.setWriters(Arrays.asList(testUser1.getId()));
        testNote1 = noteRepository.save(testNote1);

        testNote2 = new Note("Spring Boot Guide", "Spring Boot basics", testUser2);
        testNote2.setReaders(Arrays.asList(testUser2.getId()));
        testNote2.setWriters(Arrays.asList(testUser2.getId()));
        testNote2 = noteRepository.save(testNote2);

        // Add note to folder
        FolderNoteId folderNoteId = new FolderNoteId(testFolder.getId(), testNote1.getId());
        FolderNote folderNote = new FolderNote(folderNoteId);
        folderNoteRepository.save(folderNote);
    }

    @Test
    @DisplayName("Should test User repository operations")
    void shouldTestUserRepositoryOperations() {
        // Test findByEmail
        Optional<User> userByEmail = userRepository.findByEmail("test1@example.com");
        assertThat(userByEmail).isPresent();
        assertThat(userByEmail.get().getUsername()).isEqualTo("testuser1");

        // Test findByUsername
        Optional<User> userByUsername = userRepository.findByUsername("testuser2");
        assertThat(userByUsername).isPresent();
        assertThat(userByUsername.get().getEmail()).isEqualTo("test2@example.com");

        // Test existsByEmail
        assertThat(userRepository.existsByEmail("test1@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();

        // Test findUsernamesByIds
        List<Long> userIds = Arrays.asList(testUser1.getId(), testUser2.getId());
        List<Object[]> usernames = userRepository.findUsernamesByIds(userIds);
        assertThat(usernames).hasSize(2);
    }

    @Test
    @DisplayName("Should test Note repository permission and search operations")
    void shouldTestNoteRepositoryOperations() {
        // Test permission-based queries
        List<Note> readableNotes = noteRepository.findByReadersContaining(testUser2.getId());
        assertThat(readableNotes).hasSize(2); // Both notes are readable by user2
        
        List<Note> writableNotes = noteRepository.findByWritersContaining(testUser1.getId());
        assertThat(writableNotes).hasSize(1); // Only testNote1 is writable by user1
        assertThat(writableNotes.get(0).getTitle()).isEqualTo("Java Tutorial");

        // Test search functionality
        List<Note> searchResults = noteRepository.searchNotesWithReadPermission(testUser2.getId(), "Java");
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getTitle()).isEqualTo("Java Tutorial");

        // Test content search
        List<Note> contentResults = noteRepository.searchNotesWithReadPermission(testUser2.getId(), "programming");
        assertThat(contentResults).hasSize(1);
        assertThat(contentResults.get(0).getContent()).contains("programming");

        // Test permission verification
        Optional<Note> readableNote = noteRepository.findByIdWithReadPermission(testNote1.getId(), testUser2.getId());
        assertThat(readableNote).isPresent();
        
        Optional<Note> notWritableNote = noteRepository.findByIdWithWritePermission(testNote1.getId(), testUser2.getId());
        assertThat(notWritableNote).isEmpty(); // user2 doesn't have write permission

        // Test pagination
        Page<Note> pageResults = noteRepository.findByReadersContaining(testUser2.getId(), PageRequest.of(0, 1));
        assertThat(pageResults.getContent()).hasSize(1);
        assertThat(pageResults.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should test advanced search with filters")
    void shouldTestAdvancedSearchCapabilities() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneHourLater = now.plusHours(1);

        // Test advanced search with query
        List<Note> queryResults = noteRepository.searchNotesAdvanced(
            testUser2.getId(), "Java", null, null, null, null, null, null
        );
        assertThat(queryResults).hasSize(1);
        assertThat(queryResults.get(0).getTitle()).isEqualTo("Java Tutorial");

        // Test advanced search with author
        List<Note> authorResults = noteRepository.searchNotesAdvanced(
            testUser2.getId(), null, "testuser1", null, null, null, null, null
        );
        assertThat(authorResults).hasSize(1);

        // Test advanced search with time filters
        List<Note> timeResults = noteRepository.searchNotesAdvanced(
            testUser2.getId(), null, null, oneHourAgo, oneHourLater, null, null, null
        );
        assertThat(timeResults).hasSize(2); // Both notes created within this timeframe

        // Test advanced search with folder filter
        List<Note> folderResults = noteRepository.searchNotesAdvanced(
            testUser1.getId(), null, null, null, null, null, null, testFolder.getId()
        );
        assertThat(folderResults).hasSize(1);
        assertThat(folderResults.get(0).getTitle()).isEqualTo("Java Tutorial");
    }

    @Test
    @DisplayName("Should test Folder repository operations")
    void shouldTestFolderRepositoryOperations() {
        // Test findAllByOwnerIdOrderByNameAsc
        List<Folder> userFolders = folderRepository.findAllByOwnerIdOrderByNameAsc(testUser1.getId());
        assertThat(userFolders).hasSize(1);
        assertThat(userFolders.get(0).getName()).isEqualTo("Test Folder");

        // Test findByIdAndOwnerId
        Optional<Folder> ownerFolder = folderRepository.findByIdAndOwnerId(testFolder.getId(), testUser1.getId());
        assertThat(ownerFolder).isPresent();

        Optional<Folder> nonOwnerFolder = folderRepository.findByIdAndOwnerId(testFolder.getId(), testUser2.getId());
        assertThat(nonOwnerFolder).isEmpty();

        // Test existsByIdAndOwnerId
        assertThat(folderRepository.existsByIdAndOwnerId(testFolder.getId(), testUser1.getId())).isTrue();
        assertThat(folderRepository.existsByIdAndOwnerId(testFolder.getId(), testUser2.getId())).isFalse();

        // Test existsByOwnerIdAndName
        assertThat(folderRepository.existsByOwnerIdAndName(testUser1.getId(), "Test Folder")).isTrue();
        assertThat(folderRepository.existsByOwnerIdAndName(testUser1.getId(), "Nonexistent Folder")).isFalse();

        // Create another folder with same name for different user
        Folder anotherFolder = new Folder();
        anotherFolder.setName("Test Folder");
        anotherFolder.setOwnerId(testUser2.getId());
        folderRepository.save(anotherFolder);

        // Same name should be allowed for different owners
        assertThat(folderRepository.existsByOwnerIdAndName(testUser2.getId(), "Test Folder")).isTrue();
    }

    @Test
    @DisplayName("Should test Tag repository operations and constraints")
    void shouldTestTagRepositoryOperations() {
        // Test basic tag operations
        Optional<Tag> foundTag = tagRepository.findByName("Java");
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getCreatedBy()).isEqualTo(testUser1.getId());

        // Test search functionality
        List<Tag> searchResults = tagRepository.findByNameContainingIgnoreCaseOrderByNameAsc("ja");
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getName()).isEqualTo("Java");

        // Test ordering and limits
        List<Tag> topTags = tagRepository.findTop20ByOrderByNameAsc();
        assertThat(topTags).hasSize(2);
        assertThat(topTags.get(0).getName()).isEqualTo("Java"); // Alphabetically first
        assertThat(topTags.get(1).getName()).isEqualTo("Spring");
    }

    @Test
    @DisplayName("Should test FolderNote relationship operations")
    void shouldTestFolderNoteOperations() {
        // Test that the relationship was created in setup
        FolderNoteId searchId = new FolderNoteId(testFolder.getId(), testNote1.getId());
        Optional<FolderNote> folderNote = folderNoteRepository.findById(searchId);
        assertThat(folderNote).isPresent();
        assertThat(folderNote.get().getId().getFolderId()).isEqualTo(testFolder.getId());
        assertThat(folderNote.get().getId().getNoteId()).isEqualTo(testNote1.getId());

        // Test folder-based queries
        List<FolderNote> folderNotes = folderNoteRepository.findAllById_FolderId(testFolder.getId());
        assertThat(folderNotes).hasSize(1);
        assertThat(folderNotes.get(0).getId().getNoteId()).isEqualTo(testNote1.getId());

        // Test cascade behavior - entities should remain after removing relationship
        folderNoteRepository.delete(folderNote.get());
        
        assertThat(noteRepository.findById(testNote1.getId())).isPresent();
        assertThat(folderRepository.findById(testFolder.getId())).isPresent();

        // Test adding multiple notes to same folder
        Note additionalNote = new Note("Additional Note", "More content", testUser1);
        additionalNote.setReaders(Arrays.asList(testUser1.getId()));
        additionalNote.setWriters(Arrays.asList(testUser1.getId()));
        additionalNote = noteRepository.save(additionalNote);

        FolderNoteId additionalId = new FolderNoteId(testFolder.getId(), additionalNote.getId());
        FolderNote additionalFolderNote = new FolderNote(additionalId);
        folderNoteRepository.save(additionalFolderNote);

        // Verify new relationship
        Optional<FolderNote> additionalRelation = folderNoteRepository.findById(additionalId);
        assertThat(additionalRelation).isPresent();
        
        // Verify folder now contains the new note (original was deleted above)
        List<FolderNote> allFolderNotes = folderNoteRepository.findAllById_FolderId(testFolder.getId());
        assertThat(allFolderNotes).hasSize(1);
    }

    @Test
    @DisplayName("Should test NoteVersion repository operations")
    void shouldTestNoteVersionRepositoryOperations() {
        // Create some note versions
        NoteVersion version1 = new NoteVersion();
        version1.setNoteId(testNote1.getId());
        version1.setVersionNumber(1);
        version1.setTitle("Java Tutorial v1");
        version1.setContent("First version content");
        version1.setCreatedBy(testUser1.getId());
        version1.setNoteCreatorId(testUser1.getId());
        version1.setReaders(Arrays.asList(testUser1.getId()));
        version1.setWriters(Arrays.asList(testUser1.getId()));
        version1 = noteVersionRepository.save(version1);

        NoteVersion version2 = new NoteVersion();
        version2.setNoteId(testNote1.getId());
        version2.setVersionNumber(2);
        version2.setTitle("Java Tutorial v2");
        version2.setContent("Second version content");
        version2.setCreatedBy(testUser1.getId());
        version2.setNoteCreatorId(testUser1.getId());
        version2.setReaders(Arrays.asList(testUser1.getId()));
        version2.setWriters(Arrays.asList(testUser1.getId()));
        version2 = noteVersionRepository.save(version2);

        // Test basic CRUD operations
        Optional<NoteVersion> foundVersion = noteVersionRepository.findById(version1.getId());
        assertThat(foundVersion).isPresent();
        assertThat(foundVersion.get().getTitle()).isEqualTo("Java Tutorial v1");

        List<NoteVersion> allVersions = noteVersionRepository.findAll();
        assertThat(allVersions).hasSize(2);

        // Verify version data integrity
        NoteVersion retrievedVersion = foundVersion.get();
        assertThat(retrievedVersion.getNoteId()).isEqualTo(testNote1.getId());
        assertThat(retrievedVersion.getVersionNumber()).isEqualTo(1);
        assertThat(retrievedVersion.getCreatedBy()).isEqualTo(testUser1.getId());
        assertThat(retrievedVersion.getReaders()).containsExactly(testUser1.getId());
        assertThat(retrievedVersion.getWriters()).containsExactly(testUser1.getId());
    }

    @Test
    @DisplayName("Should test complex cross-repository operations")
    void shouldTestComplexRepositoryInteractions() {
        // Test scenario: User creates a note, adds it to folder, creates versions, then searches
        
        // 1. Create a new comprehensive note
        Note complexNote = new Note("Complex Integration Test", "This is a complex note for testing", testUser1);
        complexNote.setReaders(Arrays.asList(testUser1.getId(), testUser2.getId()));
        complexNote.setWriters(Arrays.asList(testUser1.getId()));
        final Note savedComplexNote = noteRepository.save(complexNote);

        // 2. Add to folder
        FolderNoteId complexId = new FolderNoteId(testFolder.getId(), savedComplexNote.getId());
        FolderNote complexFolderNote = new FolderNote(complexId);
        folderNoteRepository.save(complexFolderNote);

        // 3. Create a version
        NoteVersion complexVersion = new NoteVersion();
        complexVersion.setNoteId(savedComplexNote.getId());
        complexVersion.setVersionNumber(1);
        complexVersion.setTitle("Complex Integration Test v1");
        complexVersion.setContent("First version of complex content");
        complexVersion.setCreatedBy(testUser1.getId());
        complexVersion.setNoteCreatorId(testUser1.getId());
        complexVersion.setReaders(Arrays.asList(testUser1.getId(), testUser2.getId()));
        complexVersion.setWriters(Arrays.asList(testUser1.getId()));
        noteVersionRepository.save(complexVersion);

        // 4. Verify all cross-repository relationships work together
        
        // Verify folder contains the note
        Optional<FolderNote> folderRelation = folderNoteRepository.findById(complexId);
        assertThat(folderRelation).isPresent();
        assertThat(folderRelation.get().getId().getNoteId()).isEqualTo(savedComplexNote.getId());

        // Verify version was created and linked correctly
        List<NoteVersion> versions = noteVersionRepository.findAll();
        boolean versionExists = versions.stream()
            .anyMatch(v -> v.getNoteId().equals(savedComplexNote.getId()) && v.getVersionNumber() == 1);
        assertThat(versionExists).isTrue();

        // Test cross-repository search: find note through folder search
        List<Note> folderSearchResults = noteRepository.searchNotesAdvanced(
            testUser1.getId(), "Integration", null, null, null, null, null, testFolder.getId()
        );
        assertThat(folderSearchResults).hasSize(1);
        assertThat(folderSearchResults.get(0).getId()).isEqualTo(savedComplexNote.getId());
    }

    @Test
    @DisplayName("Should test data integrity and consistency")
    void shouldTestDataConsistency() {
        // Test that data relationships are maintained correctly
        Long userId = testUser1.getId();
        
        // Verify user relationships exist
        List<Note> userNotes = noteRepository.findByCreatorId(userId);
        assertThat(userNotes).isNotEmpty();
        
        List<Folder> userFolders = folderRepository.findAllByOwnerIdOrderByNameAsc(userId);
        assertThat(userFolders).isNotEmpty();

        Optional<Tag> javaTag = tagRepository.findByName("Java");
        assertThat(javaTag).isPresent();
        assertThat(javaTag.get().getCreatedBy()).isEqualTo(userId);

        // Test legacy compatibility
        List<Note> legacyUserNotes = noteRepository.findByUserOrderByCreatedAtDesc(testUser1);
        assertThat(legacyUserNotes).hasSize(1);
        
        Optional<Note> legacyUserNote = noteRepository.findByIdAndUser(testNote1.getId(), testUser1);
        assertThat(legacyUserNote).isPresent();

        // Verify shared notes functionality  
        List<Note> sharedNotes = noteRepository.findSharedWithUser(testUser2.getId());
        assertThat(sharedNotes).hasSize(1); // testNote1 is shared with user2

        // Verify minimum expected counts (allowing for other tests' data)
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(noteRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(folderRepository.count()).isGreaterThanOrEqualTo(1);
        assertThat(tagRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should test repository performance and bulk operations")
    void shouldTestRepositoryPerformance() {
        // Create multiple notes for performance testing
        for (int i = 0; i < 15; i++) {
            Note note = new Note("Performance Test Note " + i, "Content " + i, testUser1);
            note.setReaders(Arrays.asList(testUser1.getId()));
            note.setWriters(Arrays.asList(testUser1.getId()));
            noteRepository.save(note);
        }

        // Test pagination efficiency
        Page<Note> firstPage = noteRepository.findByReadersContaining(testUser1.getId(), PageRequest.of(0, 5));
        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(firstPage.getTotalElements()).isEqualTo(16); // 15 new + 1 existing

        // Test bulk search performance
        List<Note> performanceResults = noteRepository.searchNotesWithReadPermission(testUser1.getId(), "Performance");
        assertThat(performanceResults).hasSize(15);

        // Test tag ordering and limits
        // Create additional tags for limit testing
        for (int i = 0; i < 25; i++) {
            Tag tag = new Tag();
            tag.setName("Tag" + String.format("%02d", i));
            tag.setCreatedBy(testUser1.getId());
            tagRepository.save(tag);
        }

        List<Tag> limitedTags = tagRepository.findTop20ByOrderByNameAsc();
        assertThat(limitedTags).hasSize(20);
        
        // Verify alphabetical ordering
        for (int i = 0; i < limitedTags.size() - 1; i++) {
            assertThat(limitedTags.get(i).getName())
                .isLessThanOrEqualTo(limitedTags.get(i + 1).getName());
        }
    }
}
