package com.notabene.controller;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.SearchNotesRequest;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.dto.AddPermissionRequest;
import com.notabene.dto.RemovePermissionRequest;
import com.notabene.dto.NotePermissionsResponse;
import com.notabene.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Slf4j
public class NoteController {
    
    private final NoteService noteService;
    
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody CreateNoteRequest request) {
        NoteResponse response = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        try {
            log.info("Getting all notes - page: {}, size: {}", page, size);
            List<NoteResponse> notes;
            if (page != null && size != null) {
                notes = noteService.getAllNotesPaginated(page, size);
            } else {
                notes = noteService.getAllNotes();
            }
            log.info("Successfully retrieved {} notes", notes.size());
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting all notes", e);
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        NoteResponse response = noteService.getNoteById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateNoteRequest request) {
        NoteResponse response = noteService.updateNote(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/copy")
    public ResponseEntity<NoteResponse> copyNote(@PathVariable Long id) {
        try {
            log.info("Copying note with ID: {}", id);
            NoteResponse copiedNote = noteService.copyNote(id);
            log.info("Note copied successfully with new ID: {}", copiedNote.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(copiedNote);
        } catch (Exception e) {
            log.error("Error copying note with ID: {}", id, e);
            throw e;
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(@RequestParam String q) {
        try {
            log.info("Basic search - query: {}", q);
            List<NoteResponse> notes = noteService.searchNotes(q);
            log.info("Basic search completed successfully. Found {} notes", notes.size());
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error in basic search - query: {}", q, e);
            throw e;
        }
    }
    
    /**
     * Advanced search with multiple criteria (GET with query parameters)
     * Supports ISO date format: 2025-08-25T10:30:00
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<List<NoteResponse>> searchNotesAdvanced(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedBefore,
            @RequestParam(required = false) Long folderId) {
        
        try {
            
            SearchNotesRequest request = new SearchNotesRequest();
            request.setQuery(query);
            if (tags != null && !tags.trim().isEmpty()) {
                request.setTags(Arrays.asList(tags.split(",")));
                log.info("Parsed tags: {}", request.getTags());
            }
            request.setAuthor(author);
            request.setCreatedAfter(createdAfter);
            request.setCreatedBefore(createdBefore);
            request.setUpdatedAfter(updatedAfter);
            request.setUpdatedBefore(updatedBefore);
            request.setFolderId(folderId);
            
            log.info("Calling noteService.searchNotesAdvanced with request: {}", request);
            List<NoteResponse> notes = noteService.searchNotesAdvanced(request);
            log.info("Advanced search GET completed successfully. Found {} notes", notes.size());
            
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error in advanced search GET - query: {}, tags: {}, author: {}, folderId: {}", 
                    query, tags, author, folderId, e);
            log.error("Date parsing might have failed. Expected format: 2025-08-25T10:30:00", e);
            throw e;
        }
    }
    
    /**
     * Advanced search with flexible date format (alternative endpoint)
     * Supports: 2025-08-25 10:30:00 or 2025-08-25T10:30:00
     */
    @GetMapping("/search/advanced-flexible")
    public ResponseEntity<List<NoteResponse>> searchNotesAdvancedFlexible(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String createdAfter,
            @RequestParam(required = false) String createdBefore,
            @RequestParam(required = false) String updatedAfter,
            @RequestParam(required = false) String updatedBefore,
            @RequestParam(required = false) Long folderId) {
        
        try {
            log.info("Flexible advanced search - query: {}, tags: {}, author: {}, folderId: {}", query, tags, author, folderId);
            log.info("Flexible search - RAW DATE STRINGS: createdAfter: [{}], createdBefore: [{}], updatedAfter: [{}], updatedBefore: [{}]", 
                    createdAfter, createdBefore, updatedAfter, updatedBefore);
            
            SearchNotesRequest request = new SearchNotesRequest();
            request.setQuery(query);
            if (tags != null && !tags.trim().isEmpty()) {
                request.setTags(Arrays.asList(tags.split(",")));
            }
            request.setAuthor(author);
            
            // Parse dates manually with flexible formats
            request.setCreatedAfter(parseFlexibleDateTime(createdAfter, "createdAfter"));
            request.setCreatedBefore(parseFlexibleDateTime(createdBefore, "createdBefore"));
            request.setUpdatedAfter(parseFlexibleDateTime(updatedAfter, "updatedAfter"));
            request.setUpdatedBefore(parseFlexibleDateTime(updatedBefore, "updatedBefore"));
            request.setFolderId(folderId);
            
            List<NoteResponse> notes = noteService.searchNotesAdvanced(request);
            
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error in flexible advanced search", e);
            throw e;
        }
    }
    
    private LocalDateTime parseFlexibleDateTime(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try different formats
            String trimmed = dateStr.trim();
            
            // Format 1: 2025-08-25T10:30:00 (ISO)
            if (trimmed.contains("T")) {
                LocalDateTime parsed = LocalDateTime.parse(trimmed);
                log.info("Parsed {} with ISO format: {} -> {}", fieldName, trimmed, parsed);
                return parsed;
            }
            
            // Format 2: 2025-08-25 10:30:00 (space separated)
            if (trimmed.contains(" ")) {
                String isoFormat = trimmed.replace(" ", "T");
                LocalDateTime parsed = LocalDateTime.parse(isoFormat);
                log.info("Parsed {} with space format: {} -> {} -> {}", fieldName, trimmed, isoFormat, parsed);
                return parsed;
            }
            
            // Format 3: 2025-08-25 (date only, add time)
            if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String isoFormat = trimmed + "T00:00:00";
                LocalDateTime parsed = LocalDateTime.parse(isoFormat);
                log.info("Parsed {} with date-only format: {} -> {} -> {}", fieldName, trimmed, isoFormat, parsed);
                return parsed;
            }
            
            log.warn("Unable to parse date {} for field {}: unrecognized format", trimmed, fieldName);
            return null;
            
        } catch (Exception e) {
            log.error("Error parsing date {} for field {}: {}", dateStr, fieldName, e.getMessage());
            return null;
        }
    }
    
    /**
     * Advanced search with POST request body
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<List<NoteResponse>> searchNotesAdvancedPost(
            @Valid @RequestBody SearchNotesRequest request) {
        
        try {
            log.info("Advanced search POST - query: {}, tags: {}, author: {}, folderId: {}", 
                    request.getQuery(), request.getTags(), request.getAuthor(), request.getFolderId());
            log.info("Advanced search POST - createdAfter: {}, createdBefore: {}, updatedAfter: {}, updatedBefore: {}", 
                    request.getCreatedAfter(), request.getCreatedBefore(), 
                    request.getUpdatedAfter(), request.getUpdatedBefore());
            
            List<NoteResponse> notes = noteService.searchNotesAdvanced(request);
            log.info("Advanced search POST completed successfully. Found {} notes", notes.size());
            
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error in advanced search POST - request: {}", request, e);
            throw e;
        }
    }
    
    /**
     * Get notes created by the current user
     */
    @GetMapping("/created")
    public ResponseEntity<List<NoteResponse>> getCreatedNotes() {
        log.info("Getting created notes for current user");
        List<NoteResponse> notes = noteService.getCreatedNotes();
        return ResponseEntity.ok(notes);
    }
    
    /**
     * Get notes shared with the current user
     */
    @GetMapping("/shared")
    public ResponseEntity<List<NoteResponse>> getSharedNotes() {
        log.info("Getting shared notes for current user");
        List<NoteResponse> notes = noteService.getSharedNotes();
        return ResponseEntity.ok(notes);
    }
    
    /**
     * Get permissions for a specific note
     */
    @GetMapping("/{noteId}/permissions")
    public ResponseEntity<NotePermissionsResponse> getNotePermissions(@PathVariable Long noteId) {
        log.info("Getting permissions for note ID: {}", noteId);
        NotePermissionsResponse permissions = noteService.getNotePermissions(noteId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Add read permission for a user to a note
     */
    @PostMapping("/{noteId}/permissions/readers")
    public ResponseEntity<Void> addReaderPermission(
            @PathVariable Long noteId,
            @Valid @RequestBody AddPermissionRequest request) {
        log.info("Adding read permission for user {} to note {}", request.getUsername(), noteId);
        
        noteService.addReaderByUsername(noteId, request.getUsername());
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Add write permission for a user to a note
     */
    @PostMapping("/{noteId}/permissions/writers")
    public ResponseEntity<Void> addWriterPermission(
            @PathVariable Long noteId,
            @Valid @RequestBody AddPermissionRequest request) {
        log.info("Adding write permission for user {} to note {}", request.getUsername(), noteId);
        
        noteService.addWriterByUsername(noteId, request.getUsername());
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove read permission for a user from a note (by username)
     */
    @DeleteMapping("/{noteId}/permissions/readers")
    public ResponseEntity<Void> removeReaderPermissionByUsername(
            @PathVariable Long noteId,
            @Valid @RequestBody RemovePermissionRequest request) {
        log.info("Removing read permission for user {} from note {}", request.getUsername(), noteId);
        
        noteService.removeReaderByUsername(noteId, request.getUsername());
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove write permission for a user from a note (by username)
     */
    @DeleteMapping("/{noteId}/permissions/writers")
    public ResponseEntity<Void> removeWriterPermissionByUsername(
            @PathVariable Long noteId,
            @Valid @RequestBody RemovePermissionRequest request) {
        log.info("Removing write permission for user {} from note {}", request.getUsername(), noteId);
        
        noteService.removeWriterByUsername(noteId, request.getUsername());
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove read permission for a user from a note (by userId)
     */
    @DeleteMapping("/{noteId}/permissions/readers/{userId}")
    public ResponseEntity<Void> removeReaderPermission(
            @PathVariable Long noteId,
            @PathVariable Long userId) {
        log.info("Removing read permission for user {} from note {}", userId, noteId);
        
        noteService.removeReaderPermission(noteId, userId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove write permission for a user from a note (by userId)
     */
    @DeleteMapping("/{noteId}/permissions/writers/{userId}")
    public ResponseEntity<Void> removeWriterPermission(
            @PathVariable Long noteId,
            @PathVariable Long userId) {
        log.info("Removing write permission for user {} from note {}", userId, noteId);
        
        noteService.removeWriterPermission(noteId, userId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Add read permission for a user to a note (backward compatibility)
     */
    @PostMapping("/{id}/readers")
    public ResponseEntity<Void> addReader(
            @PathVariable Long id, 
            @Valid @RequestBody AddPermissionRequest request) {
        log.info("Adding read permission for user {} to note {}", request.getUsername(), id);
        noteService.addReaderByUsername(id, request.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * Allow user to remove themselves from a shared note
     */
    @DeleteMapping("/{noteId}/leave")
    public ResponseEntity<Void> leaveSharedNote(@PathVariable Long noteId) {
        log.info("User leaving shared note with ID: {}", noteId);
        noteService.leaveSharedNote(noteId);
        return ResponseEntity.ok().build();
    }
}
