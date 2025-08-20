package com.notabene.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.notabene.dto.FolderDtos.CreateFolderRequest;
import com.notabene.dto.FolderDtos.FolderDetail;
import com.notabene.dto.FolderDtos.FolderSummary;
import com.notabene.service.FolderService;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService service;

    public FolderController(FolderService service) { this.service = service; }

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
}
