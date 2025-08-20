package com.notabene.entity;

import com.notabene.model.FolderNoteId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "folder_notes")
public class FolderNote {
    @EmbeddedId
    private FolderNoteId id;

    public FolderNote() {}
    public FolderNote(FolderNoteId id){ this.id = id; }

    public FolderNoteId getId(){ return id; }
}

