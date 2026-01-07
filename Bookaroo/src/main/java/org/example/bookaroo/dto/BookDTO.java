package org.example.bookaroo.dto;

import java.util.List;
import java.util.UUID;

public record BookDTO(
        UUID id,
        String title,
        String isbn,
        String description,
        int publicationYear,
        UUID authorId,
        String authorName,
        Double averageRating,
        List<String> genres
) {}