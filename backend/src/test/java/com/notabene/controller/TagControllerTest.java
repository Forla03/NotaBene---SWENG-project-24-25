package com.notabene.controller;

import java.util.List;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.notabene.config.SecurityConfig;
import com.notabene.config.TokenAuthenticationFilter;
import com.notabene.dto.TagDTO;
import com.notabene.service.TagService;


@Import(com.notabene.exception.GlobalExceptionHandler.class)
@WebMvcTest(
  controllers = TagController.class,
  excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = { TokenAuthenticationFilter.class, SecurityConfig.class }
      )
  }
)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TagService service;

    @Test
    void post_create_returns201_andBody() throws Exception {
        Mockito.when(service.create(eq("Lavoro"), any()))
               .thenReturn(new TagDTO(1L, "Lavoro"));

        mvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Lavoro\"}"))
           .andExpect(status().isCreated())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.id", is(1)))          // id numerico
           .andExpect(jsonPath("$.name", is("Lavoro")));
    }

    @Test
    void get_list_noQuery_returns200() throws Exception {
        Mockito.when(service.list(isNull()))
               .thenReturn(List.of(new TagDTO(2L, "Casa")));

        mvc.perform(get("/api/tags"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name", is("Casa")));
    }

    @Test
    void get_list_withQuery_returns200() throws Exception {
        Mockito.when(service.list(eq("lav")))
               .thenReturn(List.of(new TagDTO(1L, "Lavoro")));

        mvc.perform(get("/api/tags?q=lav"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].name", is("Lavoro")));
    }

    @Test
    void post_duplicate_returns409() throws Exception {
        Mockito.when(service.create(eq("Lavoro"), any()))
               .thenThrow(new TagService.DuplicateTagException("Lavoro"));

        mvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Lavoro\"}"))
           .andExpect(status().isConflict());
    }
}


