package com.notabene.dto;

import com.notabene.entity.Note;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NoteResponse {
    private Long id;
    private String title;
    private String content;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public NoteResponse(Note note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.priority = note.getPriority();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
    }
    
    public static NoteResponse fromEntity(Note note) {
        return new NoteResponse(note);
    }
}
