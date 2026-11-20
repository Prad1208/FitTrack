package com.fittrack.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private Integer age;

    @Column(length = 20)
    private String gender; // MALE, FEMALE, OTHER

    @Column(precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(precision = 5, scale = 2)
    private BigDecimal currentWeightKg;

    @Column(precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Column(length = 30)
    private String activityLevel; // SEDENTARY, LIGHT, MODERATE, VERY_ACTIVE, EXTRA_ACTIVE

    @Column(precision = 7, scale = 2)
    private BigDecimal bmrKcal;

    @Column(precision = 7, scale = 2)
    private BigDecimal tdeeKcal;

    @Column(precision = 7, scale = 2)
    private BigDecimal targetDailyCalories;

    @Column(precision = 6, scale = 2)
    private BigDecimal targetProteinGrams;

    @Column(precision = 6, scale = 2)
    private BigDecimal targetCarbsGrams;

    @Column(precision = 6, scale = 2)
    private BigDecimal targetFatsGrams;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String email, String password, String fullName, Integer age, String gender,
                BigDecimal heightCm, BigDecimal currentWeightKg, BigDecimal targetWeightKg, String activityLevel,
                BigDecimal bmrKcal, BigDecimal tdeeKcal, BigDecimal targetDailyCalories,
                BigDecimal targetProteinGrams, BigDecimal targetCarbsGrams, BigDecimal targetFatsGrams,
                LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.currentWeightKg = currentWeightKg;
        this.targetWeightKg = targetWeightKg;
        this.activityLevel = activityLevel;
        this.bmrKcal = bmrKcal;
        this.tdeeKcal = tdeeKcal;
        this.targetDailyCalories = targetDailyCalories;
        this.targetProteinGrams = targetProteinGrams;
        this.targetCarbsGrams = targetCarbsGrams;
        this.targetFatsGrams = targetFatsGrams;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String password;
        private String fullName;
        private Integer age;
        private String gender;
        private BigDecimal heightCm;
        private BigDecimal currentWeightKg;
        private BigDecimal targetWeightKg;
        private String activityLevel;
        private BigDecimal bmrKcal;
        private BigDecimal tdeeKcal;
        private BigDecimal targetDailyCalories;
        private BigDecimal targetProteinGrams;
        private BigDecimal targetCarbsGrams;
        private BigDecimal targetFatsGrams;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder heightCm(BigDecimal heightCm) { this.heightCm = heightCm; return this; }
        public Builder currentWeightKg(BigDecimal currentWeightKg) { this.currentWeightKg = currentWeightKg; return this; }
        public Builder targetWeightKg(BigDecimal targetWeightKg) { this.targetWeightKg = targetWeightKg; return this; }
        public Builder activityLevel(String activityLevel) { this.activityLevel = activityLevel; return this; }
        public Builder bmrKcal(BigDecimal bmrKcal) { this.bmrKcal = bmrKcal; return this; }
        public Builder tdeeKcal(BigDecimal tdeeKcal) { this.tdeeKcal = tdeeKcal; return this; }
        public Builder targetDailyCalories(BigDecimal targetDailyCalories) { this.targetDailyCalories = targetDailyCalories; return this; }
        public Builder targetProteinGrams(BigDecimal targetProteinGrams) { this.targetProteinGrams = targetProteinGrams; return this; }
        public Builder targetCarbsGrams(BigDecimal targetCarbsGrams) { this.targetCarbsGrams = targetCarbsGrams; return this; }
        public Builder targetFatsGrams(BigDecimal targetFatsGrams) { this.targetFatsGrams = targetFatsGrams; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public User build() {
            return new User(id, email, password, fullName, age, gender, heightCm, currentWeightKg,
                    targetWeightKg, activityLevel, bmrKcal, tdeeKcal, targetDailyCalories,
                    targetProteinGrams, targetCarbsGrams, targetFatsGrams, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public BigDecimal getCurrentWeightKg() { return currentWeightKg; }
    public void setCurrentWeightKg(BigDecimal currentWeightKg) { this.currentWeightKg = currentWeightKg; }

    public BigDecimal getTargetWeightKg() { return targetWeightKg; }
    public void setTargetWeightKg(BigDecimal targetWeightKg) { this.targetWeightKg = targetWeightKg; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public BigDecimal getBmrKcal() { return bmrKcal; }
    public void setBmrKcal(BigDecimal bmrKcal) { this.bmrKcal = bmrKcal; }

    public BigDecimal getTdeeKcal() { return tdeeKcal; }
    public void setTdeeKcal(BigDecimal tdeeKcal) { this.tdeeKcal = tdeeKcal; }

    public BigDecimal getTargetDailyCalories() { return targetDailyCalories; }
    public void setTargetDailyCalories(BigDecimal targetDailyCalories) { this.targetDailyCalories = targetDailyCalories; }

    public BigDecimal getTargetProteinGrams() { return targetProteinGrams; }
    public void setTargetProteinGrams(BigDecimal targetProteinGrams) { this.targetProteinGrams = targetProteinGrams; }

    public BigDecimal getTargetCarbsGrams() { return targetCarbsGrams; }
    public void setTargetCarbsGrams(BigDecimal targetCarbsGrams) { this.targetCarbsGrams = targetCarbsGrams; }

    public BigDecimal getTargetFatsGrams() { return targetFatsGrams; }
    public void setTargetFatsGrams(BigDecimal targetFatsGrams) { this.targetFatsGrams = targetFatsGrams; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
