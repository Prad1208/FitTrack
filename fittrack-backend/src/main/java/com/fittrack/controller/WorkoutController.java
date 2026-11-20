package com.fittrack.controller;

import com.fittrack.dto.WorkoutDtos.*;
import com.fittrack.entity.User;
import com.fittrack.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workouts")
@Tag(name = "Gym Workouts", description = "Routines, muscle building exercises, sets, volume & 1RM tracking")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Start a new gym workout session")
    public ResponseEntity<WorkoutSessionDto> startWorkout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody StartWorkoutRequest req
    ) {
        return ResponseEntity.ok(workoutService.startWorkout(user, req));
    }

    @PostMapping("/sessions/{sessionId}/exercises")
    @Operation(summary = "Add an exercise (Chest, Back, Shoulders, Legs, Biceps, Triceps, Abs, Core) to the session")
    public ResponseEntity<WorkoutSessionDto> addExercise(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @Valid @RequestBody AddExerciseRequest req
    ) {
        return ResponseEntity.ok(workoutService.addExercise(user, sessionId, req));
    }

    @PostMapping("/sessions/{sessionId}/exercises/{exerciseId}/sets")
    @Operation(summary = "Log an exercise set with weight, reps, RPE, and auto-computed 1RM")
    public ResponseEntity<WorkoutSessionDto> logSet(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody LogSetRequest req
    ) {
        return ResponseEntity.ok(workoutService.logSet(user, sessionId, exerciseId, req));
    }

    @PutMapping("/sessions/{sessionId}/complete")
    @Operation(summary = "Complete a workout session, calculate total volume load & burn")
    public ResponseEntity<WorkoutSessionDto> completeWorkout(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @RequestBody CompleteWorkoutRequest req
    ) {
        return ResponseEntity.ok(workoutService.completeWorkout(user, sessionId, req));
    }

    @GetMapping("/history")
    @Operation(summary = "Retrieve previous workout sessions with volume breakdown")
    public ResponseEntity<List<WorkoutSessionDto>> getHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutService.getHistory(user));
    }
}
