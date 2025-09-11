package com.notabene.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenStore Unit Tests")
class TokenStoreTest {

    private TokenStore tokenStore;

    @BeforeEach
    void setUp() {
        tokenStore = new TokenStore();
    }

    @Test
    @DisplayName("Should store and validate token successfully")
    void shouldStoreAndValidateTokenSuccessfully() {
        // Given
        String token = "test-token-123";
        String username = "testuser";

        // When
        tokenStore.store(token, username);

        // Then
        assertTrue(tokenStore.isValid(token));
        assertEquals(username, tokenStore.getUsername(token));
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void shouldReturnFalseForInvalidToken() {
        // Given
        String invalidToken = "invalid-token";

        // When & Then
        assertFalse(tokenStore.isValid(invalidToken));
    }

    @Test
    @DisplayName("Should return null for invalid token username")
    void shouldReturnNullForInvalidTokenUsername() {
        // Given
        String invalidToken = "invalid-token";

        // When & Then
        assertNull(tokenStore.getUsername(invalidToken));
    }

    @Test
    @DisplayName("Should return false for null token")
    void shouldReturnFalseForNullToken() {
        // When & Then
        assertFalse(tokenStore.isValid(null));
    }

    @Test
    @DisplayName("Should return null for null token username")
    void shouldReturnNullForNullTokenUsername() {
        // When & Then
        assertNull(tokenStore.getUsername(null));
    }

    @Test
    @DisplayName("Should handle multiple tokens")
    void shouldHandleMultipleTokens() {
        // Given
        String token1 = "token-1";
        String token2 = "token-2";
        String username1 = "user1";
        String username2 = "user2";

        // When
        tokenStore.store(token1, username1);
        tokenStore.store(token2, username2);

        // Then
        assertTrue(tokenStore.isValid(token1));
        assertTrue(tokenStore.isValid(token2));
        assertEquals(username1, tokenStore.getUsername(token1));
        assertEquals(username2, tokenStore.getUsername(token2));
    }

    @Test
    @DisplayName("Should overwrite existing token")
    void shouldOverwriteExistingToken() {
        // Given
        String token = "same-token";
        String oldUsername = "olduser";
        String newUsername = "newuser";

        // When
        tokenStore.store(token, oldUsername);
        tokenStore.store(token, newUsername);

        // Then
        assertTrue(tokenStore.isValid(token));
        assertEquals(newUsername, tokenStore.getUsername(token));
    }

    @Test
    @DisplayName("Should handle empty string token")
    void shouldHandleEmptyStringToken() {
        // Given
        String emptyToken = "";
        String username = "testuser";

        // When
        tokenStore.store(emptyToken, username);

        // Then
        assertTrue(tokenStore.isValid(emptyToken));
        assertEquals(username, tokenStore.getUsername(emptyToken));
    }
}
