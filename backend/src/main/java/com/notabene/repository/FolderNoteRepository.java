package com.notabene.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notabene.entity.FolderNote;
import com.notabene.model.FolderNoteId;

public interface FolderNoteRepository extends JpaRepository<FolderNote, FolderNoteId> {
    List<FolderNote> findAllById_FolderId(Long folderId);
}

