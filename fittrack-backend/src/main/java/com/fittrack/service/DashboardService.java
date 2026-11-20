package com.fittrack.service;

import com.fittrack.dto.DashboardDto.DashboardSummary;
import com.fittrack.entity.MealEntry;
import com.fittrack.entity.RunningLog;
import com.fittrack.entity.User;
import com.fittrack.entity.WorkoutSession;
import com.fittrack.repository.MealEntryRepository;
import com.fittrack.repository.RunningLogRepository;
import com.fittrack.repository.WorkoutSessionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final MealEntryRepository mealEntryRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final RunningLogRepository runningLogRepository;
    private final BmrTdeeCalculatorService bmrCalculator;

    public DashboardService(MealEntryRepository mealEntryRepository,
                            WorkoutSessionRepository workoutSessionRepository,
                            RunningLogRepository runningLogRepository,
                            BmrTdeeCalculatorService bmrCalculator) {
        this.mealEntryRepository = mealEntryRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.runningLogRepository = runningLogRepository;
        this.bmrCalculator = bmrCalculator;
    }

    public DashboardSummary getDashboardSummary(User user, LocalDate queryDate) {
        LocalDate targetDate = (queryDate != null) ? queryDate : LocalDate.now();

        // 1. Food and nutrition consumed on target date
        List<MealEntry> meals = mealEntryRepository.findByUserIdAndLogDateOrderByMealTypeAsc(user.getId(), targetDate);
        BigDecimal calorieIntake = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fats = BigDecimal.ZERO;

        for (MealEntry m : meals) {
            calorieIntake = calorieIntake.add(m.getCalories());
            protein = protein.add(m.getProteinGrams());
            carbs = carbs.add(m.getCarbsGrams());
            fats = fats.add(m.getFatsGrams());
        }

        // 2. Workouts on target date
        List<WorkoutSession> workouts = workoutSessionRepository.findByUserIdAndSessionDate(user.getId(), targetDate);
        String todayWorkoutRoutine = null;
        int workoutDuration = 0;
        BigDecimal workoutVolume = BigDecimal.ZERO;
        BigDecimal workoutBurn = BigDecimal.ZERO;

        if (!workouts.isEmpty()) {
            WorkoutSession ws = workouts.get(0);
            todayWorkoutRoutine = ws.getRoutineName();
            workoutDuration = ws.getDurationMinutes() != null ? ws.getDurationMinutes() : 0;
            workoutVolume = ws.getTotalVolumeKg() != null ? ws.getTotalVolumeKg() : BigDecimal.ZERO;
            workoutBurn = ws.getEstimatedCaloriesBurnt() != null ? ws.getEstimatedCaloriesBurnt() : BigDecimal.ZERO;
        }

        // 3. Runs on target date
        List<RunningLog> runs = runningLogRepository.findByUserIdAndRunDate(user.getId(), targetDate);
        BigDecimal runningDistance = BigDecimal.ZERO;
        int runningDuration = 0;
        BigDecimal runningBurn = BigDecimal.ZERO;

        for (RunningLog r : runs) {
            runningDistance = runningDistance.add(r.getDistanceKm());
            runningDuration += r.getDurationSeconds();
            if (r.getEstimatedCaloriesBurnt() != null) {
                runningBurn = runningBurn.add(r.getEstimatedCaloriesBurnt());
            }
        }

        BigDecimal totalBurn = workoutBurn.add(runningBurn);

        // 4. Calorie Deficit Engine
        // Deficit = TDEE - Intake
        BigDecimal tdee = user.getTdeeKcal() != null ? user.getTdeeKcal() : BigDecimal.valueOf(2200.0);
        BigDecimal deficit = bmrCalculator.calculateDeficit(tdee, calorieIntake);

        // 5. Weekly summary stats
        LocalDate sevenDaysAgo = targetDate.minusDays(7);
        List<WorkoutSession> weeklyWorkouts = workoutSessionRepository
                .findByUserIdAndSessionDateBetweenOrderBySessionDateDesc(user.getId(), sevenDaysAgo, targetDate);
        List<RunningLog> weeklyRuns = runningLogRepository
                .findByUserIdAndRunDateBetweenOrderByRunDateAsc(user.getId(), sevenDaysAgo, targetDate);

        BigDecimal weeklyKm = weeklyRuns.stream()
                .map(RunningLog::getDistanceKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummary(
                targetDate,
                calorieIntake,
                user.getTargetDailyCalories(),
                totalBurn,
                tdee,
                deficit,
                protein,
                user.getTargetProteinGrams(),
                carbs,
                user.getTargetCarbsGrams(),
                fats,
                user.getTargetFatsGrams(),
                todayWorkoutRoutine,
                workoutDuration,
                workoutVolume,
                runningDistance,
                runningDuration,
                weeklyWorkouts.size(),
                weeklyKm
        );
    }
}
