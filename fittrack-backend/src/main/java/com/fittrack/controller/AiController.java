package com.fittrack.controller;

import com.fittrack.dto.AiDtos.*;
import com.fittrack.entity.User;
import com.fittrack.service.GeminiAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Fitness Assistant", description = "Gemini AI Gym Planner, Nutrition Advisor, Running Coach & Chatbot")
public class AiController {

    private final GeminiAiService geminiAiService;

    public AiController(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    @PostMapping(value = "/generate-workout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate a personalized gym workout routine with structured periodization")
    public ResponseEntity<String> generateWorkout(
            @AuthenticationPrincipal User user,
            @RequestBody WorkoutPlanRequest req
    ) {
        String split = req.splitType() != null ? req.splitType() : "PPL";
        int days = req.daysPerWeek() != null ? req.daysPerWeek() : 3;
        String equipment = req.equipment() != null ? req.equipment() : "Full Gym";
        String level = req.fitnessLevel() != null ? req.fitnessLevel() : "Intermediate";

        return ResponseEntity.ok(geminiAiService.generateWorkoutPlan(split, days, equipment, level));
    }

    @PostMapping(value = "/suggest-meals", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Suggest macro-compliant meals aligned with remaining daily calorie deficit and protein target")
    public ResponseEntity<String> suggestMeals(
            @AuthenticationPrincipal User user,
            @RequestBody MealSuggestionRequest req
    ) {
        double cals = req.remainingCalories() != null ? req.remainingCalories() : 500.0;
        double protein = req.minProteinGrams() != null ? req.minProteinGrams() : 25.0;
        String diet = req.dietaryPreference() != null ? req.dietaryPreference() : "High-Protein";

        return ResponseEntity.ok(geminiAiService.suggestMeals(cals, protein, diet));
    }

    @PostMapping(value = "/generate-running-plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate structured running plan (2K, 5K, 10K, interval sessions)")
    public ResponseEntity<String> generateRunningPlan(
            @AuthenticationPrincipal User user,
            @RequestBody RunningPlanRequest req
    ) {
        String level = req.currentLevel() != null ? req.currentLevel() : "Beginner";
        String goal = req.targetGoal() != null ? req.targetGoal() : "5K";
        int weeks = req.weeksCount() != null ? req.weeksCount() : 4;
        int days = req.daysPerWeek() != null ? req.daysPerWeek() : 3;

        return ResponseEntity.ok(geminiAiService.generateRunningPlan(level, goal, weeks, days));
    }

    @PostMapping("/assistant-chat")
    @Operation(summary = "Chat with FitTrack AI Assistant on exercise form, diet, recovery, and plateau busting")
    public ResponseEntity<AiTextResponse> chatAssistant(
            @AuthenticationPrincipal User user,
            @RequestBody ChatAssistantRequest req
    ) {
        String context = String.format(
                "Current Weight: %.1fkg, Target Weight: %.1fkg, Daily Calorie Target: %.0f kcal, Target Protein: %.0fg, Activity: %s",
                user.getCurrentWeightKg(), user.getTargetWeightKg(),
                user.getTargetDailyCalories(), user.getTargetProteinGrams(),
                user.getActivityLevel()
        );

        String reply = geminiAiService.chatFitnessAssistant(req.message(), context);
        return ResponseEntity.ok(new AiTextResponse(reply));
    }
}
