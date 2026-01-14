package org.example.bookaroo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record BookshelfDTO(
        UUID id,

        @NotBlank(message = "Nazwa półki nie może być pusta")
        String name,

        boolean isDefault,

        List<@Valid BookDTO> books
) {}