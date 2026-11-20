package com.fittrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsDtos {

    public record DailyTrendPoint(
            LocalDate date,
            BigDecimal calorieIntake,
            BigDecimal calorieTarget,
            BigDecimal tdeeKcal,
            BigDecimal deficitKcal,
            BigDecimal proteinGrams
    ) {}

    public record MuscleVolumePoint(
            String muscleGroup, // CHEST, BACK, SHOULDERS, LEGS, BICEPS, TRICEPS, ABS, CORE
            Long totalSets,
            BigDecimal totalVolumeKg
    ) {}

    public record AnalyticsDashboardResponse(
            List<DailyTrendPoint> trends,
            List<MuscleVolumePoint> muscleVolumeBreakdown,
            BigDecimal averageDeficitKcal,
            BigDecimal totalWorkoutsLogged,
            BigDecimal totalKmRan
    ) {}
}
