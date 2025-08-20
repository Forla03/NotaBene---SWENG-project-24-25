package com.notabene.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notabene.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    // ricerca case-insensitive + ordine alfabetico
    List<Tag> findByNameContainingIgnoreCaseOrderByNameAsc(String q);

    // elenco “top N” alfabetico quando non c’è query
    List<Tag> findTop20ByOrderByNameAsc();
}

