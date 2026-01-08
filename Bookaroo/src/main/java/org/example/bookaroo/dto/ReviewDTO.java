package org.example.bookaroo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDTO(

        UUID id,

        @Min(value = 1, message = "Ocena musi być min. 1")
        @Max(value = 5, message = "Ocena może być max. 5")
        int rating,

        @NotBlank(message = "Treść recenzji nie może być pusta")
        String content,

        @CreationTimestamp
        LocalDateTime createdAt,

        UUID userId,

        String username,

        String authorAvatar,

        @NotNull(message = "ID książki jest wymagane")
        UUID bookId
) {}