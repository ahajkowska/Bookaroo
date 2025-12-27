package org.example.bookaroo.dto;

public record ReviewBackupDTO(
        String bookIsbn,
        String content,
        int rating
) {}
