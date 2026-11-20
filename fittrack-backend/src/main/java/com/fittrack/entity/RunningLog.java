package com.fittrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "running_logs")
public class RunningLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate runDate;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(nullable = false)
    private Integer durationSeconds;

    @Column(precision = 6, scale = 2)
    private BigDecimal avgPaceMinPerKm;

    @Column(precision = 6, scale = 2)
    private BigDecimal estimatedCaloriesBurnt;

    @Column(length = 50)
    private String targetGoal; // e.g. "5K", "10K", "Intervals", "Casual"

    private String notes;

    public RunningLog() {}

    public RunningLog(Long id, User user, LocalDate runDate, BigDecimal distanceKm,
                      Integer durationSeconds, BigDecimal avgPaceMinPerKm,
                      BigDecimal estimatedCaloriesBurnt, String targetGoal, String notes) {
        this.id = id;
        this.user = user;
        this.runDate = runDate;
        this.distanceKm = distanceKm;
        this.durationSeconds = durationSeconds;
        this.avgPaceMinPerKm = avgPaceMinPerKm;
        this.estimatedCaloriesBurnt = estimatedCaloriesBurnt;
        this.targetGoal = targetGoal;
        this.notes = notes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private LocalDate runDate;
        private BigDecimal distanceKm;
        private Integer durationSeconds;
        private BigDecimal avgPaceMinPerKm;
        private BigDecimal estimatedCaloriesBurnt;
        private String targetGoal;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder runDate(LocalDate runDate) { this.runDate = runDate; return this; }
        public Builder distanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; return this; }
        public Builder durationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; return this; }
        public Builder avgPaceMinPerKm(BigDecimal avgPaceMinPerKm) { this.avgPaceMinPerKm = avgPaceMinPerKm; return this; }
        public Builder estimatedCaloriesBurnt(BigDecimal estimatedCaloriesBurnt) { this.estimatedCaloriesBurnt = estimatedCaloriesBurnt; return this; }
        public Builder targetGoal(String targetGoal) { this.targetGoal = targetGoal; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public RunningLog build() {
            return new RunningLog(id, user, runDate, distanceKm, durationSeconds, avgPaceMinPerKm, estimatedCaloriesBurnt, targetGoal, notes);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getRunDate() { return runDate; }
    public void setRunDate(LocalDate runDate) { this.runDate = runDate; }

    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public BigDecimal getAvgPaceMinPerKm() { return avgPaceMinPerKm; }
    public void setAvgPaceMinPerKm(BigDecimal avgPaceMinPerKm) { this.avgPaceMinPerKm = avgPaceMinPerKm; }

    public BigDecimal getEstimatedCaloriesBurnt() { return estimatedCaloriesBurnt; }
    public void setEstimatedCaloriesBurnt(BigDecimal estimatedCaloriesBurnt) { this.estimatedCaloriesBurnt = estimatedCaloriesBurnt; }

    public String getTargetGoal() { return targetGoal; }
    public void setTargetGoal(String targetGoal) { this.targetGoal = targetGoal; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
