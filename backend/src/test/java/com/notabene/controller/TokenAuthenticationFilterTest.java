package com.notabene.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import java.util.List;

import com.notabene.config.SecurityConfig;
import com.notabene.config.TokenAuthenticationFilter;
import com.notabene.config.TokenStore;
import com.notabene.service.AuthenticationService;
import com.notabene.service.NoteService;

@WebMvcTest(controllers = NoteController.class)
@Import({ SecurityConfig.class, TokenAuthenticationFilter.class })
class TokenAuthenticationFilterTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TokenStore tokenStore;              

    @MockBean
    AuthenticationService authenticationService; 

    @MockBean
    NoteService noteService;            

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/notes").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized())
               .andExpect(unauthenticated());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        // 1) valid token
        when(tokenStore.isValid("abc123")).thenReturn(true);
        when(tokenStore.getUsername("abc123")).thenReturn("Mario");

        // 2) Mock the noteService to return empty list
        when(noteService.getAllNotes()).thenReturn(List.of());

        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", "abc123")
                .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }
}


