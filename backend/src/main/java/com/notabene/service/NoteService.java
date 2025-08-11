package com.notabene.service;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
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
public class NoteService {
    
    private final NoteRepository noteRepository;
    
    public NoteResponse createNote(CreateNoteRequest request) {
        Note note = new Note(request.getTitle(), request.getContent());
        Note savedNote = noteRepository.save(note);
        return NoteResponse.fromEntity(savedNote);
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes() {
        return noteRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notePage = noteRepository.findAllByOrderByCreatedAtDesc(pageable);
        return notePage.getContent()
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        return NoteResponse.fromEntity(note);
    }
    
    public NoteResponse updateNote(Long id, UpdateNoteRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + id));
        
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
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException("Note not found with id: " + id);
        }
        noteRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String search) {
        return noteRepository.searchNotes(search)
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
