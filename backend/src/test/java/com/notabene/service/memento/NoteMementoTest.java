package com.notabene.service.memento;

import com.notabene.entity.Note;
import com.notabene.model.Tag;
import com.notabene.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Memento Pattern Tests")
class NoteMementoTest {

    private User testUser;
    private Note originalNote;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        originalNote = new Note("Original Title", "Original Content", testUser);
        originalNote.setId(1L);
        
        // Clear the auto-added creator and set specific permissions
        originalNote.getReaders().clear();
        originalNote.getWriters().clear();
        originalNote.getReaders().addAll(Arrays.asList(1L, 2L, 3L));
        originalNote.getWriters().addAll(Arrays.asList(1L, 2L));
        
        // Add some tags
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("important");
        
        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("work");
        
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);
        originalNote.setTags(tags);
        
        fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    }

    @Test
    @DisplayName("Should create memento with complete note state")
    void shouldCreateMementoWithCompleteNoteState() {
        // When
        NoteMemento memento = originalNote.createMemento();
        
        // Then
        assertNotNull(memento);
        assertEquals("Original Title", memento.getTitle());
        assertEquals("Original Content", memento.getContent());
        assertEquals(1L, memento.getCreatorId());
        assertEquals(Arrays.asList(1L, 2L, 3L), memento.getReaders());
        assertEquals(Arrays.asList(1L, 2L), memento.getWriters());
        assertEquals(2, memento.getTags().size());
        assertNotNull(memento.getSnapshotTime());
    }

    @Test
    @DisplayName("Should restore note from memento")
    void shouldRestoreNoteFromMemento() {
        // Given
        NoteMemento memento = originalNote.createMemento();
        
        // Modify the original note
        originalNote.setTitle("Modified Title");
        originalNote.setContent("Modified Content");
        originalNote.getReaders().clear();
        originalNote.getWriters().clear();
        originalNote.getTags().clear();
        
        // When
        originalNote.restoreFromMemento(memento);
        
        // Then
        assertEquals("Original Title", originalNote.getTitle());
        assertEquals("Original Content", originalNote.getContent());
        assertEquals(1L, originalNote.getCreatorId());
        assertEquals(Arrays.asList(1L, 2L, 3L), originalNote.getReaders());
        assertEquals(Arrays.asList(1L, 2L), originalNote.getWriters());
        assertEquals(2, originalNote.getTags().size());
    }

    @Test
    @DisplayName("Should create deep copy in memento")
    void shouldCreateDeepCopyInMemento() {
        // Given
        NoteMemento memento = originalNote.createMemento();
        
        // When - modify original note's collections
        originalNote.getReaders().add(999L);
        originalNote.getWriters().add(888L);
        originalNote.getTags().clear();
        
        // Then - memento should be unchanged
        assertEquals(Arrays.asList(1L, 2L, 3L), memento.getReaders());
        assertEquals(Arrays.asList(1L, 2L), memento.getWriters());
        assertEquals(2, memento.getTags().size());
    }

    @Test
    @DisplayName("Should handle empty collections in memento")
    void shouldHandleEmptyCollectionsInMemento() {
        // Given
        Note emptyNote = new Note("Empty Note", "Empty Content", testUser);
        emptyNote.getReaders().clear();
        emptyNote.getWriters().clear();
        emptyNote.getTags().clear();
        
        // When
        NoteMemento memento = emptyNote.createMemento();
        
        // Then
        assertNotNull(memento.getReaders());
        assertNotNull(memento.getWriters());
        assertNotNull(memento.getTags());
        assertTrue(memento.getReaders().isEmpty());
        assertTrue(memento.getWriters().isEmpty());
        assertTrue(memento.getTags().isEmpty());
    }

    @Test
    @DisplayName("Should preserve timestamps when creating memento")
    void shouldPreserveTimestampsWhenCreatingMemento() {
        // Given
        originalNote.setCreatedAt(fixedTime);
        originalNote.setUpdatedAt(fixedTime.plusHours(1));
        
        // When
        NoteMemento memento = originalNote.createMemento();
        
        // Then
        assertEquals(fixedTime, memento.getCreatedAt());
        assertEquals(fixedTime.plusHours(1), memento.getUpdatedAt());
        assertNotNull(memento.getSnapshotTime());
    }

    @Test
    @DisplayName("Should not affect original note when restoring from memento")
    void shouldNotAffectOriginalNoteWhenRestoringFromMemento() {
        // Given
        NoteMemento memento = originalNote.createMemento();
        Note anotherNote = new Note("Another Title", "Another Content", testUser);
        anotherNote.setId(2L);
        
        // When
        anotherNote.restoreFromMemento(memento);
        
        // Then
        // Original note should be unchanged
        assertEquals("Original Title", originalNote.getTitle());
        assertEquals("Original Content", originalNote.getContent());
        
        // Another note should have restored state (except ID which should remain)
        assertEquals("Original Title", anotherNote.getTitle());
        assertEquals("Original Content", anotherNote.getContent());
        assertEquals(2L, anotherNote.getId()); // ID should not change
    }

    @Test
    @DisplayName("Should handle null values in memento gracefully")
    void shouldHandleNullValuesInMementoGracefully() {
        // Given
        Note noteWithNulls = new Note();
        noteWithNulls.setTitle("Title");
        noteWithNulls.setContent("Content");
        noteWithNulls.setUser(testUser);
        noteWithNulls.setCreatorId(null);
        
        // When
        NoteMemento memento = noteWithNulls.createMemento();
        
        // Then
        assertNotNull(memento);
        assertEquals("Title", memento.getTitle());
        assertEquals("Content", memento.getContent());
        assertNull(memento.getCreatorId());
    }

    @Test
    @DisplayName("Should create memento that can be serialized")
    void shouldCreateMementoThatCanBeSerialized() {
        // When
        NoteMemento memento = originalNote.createMemento();
        
        // Then - verify all properties are accessible (important for serialization)
        assertDoesNotThrow(() -> {
            memento.getTitle();
            memento.getContent();
            memento.getCreatorId();
            memento.getReaders();
            memento.getWriters();
            memento.getTags();
            memento.getCreatedAt();
            memento.getUpdatedAt();
            memento.getSnapshotTime();
        });
    }
}
