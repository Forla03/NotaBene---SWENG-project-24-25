package com.notabene.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteVersionDTO {
    private Long id;
    private Long noteId;
    private Integer versionNumber;
    private String title;
    private String content;
    private List<Long> readers;
    private List<Long> writers;
    private Long createdBy;
    private String createdByUsername; // Nome dell'utente che ha creato la versione
    private Long noteCreatorId;
    private LocalDateTime createdAt;
    private LocalDateTime originalCreatedAt;
    private LocalDateTime originalUpdatedAt;
    private Boolean isRestored;
    private Integer restoredFromVersion;
}
