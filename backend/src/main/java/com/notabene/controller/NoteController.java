package com.notabene.controller;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
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
}
