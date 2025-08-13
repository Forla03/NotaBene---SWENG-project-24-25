package com.notabene.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.notabene.config.TokenStore;
import com.notabene.dto.LoginRequest;
import com.notabene.dto.RegisterRequest;
import com.notabene.dto.RegisterResponse;
import com.notabene.model.User;
import com.notabene.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenStore tokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
    }
    

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return new RegisterResponse(user.getUsername(), user.getEmail());
    }

    public String login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.error("User not found for email: {}", request.getEmail());
                return new IllegalArgumentException("Invalid email or password");
            });
        log.info("User found: {}, checking password...", user.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Password mismatch for user: {}", user.getUsername());
            throw new IllegalArgumentException("Invalid email or password");
        }
        String token = UUID.randomUUID().toString();
        log.info("Login successful for user: {}, token: {}", user.getUsername(), token);
        tokenStore.store(token, user.getUsername());
        return token;
    }
}
