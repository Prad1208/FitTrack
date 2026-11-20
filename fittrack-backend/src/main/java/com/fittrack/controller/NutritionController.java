package com.fittrack.controller;

import com.fittrack.dto.NutritionDtos.*;
import com.fittrack.entity.User;
import com.fittrack.service.NutritionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/nutrition")
@Tag(name = "Nutrition & Food", description = "Food item search, custom foods, and daily meal tracking")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/foods/search")
    @Operation(summary = "Search food items in the nutritional database")
    public ResponseEntity<List<FoodItemDto>> searchFoods(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(nutritionService.searchFoods(user, query));
    }

    @PostMapping("/foods/custom")
    @Operation(summary = "Add a custom food item with per-100g nutritional profile")
    public ResponseEntity<FoodItemDto> createCustomFood(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCustomFoodRequest req
    ) {
        return ResponseEntity.ok(nutritionService.createCustomFood(user, req));
    }

    @PostMapping("/entries")
    @Operation(summary = "Log a meal entry (Breakfast, Lunch, Dinner, Snack) with quantity in grams")
    public ResponseEntity<MealEntryDto> addMealEntry(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddMealEntryRequest req
    ) {
        return ResponseEntity.ok(nutritionService.addMealEntry(user, req));
    }

    @GetMapping("/daily")
    @Operation(summary = "Get all logged meals for a specific date grouped by meal type")
    public ResponseEntity<DailyNutritionResponse> getDailyNutrition(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(nutritionService.getDailyNutrition(user, date));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete a meal entry")
    public ResponseEntity<Void> deleteMealEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        nutritionService.deleteMealEntry(user, id);
        return ResponseEntity.noContent().build();
    }
}
