package com.notabene.service;

import com.notabene.dto.NoteVersionDTO;
import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.NoteVersionRepository;
import com.notabene.repository.UserRepository;
import com.notabene.service.memento.NoteVersionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing note versioning using the Memento pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NoteVersioningService {
    
    private final NoteRepository noteRepository;
    private final NoteVersionRepository noteVersionRepository;
    private final UserRepository userRepository;
    private final NoteVersionManager versionManager;
    
    /**
     * Update a note and create a version if content has changed
     */
    @Transactional
    public Note updateNoteWithVersioning(Long noteId, String newTitle, String newContent, Long editorUserId) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found with id: " + noteId));
        
        // Check if user has permission to edit
        if (!canUserEditNote(note, editorUserId)) {
            throw new SecurityException("User does not have permission to edit this note");
        }
        
        // Check if content has actually changed
        boolean titleChanged = !Objects.equals(note.getTitle(), newTitle);
        boolean contentChanged = !Objects.equals(note.getContent(), newContent);
        boolean hasChanged = titleChanged || contentChanged;
        
        log.info("Version check for note {}: titleChanged={}, contentChanged={}, hasChanged={}", 
                noteId, titleChanged, contentChanged, hasChanged);
        log.debug("Current title: '{}', New title: '{}'", note.getTitle(), newTitle);
        log.debug("Current content: '{}', New content: '{}'", note.getContent(), newContent);
        
        if (!hasChanged) {
            log.info("No changes detected for note {}, skipping version creation", noteId);
            return note;
        }
        
        log.info("Changes detected for note {}, creating new version", noteId);
        
        // Create version of current state (before updating)
        log.debug("About to call versionManager.createVersion for note {} by user {}", noteId, editorUserId);
        NoteVersion previousVersion = versionManager.createVersion(note, editorUserId);
        log.debug("versionManager.createVersion returned: {}", previousVersion);
        
        // Update the note
        note.setTitle(newTitle);
        note.setContent(newContent);
        note.setUpdatedAt(LocalDateTime.now());
        
        // Set current version pointer to null (indicating the current state is not stored as a version)
        note.setCurrentVersionPointer(null);
        log.info("Updated current version pointer to null (current version)");
        
        return noteRepository.save(note);
    }
    
    /**
     * Get version history for a note, including current state as highest version
     */
    @Transactional(readOnly = true)
    public List<NoteVersion> getVersionHistory(Long noteId) {
        log.info("Getting version history for note: {}", noteId);
        
        // Get all stored versions from database
        List<NoteVersion> storedVersions = noteVersionRepository.findByNoteIdOrderByVersionNumberDesc(noteId);
        log.info("Found {} stored versions for note {}", storedVersions.size(), noteId);
        
        // Get current note to create virtual current version
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return storedVersions;
        }
        
        Note currentNote = noteOpt.get();
        
        // Always create a virtual current version to show the current state
        int currentVersionNumber = storedVersions.isEmpty() ? 1 : storedVersions.get(0).getVersionNumber() + 1;
        
        // Create virtual current version
        NoteVersion currentVersion = new NoteVersion();
        currentVersion.setId(-1L); // Virtual ID
        currentVersion.setNoteId(noteId);
        currentVersion.setVersionNumber(currentVersionNumber);
        currentVersion.setTitle(currentNote.getTitle());
        currentVersion.setContent(currentNote.getContent());
        currentVersion.setCreatedBy(currentNote.getCreatorId());
        currentVersion.setNoteCreatorId(currentNote.getCreatorId());
        currentVersion.setCreatedAt(currentNote.getUpdatedAt() != null ? currentNote.getUpdatedAt() : currentNote.getCreatedAt());
        currentVersion.setOriginalCreatedAt(currentNote.getCreatedAt());
        currentVersion.setOriginalUpdatedAt(currentNote.getUpdatedAt() != null ? currentNote.getUpdatedAt() : currentNote.getCreatedAt());
        
        // Check if current state matches any stored version (indicating a restore)
        NoteVersion matchingVersion = null;
        for (NoteVersion stored : storedVersions) {
            boolean titleMatches = Objects.equals(currentNote.getTitle(), stored.getTitle());
            boolean contentMatches = Objects.equals(currentNote.getContent(), stored.getContent());
            
            if (titleMatches && contentMatches) {
                matchingVersion = stored;
                break;
            }
        }
        
        // If current state matches a stored version, mark it as restored
        if (matchingVersion != null) {
            log.info("Current note state matches stored version {}, marking as restored", matchingVersion.getVersionNumber());
            currentVersion.setIsRestored(true);
            currentVersion.setRestoredFromVersion(matchingVersion.getVersionNumber());
        }
        
        // Copy permissions
        if (currentNote.getReaders() != null) {
            currentVersion.setReaders(new ArrayList<>(currentNote.getReaders()));
        }
        if (currentNote.getWriters() != null) {
            currentVersion.setWriters(new ArrayList<>(currentNote.getWriters()));
        }
        
        // Always return all versions: current virtual version first, then all stored versions
        List<NoteVersion> allVersions = new ArrayList<>();
        allVersions.add(currentVersion);
        allVersions.addAll(storedVersions);
        
        log.info("Returning {} total versions (1 current + {} stored)", allVersions.size(), storedVersions.size());
        return allVersions;
    }
    
    /**
     * Switch to a specific version without creating new versions
     * This simply changes which version is "current" and updates note content
     */
    public Note restoreToVersion(Long noteId, Integer versionNumber, Long editorUserId) {
        log.info("Starting switch of note {} to version {} by user {}", noteId, versionNumber, editorUserId);
        
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found with id: " + noteId));
        
        // Check permissions
        if (!canUserEditNote(note, editorUserId)) {
            throw new SecurityException("User does not have permission to edit this note");
        }
        
        // Find the target version
        NoteVersion targetVersion = noteVersionRepository.findByNoteIdAndVersionNumber(noteId, versionNumber)
            .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionNumber));
        
        log.info("Found target version {} for note {}", versionNumber, noteId);
        
        try {
            // Save current state as a version before switching (to preserve current content)
            log.info("Saving current state as version before switching to version {}", versionNumber);
            versionManager.createVersion(note, editorUserId);
            
            // Update note content to match the target version
            log.info("Switching note content to version {}", versionNumber);
            note.setTitle(targetVersion.getTitle());
            note.setContent(targetVersion.getContent());
            
            // Update permission arrays safely
            note.getReaders().clear();
            if (targetVersion.getReaders() != null) {
                note.getReaders().addAll(targetVersion.getReaders());
            }
            
            note.getWriters().clear();
            if (targetVersion.getWriters() != null) {
                note.getWriters().addAll(targetVersion.getWriters());
            }
            
            // Set current version pointer to null (current state is now live, not stored)
            note.setCurrentVersionPointer(null);
            
            // Save the note - this becomes the new current state
            Note savedNote = noteRepository.save(note);
            
            log.info("Successfully restored note {} to version {} - no new version created", noteId, versionNumber);
            return savedNote;
            
        } catch (Exception e) {
            log.error("Error during version switch for note {} to version {}: {}", noteId, versionNumber, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Delete all versions for a note (typically called when note is deleted)
     */
    public void deleteVersionHistory(Long noteId) {
        noteVersionRepository.deleteByNoteId(noteId);
        log.info("Deleted version history for note {}", noteId);
    }
    
    /**
     * Get a specific version of a note
     */
    @Transactional(readOnly = true)
    public Optional<NoteVersion> getVersion(Long noteId, Integer versionNumber) {
        // First try to get from database
        Optional<NoteVersion> storedVersion = noteVersionRepository.findByNoteIdAndVersionNumber(noteId, versionNumber);
        if (storedVersion.isPresent()) {
            return storedVersion;
        }
        
        // If not found, check if it's the current version (virtual)
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Note note = noteOpt.get();
        List<NoteVersion> storedVersions = noteVersionRepository.findByNoteIdOrderByVersionNumberDesc(noteId);
        
        // Determine the current version number
        int currentVersionNumber = storedVersions.isEmpty() ? 1 : storedVersions.get(0).getVersionNumber() + 1;
        
        // If requested version is the current version, create virtual version
        if (versionNumber == currentVersionNumber) {
            NoteVersion currentVersion = new NoteVersion();
            currentVersion.setId(-1L); // Virtual ID
            currentVersion.setNoteId(noteId);
            currentVersion.setVersionNumber(versionNumber);
            currentVersion.setTitle(note.getTitle());
            currentVersion.setContent(note.getContent());
            currentVersion.setCreatedBy(note.getCreatorId());
            currentVersion.setNoteCreatorId(note.getCreatorId());
            currentVersion.setCreatedAt(note.getUpdatedAt() != null ? note.getUpdatedAt() : note.getCreatedAt());
            currentVersion.setOriginalCreatedAt(note.getCreatedAt());
            currentVersion.setOriginalUpdatedAt(note.getUpdatedAt() != null ? note.getUpdatedAt() : note.getCreatedAt());
            
            // Copy permissions
            if (note.getReaders() != null) {
                currentVersion.setReaders(new ArrayList<>(note.getReaders()));
            }
            if (note.getWriters() != null) {
                currentVersion.setWriters(new ArrayList<>(note.getWriters()));
            }
            
            return Optional.of(currentVersion);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get version history with usernames for display
     */
    @Transactional(readOnly = true)
    public List<NoteVersionDTO> getVersionHistoryWithUsernames(Long noteId) {
        log.info("Getting version history with usernames for note: {}", noteId);
        
        List<NoteVersion> versions = getVersionHistory(noteId);
        
        // Collect all user IDs that created versions
        List<Long> userIds = versions.stream()
            .map(NoteVersion::getCreatedBy)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        // Get usernames
        Map<Long, String> userIdToUsername = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Object[]> userResults = userRepository.findUsernamesByIds(userIds);
            for (Object[] result : userResults) {
                Long userId = (Long) result[0];
                String username = (String) result[1];
                userIdToUsername.put(userId, username);
            }
        }
        
        // Convert to DTOs with usernames
        return versions.stream()
            .map(version -> convertToDTO(version, userIdToUsername))
            .collect(Collectors.toList());
    }
    
    /**
     * Convert NoteVersion to NoteVersionDTO with username
     */
    private NoteVersionDTO convertToDTO(NoteVersion version, Map<Long, String> userIdToUsername) {
        NoteVersionDTO dto = new NoteVersionDTO();
        dto.setId(version.getId());
        dto.setNoteId(version.getNoteId());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setTitle(version.getTitle());
        dto.setContent(version.getContent());
        dto.setReaders(version.getReaders());
        dto.setWriters(version.getWriters());
        dto.setCreatedBy(version.getCreatedBy());
        dto.setCreatedByUsername(userIdToUsername.get(version.getCreatedBy()));
        dto.setNoteCreatorId(version.getNoteCreatorId());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setOriginalCreatedAt(version.getOriginalCreatedAt());
        dto.setOriginalUpdatedAt(version.getOriginalUpdatedAt());
        dto.setIsRestored(version.getIsRestored());
        dto.setRestoredFromVersion(version.getRestoredFromVersion());
        return dto;
    }
    
    /**
     * Check if a user can edit a note
     */
    private boolean canUserEditNote(Note note, Long userId) {
        return note.isCreator(userId) || note.hasWritePermission(userId);
    }
}
