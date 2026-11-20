package com.fittrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category; // Protein, Carbs, Dairy, Veggies, Fruits, Fats, Supplements

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal caloriesPer100g;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal carbsPer100g;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal fatsPer100g;

    @Column(precision = 6, scale = 2)
    private BigDecimal fiberPer100g;

    private Boolean isCustom;

    private Long createdByUserId; // null for global foods

    public FoodItem() {}

    public FoodItem(Long id, String name, String category, BigDecimal caloriesPer100g,
                    BigDecimal proteinPer100g, BigDecimal carbsPer100g, BigDecimal fatsPer100g,
                    BigDecimal fiberPer100g, Boolean isCustom, Long createdByUserId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
        this.carbsPer100g = carbsPer100g;
        this.fatsPer100g = fatsPer100g;
        this.fiberPer100g = fiberPer100g;
        this.isCustom = isCustom;
        this.createdByUserId = createdByUserId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String category;
        private BigDecimal caloriesPer100g;
        private BigDecimal proteinPer100g;
        private BigDecimal carbsPer100g;
        private BigDecimal fatsPer100g;
        private BigDecimal fiberPer100g;
        private Boolean isCustom;
        private Long createdByUserId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder caloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; return this; }
        public Builder proteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; return this; }
        public Builder carbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; return this; }
        public Builder fatsPer100g(BigDecimal fatsPer100g) { this.fatsPer100g = fatsPer100g; return this; }
        public Builder fiberPer100g(BigDecimal fiberPer100g) { this.fiberPer100g = fiberPer100g; return this; }
        public Builder isCustom(Boolean isCustom) { this.isCustom = isCustom; return this; }
        public Builder createdByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; return this; }

        public FoodItem build() {
            return new FoodItem(id, name, category, caloriesPer100g, proteinPer100g,
                    carbsPer100g, fatsPer100g, fiberPer100g, isCustom, createdByUserId);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getCaloriesPer100g() { return caloriesPer100g; }
    public void setCaloriesPer100g(BigDecimal caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }

    public BigDecimal getProteinPer100g() { return proteinPer100g; }
    public void setProteinPer100g(BigDecimal proteinPer100g) { this.proteinPer100g = proteinPer100g; }

    public BigDecimal getCarbsPer100g() { return carbsPer100g; }
    public void setCarbsPer100g(BigDecimal carbsPer100g) { this.carbsPer100g = carbsPer100g; }

    public BigDecimal getFatsPer100g() { return fatsPer100g; }
    public void setFatsPer100g(BigDecimal fatsPer100g) { this.fatsPer100g = fatsPer100g; }

    public BigDecimal getFiberPer100g() { return fiberPer100g; }
    public void setFiberPer100g(BigDecimal fiberPer100g) { this.fiberPer100g = fiberPer100g; }

    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
}
