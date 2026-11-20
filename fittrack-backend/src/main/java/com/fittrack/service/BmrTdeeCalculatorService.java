package com.fittrack.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BmrTdeeCalculatorService {

    /**
     * Calculates BMR using Mifflin-St Jeor equation.
     * Men: 10 * weight(kg) + 6.25 * height(cm) - 5 * age + 5
     * Women: 10 * weight(kg) + 6.25 * height(cm) - 5 * age - 161
     */
    public BigDecimal calculateBmr(BigDecimal weightKg, BigDecimal heightCm, Integer age, String gender) {
        if (weightKg == null || heightCm == null || age == null || gender == null) {
            return BigDecimal.ZERO;
        }

        double weight = weightKg.doubleValue();
        double height = heightCm.doubleValue();
        int userAge = age;

        double base = (10.0 * weight) + (6.25 * height) - (5.0 * userAge);
        double bmr = "FEMALE".equalsIgnoreCase(gender) ? (base - 161.0) : (base + 5.0);

        return BigDecimal.valueOf(Math.max(bmr, 800.0)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates TDEE by multiplying BMR with physical activity multiplier.
     */
    public BigDecimal calculateTdee(BigDecimal bmr, String activityLevel) {
        if (bmr == null) return BigDecimal.ZERO;

        double multiplier = switch (activityLevel != null ? activityLevel.toUpperCase() : "SEDENTARY") {
            case "LIGHT", "LIGHTLY_ACTIVE" -> 1.375;
            case "MODERATE", "MODERATELY_ACTIVE" -> 1.55;
            case "VERY_ACTIVE", "HEAVY" -> 1.725;
            case "EXTRA_ACTIVE", "ATHLETE" -> 1.9;
            default -> 1.2; // Sedentary
        };

        return bmr.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates standard Calorie Deficit without double-counting baseline exercise.
     * Calorie Deficit = TDEE - Calorie Intake
     */
    public BigDecimal calculateDeficit(BigDecimal tdee, BigDecimal intake) {
        if (tdee == null) tdee = BigDecimal.ZERO;
        if (intake == null) intake = BigDecimal.ZERO;
        return tdee.subtract(intake).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Dynamic Daily Expenditure:
     * When tracking exact GPS/Gym workout calories, baseline is kept at Sedentary (BMR * 1.2)
     * to prevent double-counting exercise against an elevated activity multiplier!
     */
    public BigDecimal calculateDynamicExpenditure(BigDecimal bmr, BigDecimal workoutBurn, BigDecimal runningBurn) {
        if (bmr == null) bmr = BigDecimal.ZERO;
        if (workoutBurn == null) workoutBurn = BigDecimal.ZERO;
        if (runningBurn == null) runningBurn = BigDecimal.ZERO;

        BigDecimal baselineSedentary = bmr.multiply(BigDecimal.valueOf(1.2));
        return baselineSedentary.add(workoutBurn).add(runningBurn).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Estimates running calories burnt:
     * Calories = Distance(km) * Weight(kg) * 1.036
     */
    public BigDecimal estimateRunningCalories(BigDecimal distanceKm, BigDecimal weightKg) {
        if (distanceKm == null || weightKg == null) return BigDecimal.ZERO;
        double calories = distanceKm.doubleValue() * weightKg.doubleValue() * 1.036;
        return BigDecimal.valueOf(calories).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Estimates gym session calories burnt based on duration and weight (~6.0 METs for resistance training).
     */
    public BigDecimal estimateGymCalories(Integer durationMinutes, BigDecimal weightKg) {
        if (durationMinutes == null || weightKg == null) return BigDecimal.ZERO;
        // Calories = MET * 3.5 * weight(kg) / 200 * duration(minutes)
        double met = 6.0;
        double calories = met * 3.5 * weightKg.doubleValue() / 200.0 * durationMinutes;
        return BigDecimal.valueOf(calories).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates Estimated 1RM using the Epley formula:
     * 1RM = Weight * (1 + Reps / 30)
     */
    public BigDecimal calculate1Rm(BigDecimal weightKg, Integer reps) {
        if (weightKg == null || reps == null || reps <= 0) return BigDecimal.ZERO;
        if (reps == 1) return weightKg.setScale(2, RoundingMode.HALF_UP);

        double weight = weightKg.doubleValue();
        double oneRm = weight * (1.0 + (reps / 30.0));
        return BigDecimal.valueOf(oneRm).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Recommends macro split for muscle building while in calorie deficit:
     * - Protein: 2.0g per kg body weight (muscle preserving)
     * - Fats: 25% of target calories
     * - Carbs: Remaining calories
     */
    public MacroDistribution calculateTargetMacros(BigDecimal targetCalories, BigDecimal weightKg) {
        if (targetCalories == null || weightKg == null) {
            return new MacroDistribution(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        double calories = targetCalories.doubleValue();
        double weight = weightKg.doubleValue();

        // Protein: 2.0g per kg = 4 kcal per gram
        double proteinGrams = weight * 2.0;
        double proteinCalories = proteinGrams * 4.0;

        // Fats: 25% of total calories = 9 kcal per gram
        double fatCalories = calories * 0.25;
        double fatGrams = fatCalories / 9.0;

        // Carbs: Remainder calories = 4 kcal per gram
        double carbCalories = Math.max(0, calories - proteinCalories - fatCalories);
        double carbGrams = carbCalories / 4.0;

        return new MacroDistribution(
                BigDecimal.valueOf(proteinGrams).setScale(1, RoundingMode.HALF_UP),
                BigDecimal.valueOf(carbGrams).setScale(1, RoundingMode.HALF_UP),
                BigDecimal.valueOf(fatGrams).setScale(1, RoundingMode.HALF_UP)
        );
    }

    public record MacroDistribution(BigDecimal proteinGrams, BigDecimal carbsGrams, BigDecimal fatsGrams) {}
}
