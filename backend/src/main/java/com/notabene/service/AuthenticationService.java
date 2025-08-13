package com.notabene.service;

import com.notabene.model.User;
import com.notabene.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    
    /**
     * Get the currently authenticated user from the security context
     * @return User entity of the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            log.error("No authentication found in security context");
            throw new IllegalStateException("No authenticated user found");
        }
        
        String username = authentication.getName();
        log.debug("Getting current user for username: {}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found in database for username: {}", username);
                    return new IllegalStateException("Authenticated user not found in database: " + username);
                });
    }
    
    /**
     * Get the currently authenticated username
     * @return username of the authenticated user
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        return authentication.getName();
    }
    
    /**
     * Check if a user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getName() != null;
    }
}
