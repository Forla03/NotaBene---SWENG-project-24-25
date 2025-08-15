package com.notabene.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddPermissionRequest {
    
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    public AddPermissionRequest(String username) {
        this.username = username;
    }
}
