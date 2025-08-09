package com.notabene.repository;

import com.notabene.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    // Find notes by title containing a specific string (case-insensitive)
    List<Note> findByTitleContainingIgnoreCase(String title);
    
    // Find notes by content containing a specific string (case-insensitive)
    List<Note> findByContentContainingIgnoreCase(String content);
    
    // Find notes by priority
    List<Note> findByPriority(Integer priority);
    
    // Find notes by priority greater than or equal to a value
    List<Note> findByPriorityGreaterThanEqual(Integer priority);
    
    // Search notes by title or content (case-insensitive)
    @Query("SELECT n FROM Note n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Note> searchNotes(@Param("search") String search);
    
    // Get notes ordered by priority descending and creation date descending
    List<Note> findAllByOrderByPriorityDescCreatedAtDesc();
    
    // Get all notes ordered by creation date descending
    List<Note> findAllByOrderByCreatedAtDesc();
    
    // Get notes with pagination ordered by creation date descending
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
