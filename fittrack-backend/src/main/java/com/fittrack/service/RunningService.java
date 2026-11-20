package com.fittrack.service;

import com.fittrack.dto.RunningDtos.*;
import com.fittrack.entity.RunningLog;
import com.fittrack.entity.User;
import com.fittrack.repository.RunningLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class RunningService {

    private final RunningLogRepository runningLogRepository;
    private final BmrTdeeCalculatorService bmrCalculator;

    public RunningService(RunningLogRepository runningLogRepository, BmrTdeeCalculatorService bmrCalculator) {
        this.runningLogRepository = runningLogRepository;
        this.bmrCalculator = bmrCalculator;
    }

    @Transactional
    public RunningLogDto logRun(User user, LogRunRequest req) {
        // Calculate pace in minutes per km
        double distance = req.distanceKm().doubleValue();
        double minutes = req.durationSeconds() / 60.0;
        BigDecimal pace = (distance > 0)
                ? BigDecimal.valueOf(minutes / distance).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal calories = bmrCalculator.estimateRunningCalories(req.distanceKm(), user.getCurrentWeightKg());

        RunningLog run = RunningLog.builder()
                .user(user)
                .runDate(req.runDate())
                .distanceKm(req.distanceKm())
                .durationSeconds(req.durationSeconds())
                .avgPaceMinPerKm(pace)
                .estimatedCaloriesBurnt(calories)
                .targetGoal(req.targetGoal() != null ? req.targetGoal() : "General")
                .notes(req.notes())
                .build();

        return mapToDto(runningLogRepository.save(run));
    }

    public List<RunningLogDto> getRunningHistory(User user) {
        return runningLogRepository.findByUserIdOrderByRunDateDesc(user.getId())
                .stream().map(this::mapToDto).toList();
    }

    public RunningStatsSummary getStats(User user) {
        List<RunningLog> allRuns = runningLogRepository.findByUserIdOrderByRunDateDesc(user.getId());

        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal fastestPace = BigDecimal.valueOf(999.0);

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        BigDecimal weeklyDistance = BigDecimal.ZERO;

        for (RunningLog r : allRuns) {
            totalDistance = totalDistance.add(r.getDistanceKm());
            if (r.getAvgPaceMinPerKm() != null && r.getAvgPaceMinPerKm().compareTo(BigDecimal.ZERO) > 0) {
                if (r.getAvgPaceMinPerKm().compareTo(fastestPace) < 0) {
                    fastestPace = r.getAvgPaceMinPerKm();
                }
            }
            if (r.getRunDate().isAfter(sevenDaysAgo) || r.getRunDate().isEqual(sevenDaysAgo)) {
                weeklyDistance = weeklyDistance.add(r.getDistanceKm());
            }
        }

        if (fastestPace.compareTo(BigDecimal.valueOf(900.0)) > 0) {
            fastestPace = BigDecimal.ZERO;
        }

        return new RunningStatsSummary(totalDistance, allRuns.size(), weeklyDistance, fastestPace);
    }

    private RunningLogDto mapToDto(RunningLog r) {
        return new RunningLogDto(
                r.getId(), r.getRunDate(), r.getDistanceKm(),
                r.getDurationSeconds(), r.getAvgPaceMinPerKm(),
                r.getEstimatedCaloriesBurnt(), r.getTargetGoal(), r.getNotes()
        );
    }
}
