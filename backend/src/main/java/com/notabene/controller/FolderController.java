package com.notabene.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.notabene.dto.FolderDtos.CreateFolderRequest;
import com.notabene.dto.FolderDtos.FolderDetail;
import com.notabene.dto.FolderDtos.FolderSummary;
import com.notabene.dto.NoteResponse;
import com.notabene.dto.SearchNotesRequest;
import com.notabene.service.FolderService;
import com.notabene.service.NoteService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/folders")
@Slf4j
public class FolderController {

    private final FolderService service;
    private final NoteService noteService;

    public FolderController(FolderService service, NoteService noteService) { 
        this.service = service; 
        this.noteService = noteService;
    }

    @GetMapping
    public List<FolderSummary> listMyFolders() { return service.listMyFolders(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderSummary create(@RequestBody CreateFolderRequest req) {
        return service.createFolder(req);
    }

    @GetMapping("/{id}")
    public FolderDetail get(@PathVariable Long id) { return service.getFolder(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.deleteFolder(id); }

    @PostMapping("/{folderId}/notes/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addNote(@PathVariable Long folderId, @PathVariable Long noteId) {
        service.addNote(folderId, noteId);
    }

    @DeleteMapping("/{folderId}/notes/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeNote(@PathVariable Long folderId, @PathVariable Long noteId) {
        service.removeNote(folderId, noteId);
    }
    
    /**
     * Search notes within a specific folder (GET with query parameters)
     */
    @GetMapping("/{folderId}/notes/search")
    public ResponseEntity<List<NoteResponse>> searchNotesInFolder(
            @PathVariable Long folderId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedBefore) {
        
        try {
            log.info("Searching notes in folder {} - query: {}, tags: {}, author: {}", folderId, query, tags, author);
            
            SearchNotesRequest request = new SearchNotesRequest();
            request.setQuery(query);
            if (tags != null && !tags.trim().isEmpty()) {
                request.setTags(Arrays.asList(tags.split(",")));
            }
            request.setAuthor(author);
            request.setCreatedAfter(createdAfter);
            request.setCreatedBefore(createdBefore);
            request.setUpdatedAfter(updatedAfter);
            request.setUpdatedBefore(updatedBefore);
            
            List<NoteResponse> notes = noteService.searchNotesInFolder(folderId, request);
            log.info("Found {} notes in folder {}", notes.size(), folderId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error searching notes in folder {} - query: {}", folderId, query, e);
            throw e;
        }
    }
    
    /**
     * Search notes within a specific folder (POST with request body)
     */
    @PostMapping("/{folderId}/notes/search")
    public ResponseEntity<List<NoteResponse>> searchNotesInFolderPost(
            @PathVariable Long folderId,
            @Valid @RequestBody SearchNotesRequest request) {
        
        try {
            log.info("Searching notes in folder {} with POST - query: {}, tags: {}, author: {}", 
                    folderId, request.getQuery(), request.getTags(), request.getAuthor());
            
            List<NoteResponse> notes = noteService.searchNotesInFolder(folderId, request);
            log.info("Found {} notes in folder {}", notes.size(), folderId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error searching notes in folder {} with POST - request: {}", folderId, request, e);
            throw e;
        }
    }
    
    /**
     * Fallback search endpoint for folder search (without /notes in path)
     * This handles the frontend call to /api/folders/{id}/search
     */
    @GetMapping("/{folderId}/search")
    public ResponseEntity<List<NoteResponse>> searchInFolder(
            @PathVariable Long folderId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedBefore) {
        
        try {
            log.info("Fallback search in folder {} - query: {}, tags: {}, author: {}", folderId, query, tags, author);
            
            SearchNotesRequest request = new SearchNotesRequest();
            request.setQuery(query);
            if (tags != null && !tags.trim().isEmpty()) {
                request.setTags(Arrays.asList(tags.split(",")));
            }
            request.setAuthor(author);
            request.setCreatedAfter(createdAfter);
            request.setCreatedBefore(createdBefore);
            request.setUpdatedAfter(updatedAfter);
            request.setUpdatedBefore(updatedBefore);
            
            List<NoteResponse> notes = noteService.searchNotesInFolder(folderId, request);
            log.info("Fallback search found {} notes in folder {}", notes.size(), folderId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error in fallback search for folder {} - query: {}", folderId, query, e);
            throw e;
        }
    }
    
    /**
     * Fallback search endpoint for folder search with POST (without /notes in path)
     */
    @PostMapping("/{folderId}/search")
    public ResponseEntity<List<NoteResponse>> searchInFolderPost(
            @PathVariable Long folderId,
            @Valid @RequestBody SearchNotesRequest request) {
        
        try {
            log.info("Fallback search in folder {} with POST - query: {}, tags: {}, author: {}", 
                    folderId, request.getQuery(), request.getTags(), request.getAuthor());
            
            List<NoteResponse> notes = noteService.searchNotesInFolder(folderId, request);
            log.info("Fallback search POST found {} notes in folder {}", notes.size(), folderId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error in fallback search POST for folder {} - request: {}", folderId, request, e);
            throw e;
        }
    }
}
