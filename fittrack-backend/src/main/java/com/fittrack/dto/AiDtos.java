package com.fittrack.dto;

import java.math.BigDecimal;

public class AiDtos {

    public record WorkoutPlanRequest(
            String splitType, // PPL, UPPER_LOWER, BRO_SPLIT, FULL_BODY
            Integer daysPerWeek,
            String equipment, // FULL_GYM, DUMBBELLS_ONLY, HOME_BODYWEIGHT
            String fitnessLevel // BEGINNER, INTERMEDIATE, ADVANCED
    ) {}

    public record MealSuggestionRequest(
            Double remainingCalories,
            Double minProteinGrams,
            String dietaryPreference // HIGH_PROTEIN, VEGETARIAN, VEGAN, KETO, OMNIVORE
    ) {}

    public record RunningPlanRequest(
            String currentLevel, // BEGINNER, INTERMEDIATE, ADVANCED
            String targetGoal, // 2K, 5K, 10K, HALF_MARATHON, FAT_BURN_HIIT
            Integer weeksCount,
            Integer daysPerWeek
    ) {}

    public record ChatAssistantRequest(
            String message
    ) {}

    public record AiTextResponse(
            String response
    ) {}
}
