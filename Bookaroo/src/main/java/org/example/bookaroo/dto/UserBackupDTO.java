package org.example.bookaroo.dto;

import java.util.List;

public record UserBackupDTO(
        List<ShelfBackupDTO> shelves,
        List<ReviewBackupDTO> reviews
) {}

