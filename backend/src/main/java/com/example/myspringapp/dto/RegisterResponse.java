package com.example.myspringapp.dto;

public class RegisterResponse {
    private String username;
    private String email;

    public RegisterResponse() {
    }

    public RegisterResponse(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getter e Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

