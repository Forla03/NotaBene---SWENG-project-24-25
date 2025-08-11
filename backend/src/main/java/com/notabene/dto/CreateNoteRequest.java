package com.notabene.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateNoteRequest {
    
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 280, message = "Content cannot exceed 280 characters")
    private String content;
    
    public CreateNoteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
