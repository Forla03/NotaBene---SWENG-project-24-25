package com.notabene.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notabene.dto.CreateNoteRequest;
import com.notabene.dto.NotePermissionsResponse;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.SearchNotesRequest;
import com.notabene.dto.TagDTO;
import com.notabene.dto.UpdateNoteRequest;
import com.notabene.entity.Note;
import com.notabene.exception.NoteNotFoundException;
import com.notabene.exception.UnauthorizedNoteAccessException;
import com.notabene.model.User;
import com.notabene.repository.NoteRepository;
import com.notabene.repository.TagRepository;
import com.notabene.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {
    
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    public NoteResponse createNote(CreateNoteRequest request) {
    User currentUser = authenticationService.getCurrentUser();
    log.info("Creating note for user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());

    // crea entity
    Note note = new Note(request.getTitle(), request.getContent(), currentUser);

    // TAGS: aggiungi PRIMA del save
    List<Long> ids = Optional.ofNullable(request.getTagIds()).orElseGet(List::of);
    if (!ids.isEmpty()) {
        var unique = new HashSet<>(ids);
        var tags = new HashSet<>(tagRepository.findAllById(unique));
        if (tags.size() != unique.size()) {
            throw new IllegalArgumentException("Alcuni tagId non esistono");
        }
        note.getTags().addAll(tags); // usa la collection esistente
    }

    Note saved = noteRepository.save(note);
    log.info("Note created successfully with ID: {}", saved.getId());

    return convertToNoteResponse(saved, currentUser.getId());
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
    
    @Transactional
public NoteResponse updateNote(Long id, UpdateNoteRequest request) {
    User currentUser = authenticationService.getCurrentUser();

    // verifica permessi di scrittura
    Note note = noteRepository.findByIdWithWritePermission(id, currentUser.getId())
        .orElseThrow(() -> new NoteNotFoundException(
            "Note not found with id: " + id + " for current user or user has no write permission"));

    // aggiorna campi base
    if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
        note.setTitle(request.getTitle());
    }
    if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
        note.setContent(request.getContent());
    }
    note.setUpdatedAt(LocalDateTime.now());

    // TAGS: sincronizza SOLO se il campo è presente (null = non toccare)
    if (request.getTagIds() != null) {
        var unique = new HashSet<>(request.getTagIds());
        var tags = new HashSet<>(tagRepository.findAllById(unique));
        if (tags.size() != unique.size()) {
            throw new IllegalArgumentException("Alcuni tagId non esistono");
        }
        note.getTags().clear();
        note.getTags().addAll(tags);
    }

    Note updated = noteRepository.save(note);
    return convertToNoteResponse(updated, currentUser.getId());
    }

    
    public void deleteNote(Long id) {
        User currentUser = authenticationService.getCurrentUser();
        // Only the note owner (creator) can delete notes
        Note note = getNoteWithOwnerPermission(id, currentUser);
        noteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String search) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            log.info("Basic search - user: {} (ID: {}), query: '{}'", 
                    currentUser.getUsername(), currentUser.getId(), search);
            
            List<Note> notes = noteRepository.searchNotesWithReadPermission(currentUser.getId(), search);
            log.info("Basic search - found {} notes", notes.size());
            
            List<NoteResponse> result = notes.stream()
                    .map(note -> convertToNoteResponse(note, currentUser.getId()))
                    .collect(Collectors.toList());
            
            log.info("Basic search completed successfully");
            return result;
        } catch (Exception e) {
            log.error("Error in basic search - query: '{}'", search, e);
            throw e;
        }
    }
    
    /**
     * Advanced search with multiple criteria
     */
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotesAdvanced(SearchNotesRequest request) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            log.info("Advanced search - user: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
            log.info("Advanced search request - query: '{}', author: '{}', folderId: {}", 
                    request.getQuery(), request.getAuthor(), request.getFolderId());
            log.info("Advanced search request - tags: {}", request.getTags());
            log.info("Advanced search request - createdAfter: {}, createdBefore: {}", 
                    request.getCreatedAfter(), request.getCreatedBefore());
            log.info("Advanced search request - updatedAfter: {}, updatedBefore: {}", 
                    request.getUpdatedAfter(), request.getUpdatedBefore());
            
            log.info("Calling repository.searchNotesAdvanced with parameters...");
            List<Note> notes = noteRepository.searchNotesAdvanced(
                currentUser.getId(),
                request.getQuery(),
                request.getAuthor(),
                request.getCreatedAfter(),
                request.getCreatedBefore(),
                request.getUpdatedAfter(),
                request.getUpdatedBefore(),
                request.getFolderId()
            );
            log.info("Repository search returned {} notes", notes.size());
            
            // Filter by tags if specified (post-processing since we removed it from SQL)
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                log.info("Applying tag filter for tags: {}", request.getTags());
                int beforeTagFilter = notes.size();
                notes = notes.stream()
                        .filter(note -> {
                            if (note.getTags() == null || note.getTags().isEmpty()) {
                                return false;
                            }
                            return note.getTags().stream()
                                    .anyMatch(tag -> request.getTags().stream()
                                            .anyMatch(requestTag -> tag.getName().toLowerCase()
                                                    .contains(requestTag.toLowerCase())));
                        })
                        .collect(Collectors.toList());
                log.info("Tag filter applied: {} notes before filter, {} notes after filter", 
                        beforeTagFilter, notes.size());
            }
            
            List<NoteResponse> result = notes.stream()
                    .map(note -> convertToNoteResponse(note, currentUser.getId()))
                    .collect(Collectors.toList());
            
            log.info("Advanced search completed successfully. Returning {} notes", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error in advanced search - request: {}", request, e);
            throw e;
        }
    }
    
    /**
     * Search notes within a specific folder
     */
    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotesInFolder(Long folderId, SearchNotesRequest request) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            log.info("Folder search - user: {} (ID: {}), folderId: {}", 
                    currentUser.getUsername(), currentUser.getId(), folderId);
            log.info("Folder search request - query: '{}', author: '{}', tags: {}", 
                    request.getQuery(), request.getAuthor(), request.getTags());
            
            // Set the folder ID in the request if not already set
            SearchNotesRequest folderRequest = new SearchNotesRequest();
            folderRequest.setQuery(request.getQuery());
            folderRequest.setTags(request.getTags());
            folderRequest.setAuthor(request.getAuthor());
            folderRequest.setCreatedAfter(request.getCreatedAfter());
            folderRequest.setCreatedBefore(request.getCreatedBefore());
            folderRequest.setUpdatedAfter(request.getUpdatedAfter());
            folderRequest.setUpdatedBefore(request.getUpdatedBefore());
            folderRequest.setFolderId(folderId);
            
            log.info("Calling searchNotesAdvanced for folder search with folderId: {}", folderId);
            List<NoteResponse> result = searchNotesAdvanced(folderRequest);
            log.info("Folder search completed successfully. Found {} notes in folder {}", 
                    result.size(), folderId);
            
            return result;
        } catch (Exception e) {
            log.error("Error in folder search - folderId: {}, request: {}", folderId, request, e);
            throw e;
        }
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
        
        // Convert user IDs to usernames
        List<String> readerUsernames = new ArrayList<>();
        List<String> writerUsernames = new ArrayList<>();
        
        if (note.getReaders() != null) {
            for (Long userId : note.getReaders()) {
                userRepository.findById(userId).ifPresent(user -> 
                    readerUsernames.add(user.getUsername()));
            }
        }
        
        if (note.getWriters() != null) {
            for (Long userId : note.getWriters()) {
                userRepository.findById(userId).ifPresent(user -> 
                    writerUsernames.add(user.getUsername()));
            }
        }
        
        return new NotePermissionsResponse(noteId, note.getCreatorId(),
                readerUsernames, writerUsernames);
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

    /**
     * Allow a user to remove themselves from a shared note
     * This removes the user from both readers and writers arrays
     */
    @Transactional
    public void leaveSharedNote(Long noteId) {
        User currentUser = authenticationService.getCurrentUser();
        
        // Check if user has access to the note
        Note note = noteRepository.findByIdWithReadPermission(noteId, currentUser.getId())
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId + " for current user"));
        
        // Prevent creator from removing themselves
        if (note.getCreatorId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Note creators cannot remove themselves from their own notes");
        }
        
        // Remove user from both readers and writers arrays
        note.removeReader(currentUser.getId());
        note.removeWriter(currentUser.getId());
        
        noteRepository.save(note);
    }

    // Helper methods
    
    /**
     * Convert Note entity to NoteResponse DTO with permission flags
     */
    private NoteResponse convertToNoteResponse(Note note, Long currentUserId) {
        boolean isOwner = note.getCreatorId().equals(currentUserId);
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

        var tagDtos = note.getTags() == null ? List.<TagDTO>of()
            : note.getTags().stream()
                .map(t -> new TagDTO(t.getId(), t.getName()))
                .sorted(Comparator.comparing(TagDTO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        response.setTags(tagDtos);
        
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
