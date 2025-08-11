package com.example.myspringapp.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.myspringapp.model.User;

public class TokenAuthenticationStore {

    private static final Map<String, User> tokens = new ConcurrentHashMap<>();

    public static void storeToken(String token, User user) {
        tokens.put(token, user);
    }

    public static boolean isValid(String token) {
        return token != null && tokens.containsKey(token);
    }

    public static User getUser(String token) {
        return tokens.get(token);
    }
}

