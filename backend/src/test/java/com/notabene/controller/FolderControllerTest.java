package com.notabene.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notabene.config.TokenAuthenticationFilter;
import com.notabene.dto.FolderDtos.CreateFolderRequest;
import com.notabene.dto.FolderDtos.FolderDetail;
import com.notabene.dto.FolderDtos.FolderNoteRef;
import com.notabene.dto.FolderDtos.FolderSummary;
import com.notabene.service.FolderService;
import com.notabene.service.NoteService;

import jakarta.annotation.Resource;

@WebMvcTest(value = FolderController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TokenAuthenticationFilter.class))
class FolderControllerTest {

    @Resource MockMvc mvc;
    @Resource ObjectMapper om;

    @MockBean FolderService service;
    @MockBean NoteService noteService;

    @Test
    @WithMockUser
    void list_returns_folders() throws Exception {
        when(service.listMyFolders()).thenReturn(List.of(
                new FolderSummary(1L,"Ideas"),
                new FolderSummary(2L,"Work")
        ));

        mvc.perform(get("/api/folders"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].name").value("Ideas"));
    }

    @Test
    @WithMockUser
    void create_returns_201() throws Exception {
        var body = new CreateFolderRequest("Ideas");
        when(service.createFolder(any())).thenReturn(new FolderSummary(10L,"Ideas"));

        mvc.perform(post("/api/folders")
             .with(csrf())
             .contentType(MediaType.APPLICATION_JSON)
             .content(om.writeValueAsString(body)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithMockUser
    void get_folder_includes_note_ids() throws Exception {
        when(service.getFolder(9L)).thenReturn(
            new FolderDetail(9L,"Ideas", List.of(new FolderNoteRef(77L)))
        );

        mvc.perform(get("/api/folders/9"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.notes[0].id").value(77));
    }
}
