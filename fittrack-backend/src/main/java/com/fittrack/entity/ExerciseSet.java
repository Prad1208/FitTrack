package com.fittrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exercise_sets")
public class ExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_entry_id", nullable = false)
    private ExerciseEntry exerciseEntry;

    @Column(nullable = false)
    private Integer setNumber;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(nullable = false)
    private Integer reps;

    private Integer rpe; // 1-10 Rate of Perceived Exertion

    @Column(precision = 6, scale = 2)
    private BigDecimal estimated1RmKg; // Calculated via Epley formula: weight * (1 + reps/30)

    private Boolean isWarmup;

    public ExerciseSet() {}

    public ExerciseSet(Long id, ExerciseEntry exerciseEntry, Integer setNumber, BigDecimal weightKg,
                       Integer reps, Integer rpe, BigDecimal estimated1RmKg, Boolean isWarmup) {
        this.id = id;
        this.exerciseEntry = exerciseEntry;
        this.setNumber = setNumber;
        this.weightKg = weightKg;
        this.reps = reps;
        this.rpe = rpe;
        this.estimated1RmKg = estimated1RmKg;
        this.isWarmup = isWarmup;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private ExerciseEntry exerciseEntry;
        private Integer setNumber;
        private BigDecimal weightKg;
        private Integer reps;
        private Integer rpe;
        private BigDecimal estimated1RmKg;
        private Boolean isWarmup;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder exerciseEntry(ExerciseEntry exerciseEntry) { this.exerciseEntry = exerciseEntry; return this; }
        public Builder setNumber(Integer setNumber) { this.setNumber = setNumber; return this; }
        public Builder weightKg(BigDecimal weightKg) { this.weightKg = weightKg; return this; }
        public Builder reps(Integer reps) { this.reps = reps; return this; }
        public Builder rpe(Integer rpe) { this.rpe = rpe; return this; }
        public Builder estimated1RmKg(BigDecimal estimated1RmKg) { this.estimated1RmKg = estimated1RmKg; return this; }
        public Builder isWarmup(Boolean isWarmup) { this.isWarmup = isWarmup; return this; }

        public ExerciseSet build() {
            return new ExerciseSet(id, exerciseEntry, setNumber, weightKg, reps, rpe, estimated1RmKg, isWarmup);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExerciseEntry getExerciseEntry() { return exerciseEntry; }
    public void setExerciseEntry(ExerciseEntry exerciseEntry) { this.exerciseEntry = exerciseEntry; }

    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getRpe() { return rpe; }
    public void setRpe(Integer rpe) { this.rpe = rpe; }

    public BigDecimal getEstimated1RmKg() { return estimated1RmKg; }
    public void setEstimated1RmKg(BigDecimal estimated1RmKg) { this.estimated1RmKg = estimated1RmKg; }

    public Boolean getIsWarmup() { return isWarmup; }
    public void setIsWarmup(Boolean isWarmup) { this.isWarmup = isWarmup; }
}
