package com.notabene.repository;

import com.notabene.entity.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {
    
    /**
     * Find all versions for a specific note, ordered by version number descending (newest first)
     */
    List<NoteVersion> findByNoteIdOrderByVersionNumberDesc(Long noteId);
    
    /**
     * Find all versions for a specific note, ordered by version number ascending (oldest first)
     */
    List<NoteVersion> findByNoteIdOrderByVersionNumberAsc(Long noteId);
    
    /**
     * Find a specific version of a note
     */
    Optional<NoteVersion> findByNoteIdAndVersionNumber(Long noteId, Integer versionNumber);
    
    /**
     * Count total versions for a specific note
     */
    Long countByNoteId(Long noteId);
    
    /**
     * Delete all versions for a specific note (useful when note is deleted)
     */
    void deleteByNoteId(Long noteId);
}
