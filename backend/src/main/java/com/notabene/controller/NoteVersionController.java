package com.notabene.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notabene.dto.EnhancedVersionComparisonDTO;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.NoteVersionDTO;
import com.notabene.dto.TextDiffDTO;
import com.notabene.entity.Note;
import com.notabene.entity.NoteVersion;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.service.AuthenticationService;
import com.notabene.service.NoteService;
import com.notabene.service.NoteVersioningService;
import com.notabene.service.TextDiffService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing note versions
 */
@RestController
@RequestMapping("/api/notes/{noteId}/versions")
@RequiredArgsConstructor
@Slf4j
public class NoteVersionController {
    
    private final NoteVersioningService noteVersioningService;
    private final AuthenticationService authenticationService;
    private final NoteRepository noteRepository;
    private final NoteService noteService;
    private final TextDiffService textDiffService;
    
    /**
     * Get version history for a note
     */
    @GetMapping
    public ResponseEntity<List<NoteVersionDTO>> getVersionHistory(@PathVariable Long noteId) {
        try {
            log.info("Getting version history for note: {}", noteId);
            
            // Check if user has permission to read the note
            User currentUser = authenticationService.getCurrentUser();
            Optional<Note> noteOpt = noteRepository.findByIdWithReadPermission(noteId, currentUser.getId());
            
            if (noteOpt.isEmpty()) {
                log.warn("User {} does not have permission to read note {}", currentUser.getUsername(), noteId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<NoteVersionDTO> versions = noteVersioningService.getVersionHistoryWithUsernames(noteId);
            log.info("Retrieved {} versions for note {}", versions.size(), noteId);
            return ResponseEntity.ok(versions);
        } catch (Exception e) {
            log.error("Error getting version history for note {}: {}", noteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific version of a note
     */
    @GetMapping("/{versionNumber}")
    public ResponseEntity<NoteVersion> getVersion(@PathVariable Long noteId,
                                                 @PathVariable Integer versionNumber) {
        try {
            log.info("Getting version {} for note {}", versionNumber, noteId);
            
            // Check if user has permission to read the note
            User currentUser = authenticationService.getCurrentUser();
            Optional<Note> noteOpt = noteRepository.findByIdWithReadPermission(noteId, currentUser.getId());
            
            if (noteOpt.isEmpty()) {
                log.warn("User {} does not have permission to read note {}", currentUser.getUsername(), noteId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<NoteVersion> version = noteVersioningService.getVersion(noteId, versionNumber);
            
            if (version.isPresent()) {
                log.info("Found version {} for note {}", versionNumber, noteId);
                return ResponseEntity.ok(version.get());
            } else {
                log.warn("Version {} not found for note {}", versionNumber, noteId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting version {} for note {}: {}", versionNumber, noteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Restore a note to a specific version
     */
    @PostMapping("/{versionNumber}/restore")
    @Transactional
    public ResponseEntity<NoteResponse> restoreToVersion(@PathVariable Long noteId, 
                                                        @PathVariable Integer versionNumber) {
        try {
            Long currentUserId = authenticationService.getCurrentUser().getId();
            log.info("User {} restoring note {} to version {}", currentUserId, noteId, versionNumber);
            
            Note restoredNote = noteVersioningService.restoreToVersion(noteId, versionNumber, currentUserId);
            log.info("Successfully restored note {} to version {}", noteId, versionNumber);
            
            // Convert to DTO to avoid serialization issues
            NoteResponse noteResponse = noteService.convertToNoteResponse(restoredNote, currentUserId);
            
            return ResponseEntity.ok(noteResponse);
        } catch (SecurityException e) {
            log.warn("Security error restoring note {} to version {}: {}", noteId, versionNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.warn("Validation error restoring note {} to version {}: {}", noteId, versionNumber, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error restoring note {} to version {}: {}", noteId, versionNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Enhanced comparison with character-level diff highlighting
     */
    @GetMapping("/compare")
    public ResponseEntity<EnhancedVersionComparisonDTO> compareVersionsEnhanced(@PathVariable Long noteId,
                                                                               @RequestParam Integer oldVersion,
                                                                               @RequestParam Integer newVersion) {
        try {
            if (oldVersion == null || newVersion == null) {
                log.warn("Missing version parameters for enhanced comparison: oldVersion={}, newVersion={}", oldVersion, newVersion);
                return ResponseEntity.badRequest().build();
            }
            
            // Check if user has permission to read the note
            User currentUser = authenticationService.getCurrentUser();
            Optional<Note> noteOpt = noteRepository.findByIdWithReadPermission(noteId, currentUser.getId());
            
            if (noteOpt.isEmpty()) {
                log.warn("User {} does not have permission to read note {}", currentUser.getUsername(), noteId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            log.info("Enhanced comparing versions {} and {} for note {}", oldVersion, newVersion, noteId);
            
            // Ottieni le versioni
            Optional<NoteVersion> oldVersionOpt = noteVersioningService.getVersion(noteId, oldVersion);
            Optional<NoteVersion> newVersionOpt = noteVersioningService.getVersion(noteId, newVersion);
            
            if (oldVersionOpt.isEmpty() || newVersionOpt.isEmpty()) {
                log.warn("Version not found for enhanced comparison: oldVersion={}, newVersion={}", oldVersion, newVersion);
                return ResponseEntity.notFound().build();
            }
            
            NoteVersion oldNote = oldVersionOpt.get();
            NoteVersion newNote = newVersionOpt.get();
            
            // Calcola le differenze carattere per carattere
            TextDiffDTO titleDiff = textDiffService.calculateDiff(oldNote.getTitle(), newNote.getTitle());
            TextDiffDTO contentDiff = textDiffService.calculateDiff(oldNote.getContent(), newNote.getContent());
            
            // Ottimizza i segmenti per una migliore visualizzazione
            textDiffService.optimizeSegments(titleDiff.getLeftSegments());
            textDiffService.optimizeSegments(titleDiff.getRightSegments());
            textDiffService.optimizeSegments(contentDiff.getLeftSegments());
            textDiffService.optimizeSegments(contentDiff.getRightSegments());
            
            // Crea la comparazione con le differenze calcolate
            EnhancedVersionComparisonDTO comparison = new EnhancedVersionComparisonDTO(
                oldNote, newNote, titleDiff, contentDiff
            );
            
            log.info("Successfully enhanced compared versions {} and {} for note {}", oldVersion, newVersion, noteId);
            return ResponseEntity.ok(comparison);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error in enhanced comparison for note {}: {}", noteId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error in enhanced comparison for note {}: {}", noteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
