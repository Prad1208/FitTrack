package com.fittrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_sessions")
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String routineName; // e.g. "Push Day - Chest, Shoulders & Triceps"

    private LocalDate sessionDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationMinutes;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalVolumeKg; // Sum of (weight * reps) across all sets

    @Column(precision = 6, scale = 2)
    private BigDecimal estimatedCaloriesBurnt;

    private String notes;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseEntry> exercises = new ArrayList<>();

    public WorkoutSession() {}

    public WorkoutSession(Long id, User user, String routineName, LocalDate sessionDate,
                          LocalDateTime startTime, LocalDateTime endTime, Integer durationMinutes,
                          BigDecimal totalVolumeKg, BigDecimal estimatedCaloriesBurnt,
                          String notes, List<ExerciseEntry> exercises) {
        this.id = id;
        this.user = user;
        this.routineName = routineName;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.totalVolumeKg = totalVolumeKg;
        this.estimatedCaloriesBurnt = estimatedCaloriesBurnt;
        this.notes = notes;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private String routineName;
        private LocalDate sessionDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
        private BigDecimal totalVolumeKg;
        private BigDecimal estimatedCaloriesBurnt;
        private String notes;
        private List<ExerciseEntry> exercises = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder routineName(String routineName) { this.routineName = routineName; return this; }
        public Builder sessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder durationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }
        public Builder totalVolumeKg(BigDecimal totalVolumeKg) { this.totalVolumeKg = totalVolumeKg; return this; }
        public Builder estimatedCaloriesBurnt(BigDecimal estimatedCaloriesBurnt) { this.estimatedCaloriesBurnt = estimatedCaloriesBurnt; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder exercises(List<ExerciseEntry> exercises) { this.exercises = exercises; return this; }

        public WorkoutSession build() {
            return new WorkoutSession(id, user, routineName, sessionDate, startTime, endTime,
                    durationMinutes, totalVolumeKg, estimatedCaloriesBurnt, notes, exercises);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRoutineName() { return routineName; }
    public void setRoutineName(String routineName) { this.routineName = routineName; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getTotalVolumeKg() { return totalVolumeKg; }
    public void setTotalVolumeKg(BigDecimal totalVolumeKg) { this.totalVolumeKg = totalVolumeKg; }

    public BigDecimal getEstimatedCaloriesBurnt() { return estimatedCaloriesBurnt; }
    public void setEstimatedCaloriesBurnt(BigDecimal estimatedCaloriesBurnt) { this.estimatedCaloriesBurnt = estimatedCaloriesBurnt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<ExerciseEntry> getExercises() { return exercises; }
    public void setExercises(List<ExerciseEntry> exercises) { this.exercises = exercises; }
}
