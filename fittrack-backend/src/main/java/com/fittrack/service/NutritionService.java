package com.fittrack.service;

import com.fittrack.dto.NutritionDtos.*;
import com.fittrack.entity.FoodItem;
import com.fittrack.entity.MealEntry;
import com.fittrack.entity.User;
import com.fittrack.repository.FoodItemRepository;
import com.fittrack.repository.MealEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class NutritionService {

    private final FoodItemRepository foodItemRepository;
    private final MealEntryRepository mealEntryRepository;

    public NutritionService(FoodItemRepository foodItemRepository, MealEntryRepository mealEntryRepository) {
        this.foodItemRepository = foodItemRepository;
        this.mealEntryRepository = mealEntryRepository;
    }

    public List<FoodItemDto> searchFoods(User user, String query) {
        List<FoodItem> items = (query != null && !query.trim().isEmpty())
                ? foodItemRepository.searchFoods(user.getId(), query)
                : foodItemRepository.findByCreatedByUserIdOrCreatedByUserIdIsNullOrderByNameAsc(user.getId());

        return items.stream().map(this::mapToFoodDto).toList();
    }

    @Transactional
    public FoodItemDto createCustomFood(User user, CreateCustomFoodRequest req) {
        FoodItem item = FoodItem.builder()
                .name(req.name())
                .category(req.category() != null ? req.category() : "Custom")
                .caloriesPer100g(req.caloriesPer100g())
                .proteinPer100g(req.proteinPer100g())
                .carbsPer100g(req.carbsPer100g())
                .fatsPer100g(req.fatsPer100g())
                .fiberPer100g(req.fiberPer100g() != null ? req.fiberPer100g() : BigDecimal.ZERO)
                .isCustom(true)
                .createdByUserId(user.getId())
                .build();

        return mapToFoodDto(foodItemRepository.save(item));
    }

    @Transactional
    public MealEntryDto addMealEntry(User user, AddMealEntryRequest req) {
        FoodItem food = foodItemRepository.findById(req.foodItemId())
                .orElseThrow(() -> new IllegalArgumentException("Food item not found with ID: " + req.foodItemId()));

        // Scale per 100g
        BigDecimal factor = req.quantityGrams().divide(BigDecimal.valueOf(100.0), 4, RoundingMode.HALF_UP);
        BigDecimal calories = food.getCaloriesPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal protein = food.getProteinPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal carbs = food.getCarbsPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fats = food.getFatsPer100g().multiply(factor).setScale(2, RoundingMode.HALF_UP);

        MealEntry entry = MealEntry.builder()
                .user(user)
                .foodItem(food)
                .logDate(req.logDate())
                .mealType(req.mealType().toUpperCase())
                .quantityGrams(req.quantityGrams())
                .calories(calories)
                .proteinGrams(protein)
                .carbsGrams(carbs)
                .fatsGrams(fats)
                .build();

        return mapToMealDto(mealEntryRepository.save(entry));
    }

    public DailyNutritionResponse getDailyNutrition(User user, LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<MealEntry> entries = mealEntryRepository.findByUserIdAndLogDateOrderByMealTypeAsc(user.getId(), queryDate);

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFats = BigDecimal.ZERO;

        List<MealEntryDto> breakfast = new ArrayList<>();
        List<MealEntryDto> lunch = new ArrayList<>();
        List<MealEntryDto> dinner = new ArrayList<>();
        List<MealEntryDto> snacks = new ArrayList<>();

        for (MealEntry e : entries) {
            totalCalories = totalCalories.add(e.getCalories());
            totalProtein = totalProtein.add(e.getProteinGrams());
            totalCarbs = totalCarbs.add(e.getCarbsGrams());
            totalFats = totalFats.add(e.getFatsGrams());

            MealEntryDto dto = mapToMealDto(e);
            switch (e.getMealType()) {
                case "BREAKFAST" -> breakfast.add(dto);
                case "LUNCH" -> lunch.add(dto);
                case "DINNER" -> dinner.add(dto);
                default -> snacks.add(dto);
            }
        }

        return new DailyNutritionResponse(
                queryDate,
                totalCalories,
                totalProtein,
                totalCarbs,
                totalFats,
                user.getTargetDailyCalories(),
                user.getTargetProteinGrams(),
                breakfast,
                lunch,
                dinner,
                snacks
        );
    }

    @Transactional
    public void deleteMealEntry(User user, Long entryId) {
        MealEntry entry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found"));
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to delete this meal entry");
        }
        mealEntryRepository.delete(entry);
    }

    private FoodItemDto mapToFoodDto(FoodItem f) {
        return new FoodItemDto(
                f.getId(), f.getName(), f.getCategory(),
                f.getCaloriesPer100g(), f.getProteinPer100g(),
                f.getCarbsPer100g(), f.getFatsPer100g(),
                f.getFiberPer100g(), f.getIsCustom()
        );
    }

    private MealEntryDto mapToMealDto(MealEntry m) {
        return new MealEntryDto(
                m.getId(), m.getFoodItem().getId(), m.getFoodItem().getName(),
                m.getMealType(), m.getQuantityGrams(),
                m.getCalories(), m.getProteinGrams(),
                m.getCarbsGrams(), m.getFatsGrams()
        );
    }
}
