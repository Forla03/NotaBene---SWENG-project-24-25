package com.notabene.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.notabene.dto.FolderDtos.CreateFolderRequest;
import com.notabene.dto.FolderDtos.FolderDetail;
import com.notabene.dto.FolderDtos.FolderNoteRef;
import com.notabene.dto.FolderDtos.FolderSummary;
import com.notabene.entity.Folder;
import com.notabene.entity.FolderNote;
import com.notabene.model.FolderNoteId;
import com.notabene.repository.FolderNoteRepository;
import com.notabene.repository.FolderRepository;
import com.notabene.service.support.CurrentUserResolver;
import com.notabene.service.support.NoteOwnershipChecker;

@Service
public class FolderService {

    private final FolderRepository folderRepo;
    private final FolderNoteRepository linkRepo;
    private final CurrentUserResolver current;
    private final NoteOwnershipChecker ownership;

    public FolderService(FolderRepository folderRepo, FolderNoteRepository linkRepo,
                         CurrentUserResolver current, NoteOwnershipChecker ownership) {
        this.folderRepo = folderRepo; this.linkRepo = linkRepo;
        this.current = current; this.ownership = ownership;
    }

    public List<FolderSummary> listMyFolders() {
        Long uid = current.currentUserId();
        return folderRepo.findAllByOwnerIdOrderByNameAsc(uid).stream()
                .map(f -> new FolderSummary(f.getId(), f.getName()))
                .toList();
    }

    public FolderSummary createFolder(CreateFolderRequest req) {
    Long uid = current.currentUserId();
    String name = req.name() == null ? "" : req.name().trim();

    if (name.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
    }

    if (folderRepo.existsByOwnerIdAndName(uid, name)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder name already exists");
    }

    try {
        Folder saved = folderRepo.save(new Folder(uid, name));
        return new FolderSummary(saved.getId(), saved.getName());
    } catch (DataIntegrityViolationException e) {
        // rete di sicurezza per race condition / vincoli DB
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder name already exists");
    }
}


    public FolderDetail getFolder(Long folderId) {
        Long uid = current.currentUserId();
        Folder f = folderRepo.findByIdAndOwnerId(folderId, uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        var notes = linkRepo.findAllById_FolderId(f.getId()).stream()
                .map(fn -> new FolderNoteRef(fn.getId().getNoteId()))
                .toList();
        return new FolderDetail(f.getId(), f.getName(), notes);
    }

    public void deleteFolder(Long folderId) {
        Long uid = current.currentUserId();
        Folder f = folderRepo.findByIdAndOwnerId(folderId, uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        folderRepo.delete(f); // ON DELETE CASCADE rimuove i link
    }

    public void addNote(Long folderId, Long noteId) {
        Long uid = current.currentUserId();
        if (!folderRepo.existsByIdAndOwnerId(folderId, uid))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Folder not yours");

        if (!ownership.isOwnedByUser(noteId, uid))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only add your own note");

        FolderNoteId id = new FolderNoteId(folderId, noteId);
        if (linkRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Note already in folder");

        linkRepo.save(new FolderNote(id));
    }

    public void removeNote(Long folderId, Long noteId) {
        Long uid = current.currentUserId();
        if (!folderRepo.existsByIdAndOwnerId(folderId, uid))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Folder not yours");
        linkRepo.deleteById(new FolderNoteId(folderId, noteId));
    }
}

