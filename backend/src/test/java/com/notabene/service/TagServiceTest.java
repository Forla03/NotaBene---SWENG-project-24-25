package com.notabene.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.DataIntegrityViolationException;

import com.notabene.dto.TagDTO;
import com.notabene.model.Tag;
import com.notabene.repository.TagRepository;

class TagServiceTest {

    TagRepository repo = mock(TagRepository.class);
    TagService service = new TagService(repo);

    @Test
    void create_ok_returnsDTO() {
        Tag saved = new Tag();
        saved.setId(10L);
        saved.setName("Lavoro");

        when(repo.saveAndFlush(any(Tag.class))).thenReturn(saved);

        TagDTO dto = service.create(" Lavoro ", 1L);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("Lavoro");

        ArgumentCaptor<Tag> cap = ArgumentCaptor.forClass(Tag.class);
        verify(repo).saveAndFlush(cap.capture());
        assertThat(cap.getValue().getName()).isEqualTo("Lavoro");
        assertThat(cap.getValue().getCreatedBy()).isEqualTo(1L);
    }

    @Test
    void create_duplicate_throwsDomainException() {
        when(repo.saveAndFlush(any(Tag.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> service.create("Lavoro", null))
                .isInstanceOf(TagService.DuplicateTagException.class)
                .hasMessageContaining("Tag già esistente");

        verify(repo).saveAndFlush(any(Tag.class));
    }

    @Test
    void list_noQuery_returnsTop20_sorted() {
        Tag a = new Tag(); a.setId(1L); a.setName("a");
        Tag b = new Tag(); b.setId(2L); b.setName("b");

        when(repo.findTop20ByOrderByNameAsc()).thenReturn(List.of(a, b));

        var res = service.list(null);
        assertThat(res).extracting(TagDTO::name).containsExactly("a","b");
    }

    @Test
    void list_withQuery_usesRepoSearch_caseInsensitive() {
        Tag a = new Tag(); a.setId(1L); a.setName("Universita");
        when(repo.findByNameContainingIgnoreCaseOrderByNameAsc("uni")).thenReturn(List.of(a));

        var res = service.list(" uni ");
        assertThat(res).singleElement().extracting(TagDTO::name).isEqualTo("Universita");
    }
}

