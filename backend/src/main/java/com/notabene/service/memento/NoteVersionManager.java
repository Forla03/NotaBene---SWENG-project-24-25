package com.notabene.service.memento;

import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.repository.NoteVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Caretaker class for the Memento pattern.
 * Manages the creation, storage, and cleanup of note versions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NoteVersionManager {
    
    private final NoteVersionRepository noteVersionRepository;
    
    private static final int MAX_VERSIONS_PER_NOTE = 10;
    
    /**
     * Create a new version of the note
     */
    public NoteVersion createVersion(Note note, Long editorUserId) {
        return createVersion(note, editorUserId, false, null);
    }
    
    /**
     * Create a new version of the note with restore information
     */
    public NoteVersion createVersion(Note note, Long editorUserId, boolean isRestored, Integer restoredFromVersion) {
        log.debug("Starting createVersion for note {} by user {} (restored: {}, from version: {})", 
                 note.getId(), editorUserId, isRestored, restoredFromVersion);
        validateInputs(note, editorUserId);
        
        // Get current version count
        Long currentVersionCount = noteVersionRepository.countByNoteId(note.getId());
        int newVersionNumber = currentVersionCount.intValue() + 1;
        log.debug("Current version count: {}, new version number: {}", currentVersionCount, newVersionNumber);
        
        // Clean up old versions if necessary
        if (currentVersionCount >= MAX_VERSIONS_PER_NOTE) {
            cleanupOldVersions(note.getId());
        }
        
        // Create memento to capture current state
        NoteMemento memento = note.createMemento();
        log.debug("Created memento with title: '{}', content: '{}'", memento.getTitle(), memento.getContent());
        
        // Create version entity
        NoteVersion version = new NoteVersion();
        version.setNoteId(note.getId());
        version.setVersionNumber(newVersionNumber);
        version.setTitle(memento.getTitle());
        version.setContent(memento.getContent());
        version.setReaders(memento.getReaders());
        version.setWriters(memento.getWriters());
        version.setCreatedBy(editorUserId);
        version.setNoteCreatorId(note.getCreatorId());
        version.setOriginalCreatedAt(memento.getCreatedAt());
        version.setOriginalUpdatedAt(memento.getUpdatedAt());
        
        // Set restore information
        version.setIsRestored(isRestored);
        version.setRestoredFromVersion(restoredFromVersion);
        
        log.debug("Created version entity with all fields set, attempting to save...");
        
        // Save version
        try {
            NoteVersion savedVersion = noteVersionRepository.save(version);
            log.info("Successfully created version {} for note {} by user {} (restored: {}, from version: {})", 
                    newVersionNumber, note.getId(), editorUserId, isRestored, restoredFromVersion);
            return savedVersion;
        } catch (Exception e) {
            log.error("Failed to save version for note {} by user {}: {}", 
                     note.getId(), editorUserId, e.getMessage(), e);
            throw new RuntimeException("Failed to save note version: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clean up old versions to maintain the maximum limit
     */
    private void cleanupOldVersions(Long noteId) {
        List<NoteVersion> oldVersions = noteVersionRepository
            .findByNoteIdOrderByVersionNumberAsc(noteId);
        
        // Calculate how many versions to delete
        int versionsToDelete = oldVersions.size() - MAX_VERSIONS_PER_NOTE + 1;
        
        if (versionsToDelete > 0) {
            List<NoteVersion> versionsToRemove = oldVersions.subList(0, versionsToDelete);
            noteVersionRepository.deleteAll(versionsToRemove);
            
            log.info("Cleaned up {} old versions for note {}", versionsToDelete, noteId);
        }
    }
    
    /**
     * Validate inputs for version creation
     */
    private void validateInputs(Note note, Long editorUserId) {
        if (note == null || note.getId() == null) {
            throw new IllegalArgumentException("Note and note ID cannot be null");
        }
        
        if (editorUserId == null) {
            throw new IllegalArgumentException("Editor user ID cannot be null");
        }
    }
}
