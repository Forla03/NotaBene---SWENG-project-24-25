package com.notabene.service;

import com.notabene.dto.TextDiffDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Text Diff Service Tests")
class TextDiffServiceTest {

    @InjectMocks
    private TextDiffService textDiffService;

    @Test
    @DisplayName("Should calculate diff for identical texts")
    void shouldCalculateDiffForIdenticalTexts() {
        // Given
        String text1 = "Hello World";
        String text2 = "Hello World";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals(text1, result.getLeftText());
        assertEquals(text2, result.getRightText());
        assertNotNull(result.getLeftSegments());
        assertNotNull(result.getRightSegments());
        
        // Verify all segments are marked as EQUAL
        for (TextDiffDTO.DiffSegment segment : result.getLeftSegments()) {
            assertEquals(TextDiffDTO.DiffType.EQUAL, segment.getType());
        }
        for (TextDiffDTO.DiffSegment segment : result.getRightSegments()) {
            assertEquals(TextDiffDTO.DiffType.EQUAL, segment.getType());
        }
    }

    @Test
    @DisplayName("Should calculate diff for completely different texts")
    void shouldCalculateDiffForCompletelyDifferentTexts() {
        // Given
        String text1 = "ABC";
        String text2 = "XYZ";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals(text1, result.getLeftText());
        assertEquals(text2, result.getRightText());
        
        // Should have segments for removed and added text
        boolean hasRemovedSegments = result.getLeftSegments().stream()
                .anyMatch(segment -> segment.getType() == TextDiffDTO.DiffType.REMOVED);
        boolean hasAddedSegments = result.getRightSegments().stream()
                .anyMatch(segment -> segment.getType() == TextDiffDTO.DiffType.ADDED);
        
        assertTrue(hasRemovedSegments || hasAddedSegments);
    }

    @Test
    @DisplayName("Should calculate diff for text with additions")
    void shouldCalculateDiffForTextWithAdditions() {
        // Given
        String text1 = "Hello";
        String text2 = "Hello World";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals(text1, result.getLeftText());
        assertEquals(text2, result.getRightText());
        
        // Should detect the addition
        boolean hasAddedSegments = result.getRightSegments().stream()
                .anyMatch(segment -> segment.getType() == TextDiffDTO.DiffType.ADDED);
        assertTrue(hasAddedSegments);
    }

    @Test
    @DisplayName("Should calculate diff for text with deletions")
    void shouldCalculateDiffForTextWithDeletions() {
        // Given
        String text1 = "Hello World";
        String text2 = "Hello";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals(text1, result.getLeftText());
        assertEquals(text2, result.getRightText());
        
        // Should detect the deletion
        boolean hasRemovedSegments = result.getLeftSegments().stream()
                .anyMatch(segment -> segment.getType() == TextDiffDTO.DiffType.REMOVED);
        assertTrue(hasRemovedSegments);
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        // Given & When & Then
        TextDiffDTO result1 = textDiffService.calculateDiff(null, "test");
        assertNotNull(result1);
        assertEquals("", result1.getLeftText());
        assertEquals("test", result1.getRightText());

        TextDiffDTO result2 = textDiffService.calculateDiff("test", null);
        assertNotNull(result2);
        assertEquals("test", result2.getLeftText());
        assertEquals("", result2.getRightText());

        TextDiffDTO result3 = textDiffService.calculateDiff(null, null);
        assertNotNull(result3);
        assertEquals("", result3.getLeftText());
        assertEquals("", result3.getRightText());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void shouldHandleEmptyStrings() {
        // Given
        String text1 = "";
        String text2 = "test";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals("", result.getLeftText());
        assertEquals("test", result.getRightText());
        assertNotNull(result.getLeftSegments());
        assertNotNull(result.getRightSegments());
    }

    @Test
    @DisplayName("Should optimize segments correctly")
    void shouldOptimizeSegmentsCorrectly() {
        // Given
        List<TextDiffDTO.DiffSegment> segments = new ArrayList<>();
        segments.add(new TextDiffDTO.DiffSegment("A", TextDiffDTO.DiffType.EQUAL));
        segments.add(new TextDiffDTO.DiffSegment("B", TextDiffDTO.DiffType.EQUAL));
        segments.add(new TextDiffDTO.DiffSegment("C", TextDiffDTO.DiffType.ADDED));
        segments.add(new TextDiffDTO.DiffSegment("D", TextDiffDTO.DiffType.ADDED));

        // When
        textDiffService.optimizeSegments(segments);

        // Then
        assertEquals(2, segments.size());
        assertEquals("AB", segments.get(0).getText());
        assertEquals(TextDiffDTO.DiffType.EQUAL, segments.get(0).getType());
        assertEquals("CD", segments.get(1).getText());
        assertEquals(TextDiffDTO.DiffType.ADDED, segments.get(1).getType());
    }

    @Test
    @DisplayName("Should handle single segment optimization")
    void shouldHandleSingleSegmentOptimization() {
        // Given
        List<TextDiffDTO.DiffSegment> segments = new ArrayList<>();
        segments.add(new TextDiffDTO.DiffSegment("A", TextDiffDTO.DiffType.EQUAL));

        // When
        textDiffService.optimizeSegments(segments);

        // Then
        assertEquals(1, segments.size());
        assertEquals("A", segments.get(0).getText());
        assertEquals(TextDiffDTO.DiffType.EQUAL, segments.get(0).getType());
    }

    @Test
    @DisplayName("Should handle empty segment list")
    void shouldHandleEmptySegmentList() {
        // Given
        List<TextDiffDTO.DiffSegment> segments = new ArrayList<>();

        // When
        textDiffService.optimizeSegments(segments);

        // Then
        assertEquals(0, segments.size());
    }

    @Test
    @DisplayName("Should calculate diff for complex text changes")
    void shouldCalculateDiffForComplexTextChanges() {
        // Given
        String text1 = "The quick brown fox";
        String text2 = "The fast brown dog";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals(text1, result.getLeftText());
        assertEquals(text2, result.getRightText());
        assertNotNull(result.getLeftSegments());
        assertNotNull(result.getRightSegments());
        
        // Should have some common parts (The, brown) and some different parts
        boolean hasEqualSegments = result.getLeftSegments().stream()
                .anyMatch(segment -> segment.getType() == TextDiffDTO.DiffType.EQUAL);
        assertTrue(hasEqualSegments);
    }

    @Test
    @DisplayName("Should calculate diff for character-level changes")
    void shouldCalculateDiffForCharacterLevelChanges() {
        // Given
        String text1 = "cat";
        String text2 = "car";

        // When
        TextDiffDTO result = textDiffService.calculateDiff(text1, text2);

        // Then
        assertNotNull(result);
        assertEquals(text1, result.getLeftText());
        assertEquals(text2, result.getRightText());
        
        // Should detect character-level differences
        assertFalse(result.getLeftSegments().isEmpty());
        assertFalse(result.getRightSegments().isEmpty());
    }
}
