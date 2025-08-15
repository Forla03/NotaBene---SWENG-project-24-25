package com.notabene.entity;

import com.notabene.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
public class Note {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 280, message = "Content cannot exceed 280 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Permission fields - PostgreSQL arrays for optimized storage
    @NotNull(message = "Creator ID cannot be null")
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "readers", columnDefinition = "bigint[]")
    private List<Long> readers = new ArrayList<>();
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "writers", columnDefinition = "bigint[]")
    private List<Long> writers = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        // Initialize permission arrays and set creator permissions
        if (this.readers == null) {
            this.readers = new ArrayList<>();
        }
        if (this.writers == null) {
            this.writers = new ArrayList<>();
        }
        
        // Ensure creator has read and write permissions
        if (this.user != null && this.creatorId == null) {
            this.creatorId = this.user.getId();
        }
        
        if (this.creatorId != null) {
            if (!this.readers.contains(this.creatorId)) {
                this.readers.add(this.creatorId);
            }
            if (!this.writers.contains(this.creatorId)) {
                this.writers.add(this.creatorId);
            }
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor for tests and manual creation
    public Note(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.creatorId = user != null ? user.getId() : null;
        this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();
        
        // Ensure creator has permissions
        if (this.creatorId != null) {
            this.readers.add(this.creatorId);
            this.writers.add(this.creatorId);
        }
    }
    
    // Constructor for backward compatibility (deprecated)
    @Deprecated
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.readers = new ArrayList<>();
        this.writers = new ArrayList<>();
    }
    
    // Helper methods for permission management
    
    /**
     * Add a user to the readers list if not already present
     */
    public void addReader(Long userId) {
        if (userId != null && !this.readers.contains(userId)) {
            this.readers.add(userId);
        }
    }
    
    /**
     * Add a user to the writers list if not already present
     */
    public void addWriter(Long userId) {
        if (userId != null && !this.writers.contains(userId)) {
            this.writers.add(userId);
        }
    }
    
    /**
     * Remove a user from readers list (except creator)
     */
    public void removeReader(Long userId) {
        if (userId != null && !userId.equals(this.creatorId)) {
            this.readers.remove(userId);
        }
    }
    
    /**
     * Remove a user from writers list (except creator)
     */
    public void removeWriter(Long userId) {
        if (userId != null && !userId.equals(this.creatorId)) {
            this.writers.remove(userId);
        }
    }
    
    /**
     * Check if a user has read permission
     */
    public boolean hasReadPermission(Long userId) {
        return userId != null && this.readers.contains(userId);
    }
    
    /**
     * Check if a user has write permission
     */
    public boolean hasWritePermission(Long userId) {
        return userId != null && this.writers.contains(userId);
    }
    
    /**
     * Check if a user is the creator
     */
    public boolean isCreator(Long userId) {
        return userId != null && userId.equals(this.creatorId);
    }
}
