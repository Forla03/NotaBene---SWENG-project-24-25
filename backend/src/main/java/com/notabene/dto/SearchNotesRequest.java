package com.notabene.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for advanced note search requests
 */
@Data
@NoArgsConstructor
public class SearchNotesRequest {
    
    /**
     * General text search query (searches in title and content)
     */
    private String query;
    
    /**
     * Filter by tag names (case-insensitive)
     */
    private List<String> tags;
    
    /**
     * Filter by author/creator username (case-insensitive)
     */
    private String author;
    
    /**
     * Filter by creation date range
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAfter;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdBefore;
    
    /**
     * Filter by last modification date range
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAfter;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedBefore;
    
    /**
     * Folder ID for folder-specific search (optional)
     */
    private Long folderId;
    
    public SearchNotesRequest(String query) {
        this.query = query;
    }
}
