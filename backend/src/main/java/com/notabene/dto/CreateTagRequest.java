package com.notabene.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagRequest(
        @NotBlank(message = "Il nome del tag è obbligatorio")
        String name
) {}
