package com.fittrack.service;

import com.fittrack.dto.AnalyticsDtos.*;
import com.fittrack.entity.MealEntry;
import com.fittrack.entity.RunningLog;
import com.fittrack.entity.User;
import com.fittrack.entity.WorkoutSession;
import com.fittrack.repository.MealEntryRepository;
import com.fittrack.repository.RunningLogRepository;
import com.fittrack.repository.WorkoutSessionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class AnalyticsService {

    private final MealEntryRepository mealEntryRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final RunningLogRepository runningLogRepository;

    public AnalyticsService(MealEntryRepository mealEntryRepository,
                            WorkoutSessionRepository workoutSessionRepository,
                            RunningLogRepository runningLogRepository) {
        this.mealEntryRepository = mealEntryRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.runningLogRepository = runningLogRepository;
    }

    public AnalyticsDashboardResponse getAnalytics(User user, int days) {
        int lookbackDays = Math.max(7, Math.min(days, 90));
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(lookbackDays);

        // 1. Group daily nutrition
        List<MealEntry> meals = mealEntryRepository.findByUserIdAndLogDateBetweenOrderByLogDateAsc(user.getId(), startDate, endDate);
        Map<LocalDate, BigDecimal[]> dailyMacros = new HashMap<>(); // [0: calories, 1: protein]

        for (MealEntry m : meals) {
            dailyMacros.putIfAbsent(m.getLogDate(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal[] acc = dailyMacros.get(m.getLogDate());
            acc[0] = acc[0].add(m.getCalories());
            acc[1] = acc[1].add(m.getProteinGrams());
        }

        BigDecimal tdee = user.getTdeeKcal() != null ? user.getTdeeKcal() : BigDecimal.valueOf(2200.0);
        BigDecimal targetCals = user.getTargetDailyCalories() != null ? user.getTargetDailyCalories() : BigDecimal.valueOf(1800.0);

        List<DailyTrendPoint> trendPoints = new ArrayList<>();
        BigDecimal totalDeficitSum = BigDecimal.ZERO;
        int loggedDaysCount = 0;

        for (int i = 0; i <= lookbackDays; i++) {
            LocalDate d = startDate.plusDays(i);
            BigDecimal[] recorded = dailyMacros.getOrDefault(d, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal intake = recorded[0];
            BigDecimal protein = recorded[1];
            BigDecimal deficit = tdee.subtract(intake);

            if (intake.compareTo(BigDecimal.ZERO) > 0) {
                totalDeficitSum = totalDeficitSum.add(deficit);
                loggedDaysCount++;
            }

            trendPoints.add(new DailyTrendPoint(d, intake, targetCals, tdee, deficit, protein));
        }

        BigDecimal avgDeficit = (loggedDaysCount > 0)
                ? totalDeficitSum.divide(BigDecimal.valueOf(loggedDaysCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 2. Muscle volume breakdown
        List<Object[]> rawVolume = workoutSessionRepository.getMuscleVolumeBreakdown(user.getId(), startDate);
        List<MuscleVolumePoint> volumePoints = new ArrayList<>();
        for (Object[] row : rawVolume) {
            String muscle = (String) row[0];
            Long totalSets = ((Number) row[1]).longValue();
            BigDecimal totalVol = (row[2] != null) ? BigDecimal.valueOf(((Number) row[2]).doubleValue()) : BigDecimal.ZERO;
            volumePoints.add(new MuscleVolumePoint(muscle, totalSets, totalVol));
        }

        // If no workouts recorded yet, provide baseline zero-filled categories
        if (volumePoints.isEmpty()) {
            List<String> muscles = List.of("CHEST", "BACK", "SHOULDERS", "LEGS", "BICEPS", "TRICEPS", "ABS", "CORE");
            for (String m : muscles) {
                volumePoints.add(new MuscleVolumePoint(m, 0L, BigDecimal.ZERO));
            }
        }

        // 3. Totals
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserIdAndSessionDateBetweenOrderBySessionDateDesc(user.getId(), startDate, endDate);
        List<RunningLog> runs = runningLogRepository.findByUserIdAndRunDateBetweenOrderByRunDateAsc(user.getId(), startDate, endDate);
        BigDecimal totalKm = runs.stream().map(RunningLog::getDistanceKm).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AnalyticsDashboardResponse(
                trendPoints,
                volumePoints,
                avgDeficit,
                BigDecimal.valueOf(sessions.size()),
                totalKm
        );
    }
}
