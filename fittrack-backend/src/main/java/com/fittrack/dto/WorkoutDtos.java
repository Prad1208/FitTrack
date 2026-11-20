package com.fittrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class WorkoutDtos {

    public record StartWorkoutRequest(
            @NotBlank String routineName,
            LocalDate sessionDate,
            String notes
    ) {}

    public record AddExerciseRequest(
            @NotBlank String exerciseName,
            @NotBlank String muscleGroup // CHEST, BACK, SHOULDERS, LEGS, BICEPS, TRICEPS, ABS, CORE
    ) {}

    public record LogSetRequest(
            @NotNull Integer setNumber,
            @NotNull BigDecimal weightKg,
            @NotNull Integer reps,
            Integer rpe,
            Boolean isWarmup
    ) {}

    public record CompleteWorkoutRequest(
            Integer durationMinutes,
            String notes
    ) {}

    public record ExerciseSetDto(
            Long id,
            Integer setNumber,
            BigDecimal weightKg,
            Integer reps,
            Integer rpe,
            BigDecimal estimated1RmKg,
            Boolean isWarmup
    ) {}

    public record ExerciseEntryDto(
            Long id,
            String exerciseName,
            String muscleGroup,
            List<ExerciseSetDto> sets
    ) {}

    public record WorkoutSessionDto(
            Long id,
            String routineName,
            LocalDate sessionDate,
            Integer durationMinutes,
            BigDecimal totalVolumeKg,
            BigDecimal estimatedCaloriesBurnt,
            String notes,
            List<ExerciseEntryDto> exercises
    ) {}
}
