package com.fittrack.controller;

import com.fittrack.dto.AnalyticsDtos.AnalyticsDashboardResponse;
import com.fittrack.entity.User;
import com.fittrack.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Progress", description = "Historical trends, calorie deficit charts, and muscle volume distribution")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get historical trends for calorie intake vs TDEE deficit and muscle volume distribution")
    public ResponseEntity<AnalyticsDashboardResponse> getAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "14") int days
    ) {
        return ResponseEntity.ok(analyticsService.getAnalytics(user, days));
    }
}
