package com.fittrack.repository;

import com.fittrack.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    @Query("SELECT f FROM FoodItem f WHERE (f.createdByUserId IS NULL OR f.createdByUserId = :userId) " +
           "AND LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FoodItem> searchFoods(@Param("userId") Long userId, @Param("query") String query);

    List<FoodItem> findByCreatedByUserIdOrCreatedByUserIdIsNullOrderByNameAsc(Long userId);
}
