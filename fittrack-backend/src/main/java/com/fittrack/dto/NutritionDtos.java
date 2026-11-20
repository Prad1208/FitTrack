package com.fittrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class NutritionDtos {

    public record FoodItemDto(
            Long id,
            String name,
            String category,
            BigDecimal caloriesPer100g,
            BigDecimal proteinPer100g,
            BigDecimal carbsPer100g,
            BigDecimal fatsPer100g,
            BigDecimal fiberPer100g,
            Boolean isCustom
    ) {}

    public record CreateCustomFoodRequest(
            @NotBlank String name,
            String category,
            @NotNull BigDecimal caloriesPer100g,
            @NotNull BigDecimal proteinPer100g,
            @NotNull BigDecimal carbsPer100g,
            @NotNull BigDecimal fatsPer100g,
            BigDecimal fiberPer100g
    ) {}

    public record AddMealEntryRequest(
            @NotNull Long foodItemId,
            @NotNull LocalDate logDate,
            @NotBlank String mealType, // BREAKFAST, LUNCH, DINNER, SNACK
            @NotNull BigDecimal quantityGrams
    ) {}

    public record MealEntryDto(
            Long id,
            Long foodItemId,
            String foodName,
            String mealType,
            BigDecimal quantityGrams,
            BigDecimal calories,
            BigDecimal proteinGrams,
            BigDecimal carbsGrams,
            BigDecimal fatsGrams
    ) {}

    public record DailyNutritionResponse(
            LocalDate date,
            BigDecimal totalCalories,
            BigDecimal totalProteinGrams,
            BigDecimal totalCarbsGrams,
            BigDecimal totalFatsGrams,
            BigDecimal calorieTarget,
            BigDecimal proteinTarget,
            List<MealEntryDto> breakfast,
            List<MealEntryDto> lunch,
            List<MealEntryDto> dinner,
            List<MealEntryDto> snacks
    ) {}
}
