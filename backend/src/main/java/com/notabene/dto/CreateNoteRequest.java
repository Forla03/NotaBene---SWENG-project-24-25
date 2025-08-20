package com.notabene.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @JsonAlias({"tagIds","tag_ids"})
    private List<Long> tagIds = new ArrayList<>();
    
    public CreateNoteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
