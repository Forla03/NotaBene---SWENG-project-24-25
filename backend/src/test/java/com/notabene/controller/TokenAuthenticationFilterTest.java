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

import com.notabene.config.TokenStore;
import com.notabene.model.User;
import com.notabene.service.AuthenticationService;
import com.notabene.service.NoteService;

@WebMvcTest(controllers = NoteController.class)
@Import(com.notabene.config.TestSecurityConfig.class)
class TokenAuthenticationFilterTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TokenStore tokenStore;              // usato dal filtro

    @MockBean
    AuthenticationService authenticationService; // usato dal filtro/contesto auth

    @MockBean
    NoteService noteService;            // dipendenza del controller

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/notes").accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        // 1) token valido
        when(tokenStore.isValid("abc123")).thenReturn(true);
        when(tokenStore.getUsername("abc123")).thenReturn("Mario");

        // 2) utente di dominio (NON UserDetails di Spring!)
        User domainUser = new User();
        domainUser.setId(1L);
        domainUser.setUsername("Mario");
        // imposta altri campi se il tuo AuthenticationService/Controller li usa

        when(authenticationService.getCurrentUser()).thenReturn(domainUser);

        mockMvc.perform(get("/api/notes")
                .header("X-Auth-Token", "abc123")
                .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }
}


