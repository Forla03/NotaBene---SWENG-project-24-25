package com.notabene.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.notabene.config.TokenStore;
import com.notabene.dto.LoginRequest;
import com.notabene.dto.RegisterRequest;
import com.notabene.model.User;
import com.notabene.repository.UserRepository;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private TokenStore tokenStore;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenStore = mock(TokenStore.class);
        userService = new UserService(userRepository, passwordEncoder, tokenStore);
    }

    @Test
    void register_newUser_savesUser() {
        RegisterRequest req = new RegisterRequest("Mario", "mario@example.com", "pwd");
        when(userRepository.existsByEmail("mario@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("hashed");

        userService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Mario", captor.getValue().getUsername());
        assertEquals("hashed", captor.getValue().getPassword());
    }

    @Test
    void register_existingEmail_throws() {
        when(userRepository.existsByEmail("mario@example.com")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("Mario", "mario@example.com", "pwd");

        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setUsername("Mario");
        user.setEmail("mario@example.com");
        user.setPassword("hashed");

        when(userRepository.findByEmail("mario@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pwd", "hashed")).thenReturn(true);
        doNothing().when(tokenStore).store(anyString(), eq("Mario")); // fix metodo void

        String token = userService.login(new LoginRequest("mario@example.com", "pwd"));

        assertNotNull(token);
        verify(tokenStore).store(eq(token), eq("Mario"));
    }

    @Test
    void login_invalidPassword_throws() {
        User user = new User();
        user.setEmail("mario@example.com");
        user.setPassword("hashed");

        when(userRepository.findByEmail("mario@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.login(new LoginRequest("mario@example.com", "wrong")));
    }
}
