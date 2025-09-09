package com.notabene.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import com.notabene.repository.UserRepository;
import com.notabene.model.User;
import java.util.Optional;

/**
 * Test configuration for TokenStore to provide mock tokens for integration tests
 */
@TestConfiguration
@Profile("test")
public class TestTokenConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    @Primary
    public TokenStore testTokenStore() {
        return new TokenStore() {
            @Override
            public boolean isValid(String token) {
                // Accept any test token that starts with "test-token-"
                return token != null && token.startsWith("test-token-");
            }
            
            @Override
            public String getUsername(String token) {
                if (token != null && token.startsWith("test-token-")) {
                    try {
                        // Extract user ID from token format "test-token-{userId}"
                        String userIdStr = token.substring("test-token-".length());
                        Long userId = Long.parseLong(userIdStr);
                        
                        // Look up the actual user by ID
                        Optional<User> user = userRepository.findById(userId);
                        if (user.isPresent()) {
                            return user.get().getUsername();
                        }
                    } catch (NumberFormatException e) {
                        // If token format is invalid, return null
                    }
                }
                return null;
            }
        };
    }
}
