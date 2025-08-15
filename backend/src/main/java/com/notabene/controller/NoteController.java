package com.notabene.controller;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.dto.AddPermissionRequest;
import com.notabene.dto.RemovePermissionRequest;
import com.notabene.dto.NotePermissionsResponse;
import com.notabene.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(@RequestParam String q) {
        List<NoteResponse> notes = noteService.searchNotes(q);
        return ResponseEntity.ok(notes);
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
}
