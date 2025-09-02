package com.notabene.service;

import com.notabene.dto.TextDiffDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating text differences character by character
 */
@Service
public class TextDiffService {
    
    /**
     * Calculate differences between two texts using a simple diff algorithm
     */
    public TextDiffDTO calculateDiff(String leftText, String rightText) {
        if (leftText == null) leftText = "";
        if (rightText == null) rightText = "";
        
        // Calcola i segmenti di differenza
        List<TextDiffDTO.DiffSegment> leftSegments = new ArrayList<>();
        List<TextDiffDTO.DiffSegment> rightSegments = new ArrayList<>();
        
        // Implementazione semplificata di diff - algoritmo Myers semplificato
        calculateDiffSegments(leftText, rightText, leftSegments, rightSegments);
        
        return new TextDiffDTO(leftText, rightText, leftSegments, rightSegments);
    }
    
    /**
     * Calcola i segmenti di differenza usando un algoritmo di programmazione dinamica
     */
    private void calculateDiffSegments(String left, String right, 
                                     List<TextDiffDTO.DiffSegment> leftSegments,
                                     List<TextDiffDTO.DiffSegment> rightSegments) {
        
        int leftLen = left.length();
        int rightLen = right.length();
        
        // Matrice DP per LCS (Longest Common Subsequence)
        int[][] dp = new int[leftLen + 1][rightLen + 1];
        
        // Riempi la matrice DP
        for (int i = 1; i <= leftLen; i++) {
            for (int j = 1; j <= rightLen; j++) {
                if (left.charAt(i - 1) == right.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        // Ricostruisci le differenze
        reconstructDiff(left, right, dp, leftLen, rightLen, leftSegments, rightSegments);
    }
    
    /**
     * Ricostruisce le differenze dalla matrice DP
     */
    private void reconstructDiff(String left, String right, int[][] dp, 
                               int i, int j,
                               List<TextDiffDTO.DiffSegment> leftSegments,
                               List<TextDiffDTO.DiffSegment> rightSegments) {
        
        if (i > 0 && j > 0 && left.charAt(i - 1) == right.charAt(j - 1)) {
            // Carattere uguale - ricorsione e poi aggiungi come EQUAL
            reconstructDiff(left, right, dp, i - 1, j - 1, leftSegments, rightSegments);
            
            String commonChar = String.valueOf(left.charAt(i - 1));
            leftSegments.add(new TextDiffDTO.DiffSegment(commonChar, TextDiffDTO.DiffType.EQUAL));
            rightSegments.add(new TextDiffDTO.DiffSegment(commonChar, TextDiffDTO.DiffType.EQUAL));
            
        } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
            // Carattere aggiunto in right
            reconstructDiff(left, right, dp, i, j - 1, leftSegments, rightSegments);
            
            String addedChar = String.valueOf(right.charAt(j - 1));
            rightSegments.add(new TextDiffDTO.DiffSegment(addedChar, TextDiffDTO.DiffType.ADDED));
            
        } else if (i > 0) {
            // Carattere rimosso da left
            reconstructDiff(left, right, dp, i - 1, j, leftSegments, rightSegments);
            
            String removedChar = String.valueOf(left.charAt(i - 1));
            leftSegments.add(new TextDiffDTO.DiffSegment(removedChar, TextDiffDTO.DiffType.REMOVED));
            rightSegments.add(new TextDiffDTO.DiffSegment(removedChar, TextDiffDTO.DiffType.REMOVED));
        }
    }
    
    /**
     * Ottimizza i segmenti combinando segmenti consecutivi dello stesso tipo
     */
    public void optimizeSegments(List<TextDiffDTO.DiffSegment> segments) {
        if (segments.size() <= 1) return;
        
        List<TextDiffDTO.DiffSegment> optimized = new ArrayList<>();
        TextDiffDTO.DiffSegment current = segments.get(0);
        
        for (int i = 1; i < segments.size(); i++) {
            TextDiffDTO.DiffSegment next = segments.get(i);
            
            if (current.getType() == next.getType()) {
                // Combina segmenti dello stesso tipo
                current = new TextDiffDTO.DiffSegment(
                    current.getText() + next.getText(), 
                    current.getType()
                );
            } else {
                optimized.add(current);
                current = next;
            }
        }
        optimized.add(current);
        
        segments.clear();
        segments.addAll(optimized);
    }
}
