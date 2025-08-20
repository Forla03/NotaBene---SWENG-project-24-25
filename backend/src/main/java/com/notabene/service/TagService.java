package com.notabene.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notabene.dto.TagDTO;
import com.notabene.model.Tag;
import com.notabene.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {
    
    private final TagRepository repo;

    @Transactional
    public TagDTO create(String rawName, Long createdBy) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Il nome del tag è obbligatorio");
        }
        Tag t = new Tag();
        t.setName(name);
        t.setCreatedBy(createdBy);
        try {
            t = repo.saveAndFlush(t);
        } catch (DataIntegrityViolationException e) {
            // vincolo UNIQUE (citext) violato
            throw new DuplicateTagException(name);
        }
        return new TagDTO(t.getId(), t.getName());
    }

    @Transactional(readOnly = true)
    public List<TagDTO> list(String q) {
        List<Tag> items = (q == null || q.isBlank())
                ? repo.findTop20ByOrderByNameAsc()
                : repo.findByNameContainingIgnoreCaseOrderByNameAsc(q.trim());
        return items.stream().map(t -> new TagDTO(t.getId(), t.getName())).toList();
    }

    public static class DuplicateTagException extends RuntimeException {
        public DuplicateTagException(String name) {
            super("Tag già esistente: " + name);
        }
    }
}


