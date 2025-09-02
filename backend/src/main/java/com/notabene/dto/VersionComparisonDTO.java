package com.notabene.dto;

import com.notabene.entity.NoteVersion;
import lombok.Data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO for comparing two versions of a note
 */
@Data
public class VersionComparisonDTO {
    
    private final NoteVersion oldVersion;
    private final NoteVersion newVersion;
    
    public VersionComparisonDTO(NoteVersion oldVersion, NoteVersion newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }
    
    /**
     * Check if the title changed between versions
     */
    public boolean hasTitleChanged() {
        return !Objects.equals(oldVersion.getTitle(), newVersion.getTitle());
    }
    
    /**
     * Check if the content changed between versions
     */
    public boolean hasContentChanged() {
        return !Objects.equals(oldVersion.getContent(), newVersion.getContent());
    }
    
    /**
     * Check if permissions changed between versions
     */
    public boolean hasPermissionChanged() {
        return !Objects.equals(oldVersion.getReaders(), newVersion.getReaders()) ||
               !Objects.equals(oldVersion.getWriters(), newVersion.getWriters());
    }
    
    /**
     * Check if any changes occurred between versions
     */
    public boolean hasAnyChanges() {
        return hasTitleChanged() || hasContentChanged() || hasPermissionChanged();
    }
    
    /**
     * Check if the new version is actually newer
     */
    public boolean isNewerVersion() {
        return newVersion.getVersionNumber() > oldVersion.getVersionNumber();
    }
    
    /**
     * Get time difference between versions in minutes
     */
    public long getTimeDifferenceInMinutes() {
        if (oldVersion.getCreatedAt() == null || newVersion.getCreatedAt() == null) {
            return 0;
        }
        
        Duration duration = Duration.between(oldVersion.getCreatedAt(), newVersion.getCreatedAt());
        return Math.abs(duration.toMinutes());
    }
    
    /**
     * Get a summary of changes between versions
     */
    public String getChangeSummary() {
        List<String> changes = new ArrayList<>();
        
        if (hasTitleChanged()) {
            changes.add("title");
        }
        
        if (hasContentChanged()) {
            changes.add("content");
        }
        
        if (hasPermissionChanged()) {
            changes.add("permissions");
        }
        
        if (changes.isEmpty()) {
            return "No changes detected";
        }
        
        return "Changed: " + String.join(", ", changes);
    }
    
    /**
     * Get diff statistics for content changes
     */
    public DiffStatistics getDiffStatistics() {
        if (!hasContentChanged()) {
            return new DiffStatistics(0, 0, 0);
        }
        
        String[] oldLines = oldVersion.getContent().split("\n");
        String[] newLines = newVersion.getContent().split("\n");
        
        // Simple diff calculation (can be enhanced with more sophisticated algorithms)
        int linesAdded = Math.max(0, newLines.length - oldLines.length);
        int linesRemoved = Math.max(0, oldLines.length - newLines.length);
        int linesModified = calculateModifiedLines(oldLines, newLines);
        
        return new DiffStatistics(linesAdded, linesRemoved, linesModified);
    }
    
    /**
     * Simple calculation of modified lines
     */
    private int calculateModifiedLines(String[] oldLines, String[] newLines) {
        int minLength = Math.min(oldLines.length, newLines.length);
        int modified = 0;
        
        for (int i = 0; i < minLength; i++) {
            if (!Objects.equals(oldLines[i], newLines[i])) {
                modified++;
            }
        }
        
        return modified;
    }
    
    /**
     * Inner class for diff statistics
     */
    @Data
    public static class DiffStatistics {
        private final int linesAdded;
        private final int linesRemoved;
        private final int linesModified;
        
        public DiffStatistics(int linesAdded, int linesRemoved, int linesModified) {
            this.linesAdded = linesAdded;
            this.linesRemoved = linesRemoved;
            this.linesModified = linesModified;
        }
    }
}
