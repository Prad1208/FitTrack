package com.fittrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meal_entries")
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false, length = 30)
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal quantityGrams;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal calories;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal proteinGrams;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal carbsGrams;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal fatsGrams;

    public MealEntry() {}

    public MealEntry(Long id, User user, FoodItem foodItem, LocalDate logDate, String mealType,
                     BigDecimal quantityGrams, BigDecimal calories, BigDecimal proteinGrams,
                     BigDecimal carbsGrams, BigDecimal fatsGrams) {
        this.id = id;
        this.user = user;
        this.foodItem = foodItem;
        this.logDate = logDate;
        this.mealType = mealType;
        this.quantityGrams = quantityGrams;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatsGrams = fatsGrams;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private FoodItem foodItem;
        private LocalDate logDate;
        private String mealType;
        private BigDecimal quantityGrams;
        private BigDecimal calories;
        private BigDecimal proteinGrams;
        private BigDecimal carbsGrams;
        private BigDecimal fatsGrams;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder foodItem(FoodItem foodItem) { this.foodItem = foodItem; return this; }
        public Builder logDate(LocalDate logDate) { this.logDate = logDate; return this; }
        public Builder mealType(String mealType) { this.mealType = mealType; return this; }
        public Builder quantityGrams(BigDecimal quantityGrams) { this.quantityGrams = quantityGrams; return this; }
        public Builder calories(BigDecimal calories) { this.calories = calories; return this; }
        public Builder proteinGrams(BigDecimal proteinGrams) { this.proteinGrams = proteinGrams; return this; }
        public Builder carbsGrams(BigDecimal carbsGrams) { this.carbsGrams = carbsGrams; return this; }
        public Builder fatsGrams(BigDecimal fatsGrams) { this.fatsGrams = fatsGrams; return this; }

        public MealEntry build() {
            return new MealEntry(id, user, foodItem, logDate, mealType, quantityGrams, calories, proteinGrams, carbsGrams, fatsGrams);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public void setQuantityGrams(BigDecimal quantityGrams) { this.quantityGrams = quantityGrams; }

    public BigDecimal getCalories() { return calories; }
    public void setCalories(BigDecimal calories) { this.calories = calories; }

    public BigDecimal getProteinGrams() { return proteinGrams; }
    public void setProteinGrams(BigDecimal proteinGrams) { this.proteinGrams = proteinGrams; }

    public BigDecimal getCarbsGrams() { return carbsGrams; }
    public void setCarbsGrams(BigDecimal carbsGrams) { this.carbsGrams = carbsGrams; }

    public BigDecimal getFatsGrams() { return fatsGrams; }
    public void setFatsGrams(BigDecimal fatsGrams) { this.fatsGrams = fatsGrams; }
}
