package org.example.bookaroo.dto;

import java.util.List;

public record UserBackupDTO(
        String bio,
        String avatar,
        List<ShelfBackupDTO> shelves,
        List<ReviewBackupDTO> reviews
) {}

