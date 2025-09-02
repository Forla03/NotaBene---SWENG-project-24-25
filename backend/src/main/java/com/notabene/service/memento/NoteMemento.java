package com.notabene.service.memento;

import com.notabene.model.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Memento class that stores the state of a Note at a specific point in time.
 * This is part of the Memento pattern implementation for note versioning.
 */
@Data
public class NoteMemento {
    
    private final String title;
    private final String content;
    private final Long creatorId;
    private final List<Long> readers;
    private final List<Long> writers;
    private final Set<Tag> tags;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime snapshotTime;
    
    /**
     * Create a memento with deep copies of mutable collections
     */
    public NoteMemento(String title, String content, Long creatorId, 
                      List<Long> readers, List<Long> writers, Set<Tag> tags,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.content = content;
        this.creatorId = creatorId;
        
        // Create deep copies to ensure immutability
        this.readers = readers != null ? new ArrayList<>(readers) : new ArrayList<>();
        this.writers = writers != null ? new ArrayList<>(writers) : new ArrayList<>();
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
        
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.snapshotTime = LocalDateTime.now();
    }
    
    /**
     * Get readers list (returns defensive copy)
     */
    public List<Long> getReaders() {
        return new ArrayList<>(readers);
    }
    
    /**
     * Get writers list (returns defensive copy)
     */
    public List<Long> getWriters() {
        return new ArrayList<>(writers);
    }
    
    /**
     * Get tags set (returns defensive copy)
     */
    public Set<Tag> getTags() {
        return new HashSet<>(tags);
    }
}
