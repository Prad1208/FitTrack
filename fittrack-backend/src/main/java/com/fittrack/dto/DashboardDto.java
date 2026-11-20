package com.fittrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardDto {

    public record DashboardSummary(
            LocalDate date,
            // Calorie & Deficit Engine Metrics
            BigDecimal calorieIntake,
            BigDecimal calorieTarget,
            BigDecimal estimatedCaloriesBurnt,
            BigDecimal tdeeKcal,
            BigDecimal calorieDeficit, // TDEE - Intake
            // Macros
            BigDecimal proteinConsumedGrams,
            BigDecimal proteinTargetGrams,
            BigDecimal carbsConsumedGrams,
            BigDecimal carbsTargetGrams,
            BigDecimal fatsConsumedGrams,
            BigDecimal fatsTargetGrams,
            // Gym & Running overview
            String todayWorkoutRoutine,
            Integer workoutDurationMinutes,
            BigDecimal workoutVolumeKg,
            BigDecimal runningDistanceKm,
            Integer runningDurationSeconds,
            // Weekly quick streak
            Integer weeklyWorkoutCount,
            BigDecimal weeklyRunningTotalKm
    ) {}
}
