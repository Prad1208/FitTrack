package com.fittrack.repository;

import com.fittrack.entity.RunningLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RunningLogRepository extends JpaRepository<RunningLog, Long> {

    List<RunningLog> findByUserIdAndRunDate(Long userId, LocalDate runDate);

    List<RunningLog> findByUserIdOrderByRunDateDesc(Long userId);

    List<RunningLog> findByUserIdAndRunDateBetweenOrderByRunDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT r.targetGoal, MIN(r.avgPaceMinPerKm) FROM RunningLog r " +
           "WHERE r.user.id = :userId AND r.targetGoal IS NOT NULL " +
           "GROUP BY r.targetGoal")
    List<Object[]> getPersonalBests(@Param("userId") Long userId);
}
