package org.example.bookaroo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record BookStatisticsDTO(
        @Min(0)
        long readersCount,

        @Min(0)
        double averageRating,

        @NotNull
        Map<Integer, Integer> ratingDistribution
) {}