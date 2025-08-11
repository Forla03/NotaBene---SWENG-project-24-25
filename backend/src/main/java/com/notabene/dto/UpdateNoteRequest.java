package com.notabene.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateNoteRequest {
    
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;
    
    @Size(max = 280, message = "Content cannot exceed 280 characters")
    private String content;
    
    public UpdateNoteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
