package com.fittrack.repository;

import com.fittrack.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByUserIdAndSessionDate(Long userId, LocalDate sessionDate);

    List<WorkoutSession> findByUserIdAndSessionDateBetweenOrderBySessionDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    List<WorkoutSession> findByUserIdOrderBySessionDateDesc(Long userId);

    @Query("SELECT e.muscleGroup, COUNT(s.id), SUM(s.weightKg * s.reps) " +
           "FROM WorkoutSession w " +
           "JOIN w.exercises e " +
           "JOIN e.sets s " +
           "WHERE w.user.id = :userId AND w.sessionDate >= :sinceDate " +
           "GROUP BY e.muscleGroup")
    List<Object[]> getMuscleVolumeBreakdown(
            @Param("userId") Long userId,
            @Param("sinceDate") LocalDate sinceDate
    );
}
