package org.example.bookaroo.dto;

import java.util.Map;

public record BookStatisticsDTO(
        long readersCount,
        double averageRating,
        Map<Integer, Integer> ratingDistribution
) {}