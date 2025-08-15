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
    
    // Find notes by user (legacy - based on user relationship)
    List<Note> findByUserOrderByCreatedAtDesc(User user);
    
    // Find note by id and user (for security - legacy)
    Optional<Note> findByIdAndUser(Long id, User user);
    
    // Permission-based queries using PostgreSQL arrays
    
    // Find notes where user has read permission
    @Query(value = "SELECT * FROM notes n WHERE :userId = ANY(n.readers) ORDER BY n.created_at DESC", nativeQuery = true)
    List<Note> findByReadersContaining(@Param("userId") Long userId);
    
    // Find notes where user has write permission
    @Query(value = "SELECT * FROM notes n WHERE :userId = ANY(n.writers) ORDER BY n.created_at DESC", nativeQuery = true)
    List<Note> findByWritersContaining(@Param("userId") Long userId);
    
    // Find notes created by user
    @Query("SELECT n FROM Note n WHERE n.creatorId = :creatorId ORDER BY n.createdAt DESC")
    List<Note> findByCreatorId(@Param("creatorId") Long creatorId);
    
    // Find shared notes (where user is reader but not creator)
    @Query(value = "SELECT * FROM notes n WHERE :userId = ANY(n.readers) AND n.creator_id != :userId ORDER BY n.created_at DESC", nativeQuery = true)
    List<Note> findSharedWithUser(@Param("userId") Long userId);
    
    // Find note by id with read permission check
    @Query(value = "SELECT * FROM notes n WHERE n.id = :noteId AND :userId = ANY(n.readers)", nativeQuery = true)
    Optional<Note> findByIdWithReadPermission(@Param("noteId") Long noteId, @Param("userId") Long userId);
    
    // Find note by id with write permission check
    @Query(value = "SELECT * FROM notes n WHERE n.id = :noteId AND :userId = ANY(n.writers)", nativeQuery = true)
    Optional<Note> findByIdWithWritePermission(@Param("noteId") Long noteId, @Param("userId") Long userId);
    
    // Search notes with read permission
    @Query(value = "SELECT * FROM notes n WHERE :userId = ANY(n.readers) AND (" +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY n.created_at DESC", nativeQuery = true)
    List<Note> searchNotesWithReadPermission(@Param("userId") Long userId, @Param("search") String search);
    
    // Get paginated notes with read permission
    @Query(value = "SELECT * FROM notes n WHERE :userId = ANY(n.readers) ORDER BY n.created_at DESC", 
           countQuery = "SELECT count(*) FROM notes n WHERE :userId = ANY(n.readers)", 
           nativeQuery = true)
    Page<Note> findByReadersContaining(@Param("userId") Long userId, Pageable pageable);
    
    // Find notes by title containing a specific string for a specific user
    List<Note> findByUserAndTitleContainingIgnoreCase(User user, String title);
    
    // Find notes by content containing a specific string for a specific user
    List<Note> findByUserAndContentContainingIgnoreCase(User user, String content);
    
    // Search notes by title or content for a specific user (legacy)
    @Query("SELECT n FROM Note n WHERE n.user = :user AND (" +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Note> searchNotesByUser(@Param("user") User user, @Param("search") String search);
    
    // Get notes with pagination for a specific user (legacy)
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
