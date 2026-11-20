package com.fittrack.controller;

import com.fittrack.config.JwtService;
import com.fittrack.dto.AuthDtos.*;
import com.fittrack.entity.User;
import com.fittrack.repository.UserRepository;
import com.fittrack.service.BmrTdeeCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Profile", description = "Registration, Login, and User Profile endpoints")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BmrTdeeCalculatorService bmrCalculator;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          BmrTdeeCalculatorService bmrCalculator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.bmrCalculator = bmrCalculator;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user and compute initial BMR, TDEE & macro targets")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered: " + req.email());
        }

        String activity = req.activityLevel() != null ? req.activityLevel() : "MODERATE";
        BigDecimal bmr = bmrCalculator.calculateBmr(req.currentWeightKg(), req.heightCm(), req.age(), req.gender());
        BigDecimal tdee = bmrCalculator.calculateTdee(bmr, activity);
        BigDecimal targetCalories = tdee.subtract(BigDecimal.valueOf(500.0)); // Default 500 kcal deficit

        var macros = bmrCalculator.calculateTargetMacros(targetCalories, req.currentWeightKg());

        User user = User.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .age(req.age())
                .gender(req.gender().toUpperCase())
                .heightCm(req.heightCm())
                .currentWeightKg(req.currentWeightKg())
                .targetWeightKg(req.targetWeightKg() != null ? req.targetWeightKg() : req.currentWeightKg())
                .activityLevel(activity)
                .bmrKcal(bmr)
                .tdeeKcal(tdee)
                .targetDailyCalories(targetCalories)
                .targetProteinGrams(macros.proteinGrams())
                .targetCarbsGrams(macros.carbsGrams())
                .targetFatsGrams(macros.fatsGrams())
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail(), saved.getId());

        return ResponseEntity.ok(new AuthResponse(
                token, saved.getId(), saved.getEmail(), saved.getFullName(),
                saved.getBmrKcal(), saved.getTdeeKcal(),
                saved.getTargetDailyCalories(), saved.getTargetProteinGrams(),
                saved.getTargetCarbsGrams(), saved.getTargetFatsGrams()
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive Bearer JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId());

        return ResponseEntity.ok(new AuthResponse(
                token, user.getId(), user.getEmail(), user.getFullName(),
                user.getBmrKcal(), user.getTdeeKcal(),
                user.getTargetDailyCalories(), user.getTargetProteinGrams(),
                user.getTargetCarbsGrams(), user.getTargetFatsGrams()
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user's profile and nutritional targets")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserProfileResponse(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getAge(), user.getGender(), user.getHeightCm(),
                user.getCurrentWeightKg(), user.getTargetWeightKg(),
                user.getActivityLevel(), user.getBmrKcal(), user.getTdeeKcal(),
                user.getTargetDailyCalories(), user.getTargetProteinGrams(),
                user.getTargetCarbsGrams(), user.getTargetFatsGrams()
        ));
    }
}
