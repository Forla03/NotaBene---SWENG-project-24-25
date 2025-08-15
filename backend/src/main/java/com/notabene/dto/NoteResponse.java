package com.notabene.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.notabene.entity.Note;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class NoteResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Permission fields
    private Long creatorId;
    private List<String> readers;
    private List<String> writers;
    
    // UI permission flags
    @JsonProperty("isOwner")
    private boolean isOwner;
    @JsonProperty("canEdit")
    private boolean canEdit;
    @JsonProperty("canDelete")
    private boolean canDelete;
    @JsonProperty("canShare")
    private boolean canShare;
    
    public NoteResponse(Note note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
        this.creatorId = note.getCreatorId();
        // readers and writers will be set separately with usernames
        // Default permission flags - should be set explicitly via setters
        this.isOwner = false;
        this.canEdit = false;
        this.canDelete = false;
        this.canShare = false;
    }
    
    public static NoteResponse fromEntity(Note note) {
        return new NoteResponse(note);
    }
    
    // Manual setter for isOwner (in case Lombok doesn't generate it)
    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
    
    public boolean getIsOwner() {
        return this.isOwner;
    }
}
