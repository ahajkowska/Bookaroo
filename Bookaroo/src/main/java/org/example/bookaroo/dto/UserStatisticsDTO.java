package org.example.bookaroo.dto;

import jakarta.validation.constraints.Min;

public record UserStatisticsDTO(
        @Min(0)
        int readCount,
        int currentYear
) {}