package com.example.myspringapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "NotaBene Backend");
        response.put("timestamp", System.currentTimeMillis());
        
        // Test database connection
        try (Connection conn = dataSource.getConnection()) {
            response.put("database", "CONNECTED");
        } catch (Exception e) {
            response.put("database", "DISCONNECTED");
            response.put("database_error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
