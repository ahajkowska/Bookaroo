package org.example.bookaroo.dto;

import java.util.List;

public record ShelfBackupDTO(
        String name,
        List<String> bookIsbns
) {}
