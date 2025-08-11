package com.example.myspringapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.myspringapp.dto.LoginRequest;
import com.example.myspringapp.dto.LoginResponse;
import com.example.myspringapp.dto.RegisterRequest;
import com.example.myspringapp.service.UserService;

@RestController
@RequestMapping("/api/auth")
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
            String token = userService.login(request);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }
}

