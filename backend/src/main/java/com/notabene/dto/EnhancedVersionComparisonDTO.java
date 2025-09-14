package com.notabene.dto;

import com.notabene.entity.NoteVersion;
import lombok.Data;

/**
 * Enhanced DTO for comparing two versions with detailed text differences
 */
@Data
public class EnhancedVersionComparisonDTO {
    
    private final NoteVersion leftVersion;   // Versione selezionata prima (sinistra)
    private final NoteVersion rightVersion;  // Versione selezionata dopo (destra)
    
    private final TextDiffDTO titleDiff;     // Differenze nel titolo
    private final TextDiffDTO contentDiff;   // Differenze nel contenuto
    
    private final boolean hasChanges;        // Se ci sono differenze
    
    public EnhancedVersionComparisonDTO(NoteVersion leftVersion, NoteVersion rightVersion, 
                                       TextDiffDTO titleDiff, TextDiffDTO contentDiff) {
        this.leftVersion = leftVersion;
        this.rightVersion = rightVersion;
        this.titleDiff = titleDiff;
        this.contentDiff = contentDiff;
        
        // Determine if there are changes by checking for ADDED or REMOVED segments
        this.hasChanges = hasSegmentChanges(titleDiff) || hasSegmentChanges(contentDiff);
    }
    
    private boolean hasSegmentChanges(TextDiffDTO diff) {
        if (diff == null || diff.getRightSegments() == null) {
            return false;
        }
        
        return diff.getRightSegments().stream()
                .anyMatch(segment -> segment.getType() == TextDiffDTO.DiffType.ADDED || 
                                   segment.getType() == TextDiffDTO.DiffType.REMOVED);
    }
}
