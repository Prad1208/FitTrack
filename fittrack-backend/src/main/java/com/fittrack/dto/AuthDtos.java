package com.fittrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String fullName,
            @NotNull Integer age,
            @NotBlank String gender,
            @NotNull BigDecimal heightCm,
            @NotNull BigDecimal currentWeightKg,
            BigDecimal targetWeightKg,
            String activityLevel // SEDENTARY, LIGHT, MODERATE, VERY_ACTIVE, EXTRA_ACTIVE
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String token,
            Long userId,
            String email,
            String fullName,
            BigDecimal bmrKcal,
            BigDecimal tdeeKcal,
            BigDecimal targetDailyCalories,
            BigDecimal targetProteinGrams,
            BigDecimal targetCarbsGrams,
            BigDecimal targetFatsGrams
    ) {}

    public record UserProfileResponse(
            Long id,
            String email,
            String fullName,
            Integer age,
            String gender,
            BigDecimal heightCm,
            BigDecimal currentWeightKg,
            BigDecimal targetWeightKg,
            String activityLevel,
            BigDecimal bmrKcal,
            BigDecimal tdeeKcal,
            BigDecimal targetDailyCalories,
            BigDecimal targetProteinGrams,
            BigDecimal targetCarbsGrams,
            BigDecimal targetFatsGrams
    ) {}
}
