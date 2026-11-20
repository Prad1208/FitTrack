package com.fittrack.service;

import com.fittrack.dto.WorkoutDtos.*;
import com.fittrack.entity.ExerciseEntry;
import com.fittrack.entity.ExerciseSet;
import com.fittrack.entity.User;
import com.fittrack.entity.WorkoutSession;
import com.fittrack.repository.WorkoutSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final BmrTdeeCalculatorService bmrCalculator;

    public WorkoutService(WorkoutSessionRepository workoutSessionRepository, BmrTdeeCalculatorService bmrCalculator) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.bmrCalculator = bmrCalculator;
    }

    @Transactional
    public WorkoutSessionDto startWorkout(User user, StartWorkoutRequest req) {
        WorkoutSession session = WorkoutSession.builder()
                .user(user)
                .routineName(req.routineName())
                .sessionDate(req.sessionDate() != null ? req.sessionDate() : LocalDate.now())
                .startTime(LocalDateTime.now())
                .notes(req.notes())
                .totalVolumeKg(BigDecimal.ZERO)
                .estimatedCaloriesBurnt(BigDecimal.ZERO)
                .build();

        return mapToSessionDto(workoutSessionRepository.save(session));
    }

    @Transactional
    public WorkoutSessionDto addExercise(User user, Long sessionId, AddExerciseRequest req) {
        WorkoutSession session = getSessionOrThrow(user, sessionId);

        ExerciseEntry exercise = ExerciseEntry.builder()
                .workoutSession(session)
                .exerciseName(req.exerciseName())
                .muscleGroup(req.muscleGroup().toUpperCase())
                .orderIndex(session.getExercises().size() + 1)
                .build();

        session.getExercises().add(exercise);
        return mapToSessionDto(workoutSessionRepository.save(session));
    }

    @Transactional
    public WorkoutSessionDto logSet(User user, Long sessionId, Long exerciseId, LogSetRequest req) {
        WorkoutSession session = getSessionOrThrow(user, sessionId);

        ExerciseEntry exercise = session.getExercises().stream()
                .filter(e -> e.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

        BigDecimal estimated1Rm = bmrCalculator.calculate1Rm(req.weightKg(), req.reps());

        ExerciseSet set = ExerciseSet.builder()
                .exerciseEntry(exercise)
                .setNumber(req.setNumber())
                .weightKg(req.weightKg())
                .reps(req.reps())
                .rpe(req.rpe())
                .estimated1RmKg(estimated1Rm)
                .isWarmup(req.isWarmup() != null && req.isWarmup())
                .build();

        exercise.getSets().add(set);

        // Recalculate total session volume (weight * reps for working sets)
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (ExerciseEntry e : session.getExercises()) {
            for (ExerciseSet s : e.getSets()) {
                if (Boolean.TRUE.equals(s.getIsWarmup())) continue;
                totalVolume = totalVolume.add(s.getWeightKg().multiply(BigDecimal.valueOf(s.getReps())));
            }
        }
        session.setTotalVolumeKg(totalVolume);

        return mapToSessionDto(workoutSessionRepository.save(session));
    }

    @Transactional
    public WorkoutSessionDto completeWorkout(User user, Long sessionId, CompleteWorkoutRequest req) {
        WorkoutSession session = getSessionOrThrow(user, sessionId);
        session.setEndTime(LocalDateTime.now());
        session.setDurationMinutes(req.durationMinutes() != null ? req.durationMinutes() : 60);
        if (req.notes() != null) session.setNotes(req.notes());

        BigDecimal cals = bmrCalculator.estimateGymCalories(session.getDurationMinutes(), user.getCurrentWeightKg());
        session.setEstimatedCaloriesBurnt(cals);

        return mapToSessionDto(workoutSessionRepository.save(session));
    }

    public List<WorkoutSessionDto> getHistory(User user) {
        return workoutSessionRepository.findByUserIdOrderBySessionDateDesc(user.getId())
                .stream().map(this::mapToSessionDto).toList();
    }

    private WorkoutSession getSessionOrThrow(User user, Long sessionId) {
        WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to workout session");
        }
        return session;
    }

    private WorkoutSessionDto mapToSessionDto(WorkoutSession s) {
        List<ExerciseEntryDto> exerciseDtos = s.getExercises().stream().map(e -> {
            List<ExerciseSetDto> setDtos = e.getSets().stream().map(set ->
                    new ExerciseSetDto(
                            set.getId(), set.getSetNumber(), set.getWeightKg(),
                            set.getReps(), set.getRpe(), set.getEstimated1RmKg(), set.getIsWarmup()
                    )
            ).toList();

            return new ExerciseEntryDto(e.getId(), e.getExerciseName(), e.getMuscleGroup(), setDtos);
        }).toList();

        return new WorkoutSessionDto(
                s.getId(), s.getRoutineName(), s.getSessionDate(),
                s.getDurationMinutes(), s.getTotalVolumeKg(),
                s.getEstimatedCaloriesBurnt(), s.getNotes(), exerciseDtos
        );
    }
}
