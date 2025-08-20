package com.notabene.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notabene.entity.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByOwnerIdOrderByNameAsc(Long ownerId);
    Optional<Folder> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerIdAndName(Long ownerId, String name);
}

