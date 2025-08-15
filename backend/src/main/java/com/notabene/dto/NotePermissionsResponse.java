package com.notabene.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NotePermissionsResponse {
    private Long noteId;
    private Long creatorId;
    private List<Long> readers;
    private List<Long> writers;
    
    public NotePermissionsResponse(Long noteId, Long creatorId, List<Long> readers, List<Long> writers) {
        this.noteId = noteId;
        this.creatorId = creatorId;
        this.readers = readers;
        this.writers = writers;
    }
}
