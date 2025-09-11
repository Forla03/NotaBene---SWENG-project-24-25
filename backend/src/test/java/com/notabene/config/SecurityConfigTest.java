package com.notabene.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityConfig Unit Tests")
class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    @DisplayName("Should create BCrypt password encoder bean")
    void shouldCreateBCryptPasswordEncoderBean() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        // Then
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("Should encode passwords correctly")
    void shouldEncodePasswordsCorrectly() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    @DisplayName("Should not match incorrect password")
    void shouldNotMatchIncorrectPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When & Then
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    @DisplayName("Should create CORS configuration source bean")
    void shouldCreateCorsConfigurationSourceBean() {
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        
        // Then
        assertNotNull(corsConfigurationSource);
    }

    @Test
    @DisplayName("Should configure CORS for localhost:3000")
    void shouldConfigureCorsForLocalhost() {
        // Given
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // When
        var corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:3000"));
    }

    @Test
    @DisplayName("Should configure CORS methods")
    void shouldConfigureCORSMethods() {
        // Given
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // When
        var corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    @DisplayName("Should configure CORS headers")
    void shouldConfigureCORSHeaders() {
        // Given
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // When
        var corsConfig = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedHeaders().contains("Content-Type"));
        assertTrue(corsConfig.getAllowedHeaders().contains("X-Auth-Token"));
        assertTrue(corsConfig.getAllowedHeaders().contains("Authorization"));
        assertTrue(corsConfig.getExposedHeaders().contains("X-Auth-Token"));
    }
}
