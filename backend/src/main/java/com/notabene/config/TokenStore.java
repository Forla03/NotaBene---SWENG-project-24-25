package com.notabene.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TokenStore {
    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    
    public void store(String token, String username) { 
        if (token != null) {
            tokens.put(token, username); 
        }
    }
    
    public boolean isValid(String token) { 
        return token != null && tokens.containsKey(token); 
    }
    
    public String getUsername(String token) { 
        return token != null ? tokens.get(token) : null; 
    }
}
