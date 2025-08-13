package com.notabene.service;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoteService {
    
    private final NoteRepository noteRepository;
    private final AuthenticationService authenticationService;
    
    public NoteResponse createNote(CreateNoteRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        log.info("Creating note for user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
        
        Note note = new Note(request.getTitle(), request.getContent(), currentUser);
        Note savedNote = noteRepository.save(note);
        log.info("Note created successfully with ID: {}", savedNote.getId());
        
        return NoteResponse.fromEntity(savedNote);
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes() {
        try {
            User currentUser = authenticationService.getCurrentUser();
            log.info("Getting all notes for user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
            
            List<Note> notes = noteRepository.findByUserOrderByCreatedAtDesc(currentUser);
            log.info("Retrieved {} notes for user {}", notes.size(), currentUser.getUsername());
            
            List<NoteResponse> responses = notes.stream()
                    .map(NoteResponse::fromEntity)
                    .collect(Collectors.toList());
            log.info("Converted to {} note responses", responses.size());
            return responses;
        } catch (Exception e) {
            log.error("Error in getAllNotes service method", e);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotesPaginated(int page, int size) {
        User currentUser = authenticationService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notePage = noteRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        return notePage.getContent()
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long id) {
        User currentUser = authenticationService.getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id + " for current user"));
        return NoteResponse.fromEntity(note);
    }
    
    public NoteResponse updateNote(Long id, UpdateNoteRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id + " for current user"));
        
        // Update only non-null fields
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            note.setContent(request.getContent());
        }
        
        Note updatedNote = noteRepository.save(note);
        return NoteResponse.fromEntity(updatedNote);
    }
    
    public void deleteNote(Long id) {
        User currentUser = authenticationService.getCurrentUser();
        Note note = noteRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id + " for current user"));
        noteRepository.delete(note);
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String search) {
        User currentUser = authenticationService.getCurrentUser();
        return noteRepository.searchNotesByUser(currentUser, search)
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
