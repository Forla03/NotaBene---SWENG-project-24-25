package com.notabene.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.notabene.config.TokenStore;

@WebMvcTest // qui puoi specificare un controller protetto, ad esempio NotesController
class TokenAuthenticationFilterTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TokenStore tokenStore;

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        when(tokenStore.isValid("abc123")).thenReturn(true);
        when(tokenStore.getUsername("abc123")).thenReturn("Mario");

        mockMvc.perform(get("/api/notes").header("X-Auth-Token", "abc123"))
                .andExpect(status().isOk());
    }
}
