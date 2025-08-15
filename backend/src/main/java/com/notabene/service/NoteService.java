package com.notabene.service;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.dto.NotePermissionsResponse;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.exception.UnauthorizedNoteAccessException;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {
    
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    public NoteResponse createNote(CreateNoteRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        log.info("Creating note for user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
        
        Note note = new Note(request.getTitle(), request.getContent(), currentUser);
        Note savedNote = noteRepository.save(note);
        log.info("Note created successfully with ID: {}", savedNote.getId());
        
        return convertToNoteResponse(savedNote, currentUser.getId());
    }
    
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes() {
        try {
            User currentUser = authenticationService.getCurrentUser();
            log.info("Getting all notes for user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
            
            // Get notes created by the user
            List<Note> createdNotes = noteRepository.findByCreatorId(currentUser.getId());
            log.info("Found {} notes created by user", createdNotes.size());
            
            // Get notes shared with the user
            List<Note> sharedNotes = noteRepository.findSharedWithUser(currentUser.getId());
            log.info("Found {} notes shared with user", sharedNotes.size());
            
            // Combine and convert to DTOs with permission flags
            List<NoteResponse> allNotes = new ArrayList<>();
            allNotes.addAll(createdNotes.stream()
                    .map(note -> convertToNoteResponse(note, currentUser.getId()))
                    .collect(Collectors.toList()));
            allNotes.addAll(sharedNotes.stream()
                    .map(note -> convertToNoteResponse(note, currentUser.getId()))
                    .collect(Collectors.toList()));
            
            // Remove duplicates (in case a user has both created and shared access)
            Map<Long, NoteResponse> uniqueNotes = new LinkedHashMap<>();
            for (NoteResponse note : allNotes) {
                uniqueNotes.put(note.getId(), note);
            }
            
            List<NoteResponse> result = new ArrayList<>(uniqueNotes.values());
            log.info("Returning {} total unique notes", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error getting all notes", e);
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
                .map(note -> convertToNoteResponse(note, currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long id) {
        User currentUser = authenticationService.getCurrentUser();
        log.info("Getting note with id: {} for user: {} (ID: {})", id, currentUser.getUsername(), currentUser.getId());
        
        // Users can read notes if they created them or have read permission
        Optional<Note> noteOpt = noteRepository.findByIdWithReadPermission(id, currentUser.getId());
        
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            log.info("Note found: {}", note.getTitle());
            return convertToNoteResponse(note, currentUser.getId());
        } else {
            throw new NoteNotFoundException("Note not found with id: " + id + " for current user");
        }
    }
    
    public NoteResponse updateNote(Long id, UpdateNoteRequest request) {
        User currentUser = authenticationService.getCurrentUser();
        
        // Only users with write permission can update notes
        Optional<Note> noteOpt = noteRepository.findByIdWithWritePermission(id, currentUser.getId());
        
        if (noteOpt.isPresent()) {
            Note note = noteOpt.get();
            // Update only non-null fields
            if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                note.setTitle(request.getTitle());
            }
            if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
                note.setContent(request.getContent());
            }
            note.setUpdatedAt(LocalDateTime.now());
            
            Note updatedNote = noteRepository.save(note);
            return convertToNoteResponse(updatedNote, currentUser.getId());
        } else {
            throw new NoteNotFoundException("Note not found with id: " + id + " for current user or user has no write permission");
        }
    }
    
    public void deleteNote(Long id) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner (creator) can delete notes
        Note note = getNoteWithOwnerPermission(id, currentUser);
        noteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String search) {
        User currentUser = authenticationService.getCurrentUser();
        return noteRepository.searchNotesByUser(currentUser, search)
                .stream()
                .map(note -> convertToNoteResponse(note, currentUser.getId()))
                .collect(Collectors.toList());
    }

    // New permission-based methods for the enhanced system

    @Transactional(readOnly = true)
    public List<NoteResponse> getCreatedNotes() {
        User currentUser = authenticationService.getCurrentUser();
        List<Note> notes = noteRepository.findByCreatorId(currentUser.getId());
        return notes.stream()
                .map(note -> convertToNoteResponse(note, currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getSharedNotes() {
        User currentUser = authenticationService.getCurrentUser();
        List<Note> notes = noteRepository.findSharedWithUser(currentUser.getId());
        return notes.stream()
                .map(note -> convertToNoteResponse(note, currentUser.getId()))
                .collect(Collectors.toList());
    }

    public NotePermissionsResponse getNotePermissions(Long noteId) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner can view permissions
        Note note = getNoteWithOwnerPermission(noteId, currentUser);
        
        return new NotePermissionsResponse(noteId, note.getCreatorId(),
                note.getReaders() != null ? note.getReaders() : new ArrayList<>(),
                note.getWriters() != null ? note.getWriters() : new ArrayList<>());
    }

    // Permission management methods - Only note owners can manage permissions

    @Transactional
    public void addReaderPermission(Long noteId, Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner can manage permissions
        Note note = getNoteWithOwnerPermission(noteId, currentUser);
        
        List<Long> readers = note.getReaders() != null ? new ArrayList<>(note.getReaders()) : new ArrayList<>();
        if (!readers.contains(userId)) {
            readers.add(userId);
            note.setReaders(readers);
            noteRepository.save(note);
        }
    }

    @Transactional
    public void addWriterPermission(Long noteId, Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner can manage permissions
        Note note = getNoteWithOwnerPermission(noteId, currentUser);
        
        List<Long> writers = note.getWriters() != null ? new ArrayList<>(note.getWriters()) : new ArrayList<>();
        List<Long> readers = note.getReaders() != null ? new ArrayList<>(note.getReaders()) : new ArrayList<>();
        
        if (!writers.contains(userId)) {
            writers.add(userId);
            note.setWriters(writers);
        }
        
        // Writers should also have read permission
        if (!readers.contains(userId)) {
            readers.add(userId);
            note.setReaders(readers);
        }
            
        noteRepository.save(note);
    }

    @Transactional
    public void removeReaderPermission(Long noteId, Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner can manage permissions
        Note note = getNoteWithOwnerPermission(noteId, currentUser);
        
        // Prevent removing creator's permissions
        if (note.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove creator's permissions");
        }
        
        List<Long> readers = note.getReaders() != null ? new ArrayList<>(note.getReaders()) : new ArrayList<>();
        readers.remove(userId);
        note.setReaders(readers);
        
        // Also remove from writers if present
        List<Long> writers = note.getWriters() != null ? new ArrayList<>(note.getWriters()) : new ArrayList<>();
        writers.remove(userId);
        note.setWriters(writers);
        
        noteRepository.save(note);
    }

    @Transactional
    public void removeWriterPermission(Long noteId, Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner can manage permissions
        Note note = getNoteWithOwnerPermission(noteId, currentUser);
        
        // Prevent removing creator's permissions
        if (note.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove creator's permissions");
        }
        
        List<Long> writers = note.getWriters() != null ? new ArrayList<>(note.getWriters()) : new ArrayList<>();
        writers.remove(userId);
        note.setWriters(writers);
        noteRepository.save(note);
    }

    // Username-based permission methods for convenience

    @Transactional
    public void addReaderByUsername(Long noteId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        addReaderPermission(noteId, user.getId());
    }

    @Transactional
    public void addWriterByUsername(Long noteId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        addWriterPermission(noteId, user.getId());
    }

    @Transactional
    public void removeReaderByUsername(Long noteId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        removeReaderPermission(noteId, user.getId());
    }

    @Transactional
    public void removeWriterByUsername(Long noteId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        removeWriterPermission(noteId, user.getId());
    }

    // Helper methods
    
    /**
     * Convert Note entity to NoteResponse DTO with permission flags
     */
    private NoteResponse convertToNoteResponse(Note note, Long currentUserId) {
        boolean isOwner = note.getCreatorId().equals(currentUserId);
        boolean canRead = isOwner || (note.getReaders() != null && note.getReaders().contains(currentUserId));
        boolean canWrite = isOwner || (note.getWriters() != null && note.getWriters().contains(currentUserId));
        
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        response.setCreatorId(note.getCreatorId());
        
        // Convert user IDs to usernames
        if (note.getReaders() != null && !note.getReaders().isEmpty()) {
            List<String> readerUsernames = note.getReaders().stream()
                .map(userId -> userRepository.findById(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(User::getUsername)
                .collect(Collectors.toList());
            response.setReaders(readerUsernames);
        }
        
        if (note.getWriters() != null && !note.getWriters().isEmpty()) {
            List<String> writerUsernames = note.getWriters().stream()
                .map(userId -> userRepository.findById(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(User::getUsername)
                .collect(Collectors.toList());
            response.setWriters(writerUsernames);
        }
        
        response.setIsOwner(isOwner);
        response.setCanEdit(canWrite);  // Can edit if owner or has write permission
        response.setCanDelete(isOwner);  // Can delete only if owner
        response.setCanShare(isOwner);   // Can share only if owner
        return response;
    }

    /**
     * Get note with owner permission check (only creator can access)
     */
    private Note getNoteWithOwnerPermission(Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));
        
        if (!note.getCreatorId().equals(user.getId())) {
            throw new UnauthorizedNoteAccessException("Only the note owner can perform this operation");
        }
        
        return note;
    }
}
