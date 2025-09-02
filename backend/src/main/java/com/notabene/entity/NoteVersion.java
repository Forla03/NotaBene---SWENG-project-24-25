package com.notabene.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "note_versions")
@Data
@NoArgsConstructor
public class NoteVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Note ID cannot be null")
    @Column(name = "note_id", nullable = false)
    private Long noteId;
    
    @NotNull(message = "Version number cannot be null")
    @Positive(message = "Version number must be positive")
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;
    
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Content cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    // Permission arrays - snapshot of the note's permissions at the time of versioning
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "readers", columnDefinition = "bigint[]")
    private List<Long> readers = new ArrayList<>();
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "writers", columnDefinition = "bigint[]")
    private List<Long> writers = new ArrayList<>();
    
    // Metadata about the version
    @NotNull(message = "Created by cannot be null")
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "note_creator_id", nullable = false)
    private Long noteCreatorId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Original timestamps from the note when this version was created
    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;
    
    @Column(name = "original_updated_at")
    private LocalDateTime originalUpdatedAt;
    
    // Restore information
    @Column(name = "is_restored", nullable = false)
    private Boolean isRestored = false;
    
    @Column(name = "restored_from_version")
    private Integer restoredFromVersion;
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isRestored == null) {
            this.isRestored = false;
        }
    }
}
