package org.example.bookaroo.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateShelfDTO(
        @NotBlank(message = "Nazwa półki nie może być pusta")
        String name
) {}