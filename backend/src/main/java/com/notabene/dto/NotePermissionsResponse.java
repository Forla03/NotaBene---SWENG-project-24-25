package com.notabene.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NotePermissionsResponse {
    private Long noteId;
    private Long creatorId;
    private List<String> readers;
    private List<String> writers;
    
    public NotePermissionsResponse(Long noteId, Long creatorId, List<String> readers, List<String> writers) {
        this.noteId = noteId;
        this.creatorId = creatorId;
        this.readers = readers;
        this.writers = writers;
    }
}
