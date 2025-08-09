package com.notabene.controller;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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
        
        List<NoteResponse> notes;
        if (page != null && size != null) {
            notes = noteService.getAllNotesPaginated(page, size);
        } else {
            notes = noteService.getAllNotes();
        }
        return ResponseEntity.ok(notes);
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
    
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<NoteResponse>> getNotesByPriority(@PathVariable Integer priority) {
        List<NoteResponse> notes = noteService.getNotesByPriority(priority);
        return ResponseEntity.ok(notes);
    }
}
