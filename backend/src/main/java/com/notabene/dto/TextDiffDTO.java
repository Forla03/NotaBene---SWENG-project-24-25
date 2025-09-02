package com.notabene.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for representing text differences with highlighting
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextDiffDTO {
    
    private String leftText;  // Versione originale (sinistra)
    private String rightText; // Versione nuova (destra)
    private List<DiffSegment> leftSegments;  // Segmenti con evidenziazioni per il testo di sinistra
    private List<DiffSegment> rightSegments; // Segmenti con evidenziazioni per il testo di destra
    
    /**
     * Check if there are changes (additions or removals) in the diff
     */
    public boolean hasChanges() {
        if (leftSegments == null && rightSegments == null) {
            return false;
        }
        
        // Controlla se ci sono segmenti ADDED o REMOVED nei segmenti destri
        if (rightSegments != null) {
            return rightSegments.stream()
                    .anyMatch(segment -> segment.getType() == DiffType.ADDED || 
                                       segment.getType() == DiffType.REMOVED);
        }
        
        // Controlla se ci sono segmenti REMOVED nei segmenti sinistri
        if (leftSegments != null) {
            return leftSegments.stream()
                    .anyMatch(segment -> segment.getType() == DiffType.REMOVED);
        }
        
        return false;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiffSegment {
        private String text;
        private DiffType type;
    }
    
    public enum DiffType {
        EQUAL,    // Testo identico (nessuna evidenziazione)
        ADDED,    // Testo aggiunto (verde)
        REMOVED   // Testo rimosso (rosso)
    }
}
