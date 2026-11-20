package com.fittrack.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RunningDtos {

    public record LogRunRequest(
            @NotNull LocalDate runDate,
            @NotNull BigDecimal distanceKm,
            @NotNull Integer durationSeconds,
            String targetGoal, // 2K, 5K, 10K, Intervals
            String notes
    ) {}

    public record RunningLogDto(
            Long id,
            LocalDate runDate,
            BigDecimal distanceKm,
            Integer durationSeconds,
            BigDecimal avgPaceMinPerKm,
            BigDecimal estimatedCaloriesBurnt,
            String targetGoal,
            String notes
    ) {}

    public record RunningStatsSummary(
            BigDecimal totalDistanceKm,
            Integer totalRuns,
            BigDecimal weeklyDistanceKm,
            BigDecimal fastestPaceMinPerKm
    ) {}
}
