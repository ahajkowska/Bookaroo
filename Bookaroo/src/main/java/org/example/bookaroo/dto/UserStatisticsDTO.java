package org.example.bookaroo.dto;

public record UserStatisticsDTO(
        int readCount,
        int currentYear
) {}