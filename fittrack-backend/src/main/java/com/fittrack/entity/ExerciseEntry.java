package com.fittrack.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercise_entries")
public class ExerciseEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @Column(nullable = false)
    private String exerciseName; // e.g. "Barbell Bench Press"

    @Column(nullable = false, length = 30)
    private String muscleGroup; // CHEST, BACK, SHOULDERS, LEGS, BICEPS, TRICEPS, ABS, CORE

    private Integer orderIndex;

    @OneToMany(mappedBy = "exerciseEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseSet> sets = new ArrayList<>();

    public ExerciseEntry() {}

    public ExerciseEntry(Long id, WorkoutSession workoutSession, String exerciseName,
                         String muscleGroup, Integer orderIndex, List<ExerciseSet> sets) {
        this.id = id;
        this.workoutSession = workoutSession;
        this.exerciseName = exerciseName;
        this.muscleGroup = muscleGroup;
        this.orderIndex = orderIndex;
        this.sets = sets != null ? sets : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private WorkoutSession workoutSession;
        private String exerciseName;
        private String muscleGroup;
        private Integer orderIndex;
        private List<ExerciseSet> sets = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder workoutSession(WorkoutSession workoutSession) { this.workoutSession = workoutSession; return this; }
        public Builder exerciseName(String exerciseName) { this.exerciseName = exerciseName; return this; }
        public Builder muscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; return this; }
        public Builder orderIndex(Integer orderIndex) { this.orderIndex = orderIndex; return this; }
        public Builder sets(List<ExerciseSet> sets) { this.sets = sets; return this; }

        public ExerciseEntry build() {
            return new ExerciseEntry(id, workoutSession, exerciseName, muscleGroup, orderIndex, sets);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkoutSession getWorkoutSession() { return workoutSession; }
    public void setWorkoutSession(WorkoutSession workoutSession) { this.workoutSession = workoutSession; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public List<ExerciseSet> getSets() { return sets; }
    public void setSets(List<ExerciseSet> sets) { this.sets = sets; }
}
