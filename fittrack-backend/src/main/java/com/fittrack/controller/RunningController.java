package com.fittrack.controller;

import com.fittrack.dto.RunningDtos.*;
import com.fittrack.entity.User;
import com.fittrack.service.RunningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/running")
@Tag(name = "Running Tracker", description = "Track distance, duration, pace, and running statistics")
public class RunningController {

    private final RunningService runningService;

    public RunningController(RunningService runningService) {
        this.runningService = runningService;
    }

    @PostMapping("/logs")
    @Operation(summary = "Log a running session with distance, duration, and target goal")
    public ResponseEntity<RunningLogDto> logRun(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LogRunRequest req
    ) {
        return ResponseEntity.ok(runningService.logRun(user, req));
    }

    @GetMapping("/logs")
    @Operation(summary = "Get running history for the authenticated user")
    public ResponseEntity<List<RunningLogDto>> getRunningHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(runningService.getRunningHistory(user));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get total running statistics, weekly distance, and fastest pace")
    public ResponseEntity<RunningStatsSummary> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(runningService.getStats(user));
    }
}
