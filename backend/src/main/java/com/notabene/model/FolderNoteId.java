package com.notabene.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class FolderNoteId implements Serializable {
    private Long folderId;
    private Long noteId;

    public FolderNoteId() {}
    public FolderNoteId(Long folderId, Long noteId){ this.folderId=folderId; this.noteId=noteId; }

    public Long getFolderId(){ return folderId; }
    public Long getNoteId(){ return noteId; }

    @Override public boolean equals(Object o){
        if (this==o) return true; if (!(o instanceof FolderNoteId id)) return false;
        return Objects.equals(folderId,id.folderId) && Objects.equals(noteId,id.noteId);
    }
    @Override public int hashCode(){ return Objects.hash(folderId,noteId); }
}

