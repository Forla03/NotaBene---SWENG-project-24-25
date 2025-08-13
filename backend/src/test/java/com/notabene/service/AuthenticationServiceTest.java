package com.notabene.service;

import com.notabene.model.User;
import com.notabene.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        
        SecurityContextHolder.setContext(securityContext);
    }
    
    @Test
    @DisplayName("Should get current user successfully")
    void shouldGetCurrentUserSuccessfully() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        User result = authenticationService.getCurrentUser();
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Should throw exception when no authentication found")
    void shouldThrowExceptionWhenNoAuthenticationFound() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authenticationService.getCurrentUser()
        );
        
        assertEquals("No authenticated user found", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
    }
    
    @Test
    @DisplayName("Should throw exception when authentication name is null")
    void shouldThrowExceptionWhenAuthenticationNameIsNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(null);
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authenticationService.getCurrentUser()
        );
        
        assertEquals("No authenticated user found", exception.getMessage());
        verify(userRepository, never()).findByUsername(any());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found in database")
    void shouldThrowExceptionWhenUserNotFoundInDatabase() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistentuser");
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authenticationService.getCurrentUser()
        );
        
        assertEquals("Authenticated user not found in database: nonexistentuser", exception.getMessage());
        verify(userRepository).findByUsername("nonexistentuser");
    }
    
    @Test
    @DisplayName("Should get current username successfully")
    void shouldGetCurrentUsernameSuccessfully() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        
        String result = authenticationService.getCurrentUsername();
        
        assertEquals("testuser", result);
    }
    
    @Test
    @DisplayName("Should throw exception when getting username without authentication")
    void shouldThrowExceptionWhenGettingUsernameWithoutAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authenticationService.getCurrentUsername()
        );
        
        assertEquals("No authenticated user found", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should return true when user is authenticated")
    void shouldReturnTrueWhenUserIsAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        
        boolean result = authenticationService.isAuthenticated();
        
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should return false when no authentication")
    void shouldReturnFalseWhenNoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);
        
        boolean result = authenticationService.isAuthenticated();
        
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should return false when user is not authenticated")
    void shouldReturnFalseWhenUserIsNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        
        boolean result = authenticationService.isAuthenticated();
        
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should return false when authentication name is null")
    void shouldReturnFalseWhenAuthenticationNameIsNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(null);
        
        boolean result = authenticationService.isAuthenticated();
        
        assertFalse(result);
    }
}
