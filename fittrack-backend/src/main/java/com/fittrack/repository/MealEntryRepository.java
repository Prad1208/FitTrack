package com.fittrack.repository;

import com.fittrack.entity.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    List<MealEntry> findByUserIdAndLogDateOrderByMealTypeAsc(Long userId, LocalDate logDate);

    List<MealEntry> findByUserIdAndLogDateBetweenOrderByLogDateAsc(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT m.logDate, SUM(m.calories), SUM(m.proteinGrams), SUM(m.carbsGrams), SUM(m.fatsGrams) " +
           "FROM MealEntry m WHERE m.user.id = :userId AND m.logDate BETWEEN :startDate AND :endDate " +
           "GROUP BY m.logDate ORDER BY m.logDate ASC")
    List<Object[]> getDailyNutritionSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
