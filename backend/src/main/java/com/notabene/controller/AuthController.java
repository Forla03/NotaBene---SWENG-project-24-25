package com.notabene.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notabene.dto.LoginRequest;
import com.notabene.dto.LoginResponse;
import com.notabene.dto.RegisterRequest;
import com.notabene.service.UserService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            var res = userService.register(request);
            return ResponseEntity.status(201).body(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("error", "Email già in uso"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());
            String token = userService.login(request);
            log.info("Login successful for email: {}, token: {}", request.getEmail(), token);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (IllegalArgumentException e) {
            log.error("Login failed for email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}
