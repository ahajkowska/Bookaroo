package org.example.bookaroo.dto;

import java.util.UUID;

public record BookDTO(
        UUID id,
        String title,
        String isbn,
        String description,
        int publicationYear,
        UUID authorId,
        String author,
        Double averageRating
) {}