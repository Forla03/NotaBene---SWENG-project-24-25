package com.notabene.repository;

import com.notabene.entity.Note;
import com.notabene.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    
    // Find notes by user
    List<Note> findByUserOrderByCreatedAtDesc(User user);
    
    // Find note by id and user (for security)
    Optional<Note> findByIdAndUser(Long id, User user);
    
    // Find notes by title containing a specific string for a specific user
    List<Note> findByUserAndTitleContainingIgnoreCase(User user, String title);
    
    // Find notes by content containing a specific string for a specific user
    List<Note> findByUserAndContentContainingIgnoreCase(User user, String content);
    
    // Search notes by title or content for a specific user
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (" +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Note> searchNotesByUser(@Param("user") User user, @Param("search") String search);
    
    // Get notes with pagination for a specific user
    Page<Note> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Legacy methods (deprecated - should use user-specific methods)
    @Deprecated
    List<Note> findByTitleContainingIgnoreCase(String title);
    
    @Deprecated
    List<Note> findByContentContainingIgnoreCase(String content);
    
    @Deprecated
    @Query("SELECT n FROM Note n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Note> searchNotes(@Param("search") String search);
    
    @Deprecated
    List<Note> findAllByOrderByCreatedAtDesc();
    
    @Deprecated
    Page<Note> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
