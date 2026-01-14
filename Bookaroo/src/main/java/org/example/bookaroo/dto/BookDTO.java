package org.example.bookaroo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BookDTO(
        UUID id,

        @NotBlank(message = "Tytuł jest wymagany")
        @Size(min = 2, max = 100, message = "Tytuł musi mieć od 2 do 100 znaków")
        String title,

        @NotBlank(message = "ISBN jest wymagany")
        String isbn,

        String description,

        int publicationYear,

        @NotNull(message = "ID autora jest wymagane")
        UUID authorId,

        String authorName,

        Double averageRating,

        List<String> genres
) {}