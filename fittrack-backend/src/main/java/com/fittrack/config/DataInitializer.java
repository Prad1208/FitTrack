package com.fittrack.config;

import com.fittrack.entity.FoodItem;
import com.fittrack.entity.User;
import com.fittrack.repository.FoodItemRepository;
import com.fittrack.repository.UserRepository;
import com.fittrack.service.BmrTdeeCalculatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BmrTdeeCalculatorService bmrCalculator;

    public DataInitializer(FoodItemRepository foodItemRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           BmrTdeeCalculatorService bmrCalculator) {
        this.foodItemRepository = foodItemRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bmrCalculator = bmrCalculator;
    }

    @Override
    public void run(String... args) {
        seedFoodItems();
        seedDemoUser();
    }

    private void seedFoodItems() {
        log.info("Checking and seeding nutritional food catalog (per 100g)...");

        List<FoodItem> initialFoods = List.of(
                // --- Authentic Indian Staples: Rotis, Millets & Breads ---
                createFood("Roti / Chapati (Whole Wheat)", "Carbs", 264, 9.5, 53.0, 2.0, 9.2),
                createFood("Roti with Ghee", "Carbs", 298, 9.0, 50.0, 7.0, 8.5),
                createFood("Phulka (Oil-Free Puffed Roti)", "Carbs", 260, 9.5, 53.0, 1.2, 9.0),
                createFood("Bajra Roti (Pearl Millet Flatbread)", "Carbs", 360, 8.5, 67.0, 4.0, 11.0),
                createFood("Jowar Roti (Sorghum Flatbread)", "Carbs", 349, 8.0, 72.0, 2.5, 9.7),
                createFood("Ragi Roti (Finger Millet - Calcium Rich)", "Carbs", 336, 7.3, 72.0, 1.8, 11.5),
                createFood("Missi Roti (Besan & Wheat with Ajwain)", "Protein", 280, 12.0, 55.0, 4.5, 8.0),
                createFood("Methi Thepla (Fenugreek Flatbread)", "Carbs", 240, 7.0, 48.0, 5.5, 5.0),

                // --- Authentic Indian Staples: Dals, Legumes & Chana ---
                createFood("Rajma (Cooked Kidney Beans Curry)", "Protein", 135, 8.5, 21.0, 3.5, 6.4),
                createFood("Rajma (Boiled)", "Protein", 127, 8.7, 22.8, 0.5, 6.4),
                createFood("Chole / Chickpeas (Cooked Curry)", "Protein", 168, 8.5, 24.0, 4.8, 7.0),
                createFood("Chole / Chickpeas (Boiled)", "Protein", 164, 8.9, 27.4, 2.6, 7.6),
                createFood("Kala Chana (Boiled Black Chickpeas)", "Protein", 145, 9.0, 22.0, 2.2, 8.0),
                createFood("Kala Chana Chaat (with Onion & Lemon)", "Protein", 138, 8.5, 21.0, 2.0, 7.5),
                createFood("Yellow Lentils / Dal Tadka (Cooked)", "Protein", 116, 9.0, 20.0, 2.5, 7.9),
                createFood("Moong Dal (Cooked Yellow Dal)", "Protein", 105, 7.5, 18.0, 0.8, 5.0),
                createFood("Toor / Arhar Dal (Cooked)", "Protein", 120, 8.0, 18.5, 2.0, 5.5),
                createFood("Masoor Dal (Cooked Red Lentils)", "Protein", 115, 9.0, 20.0, 0.6, 6.0),
                createFood("Chana Dal (Cooked Bengal Gram)", "Protein", 130, 8.5, 21.0, 2.2, 6.5),
                createFood("Urad Dal (Cooked Black Gram)", "Protein", 135, 9.5, 21.0, 2.5, 7.0),
                createFood("Sprouted Moong Beans (Ankurit Moong)", "Protein", 105, 7.0, 19.0, 0.5, 4.5),
                createFood("Moong Dal Khichdi (Desi Comfort Food)", "Carbs", 148, 6.5, 26.0, 3.0, 3.5),

                // --- Indian Vegetarian Protein Powerhouses: Paneer, Soya, Chilla & Sattu ---
                createFood("Paneer (Indian Cottage Cheese)", "Dairy", 265, 18.3, 1.2, 20.8, 0.0),
                createFood("Paneer Low Fat", "Dairy", 160, 25.0, 2.0, 2.5, 0.0),
                createFood("Paneer Bhurji", "Protein", 210, 14.0, 4.0, 15.0, 1.5),
                createFood("Paneer Tikka (Tandoori Roasted)", "Protein", 220, 16.0, 6.0, 14.0, 1.0),
                createFood("Palak Paneer", "Veggies", 160, 9.0, 6.0, 11.0, 3.0),
                createFood("Soya Chunks / Nutrela (Raw Dry)", "Protein", 345, 52.0, 33.0, 0.5, 13.0),
                createFood("Soya Chunks (Cooked Sabzi)", "Protein", 130, 15.0, 10.0, 3.0, 4.0),
                createFood("Soya Chaap (Boiled)", "Protein", 155, 18.0, 14.0, 2.5, 3.5),
                createFood("Besan Chilla (Gram Flour Pancake)", "Protein", 185, 9.0, 22.0, 6.5, 4.5),
                createFood("Moong Dal Chilla with Paneer Stuffing", "Protein", 210, 14.0, 20.0, 7.5, 4.0),
                createFood("Sattu Flour (Roasted Gram Flour)", "Protein", 410, 23.0, 65.0, 5.0, 14.0),
                createFood("Sattu Sharbat (Namkeen with Jeera & Lemon)", "Protein", 120, 7.0, 20.0, 1.5, 4.0),

                // --- Indian Dairy & Probiotics ---
                createFood("Curd / Dahi (Plain Homestyle)", "Dairy", 61, 3.5, 4.7, 3.3, 0.0),
                createFood("Chaas / Spiced Buttermilk (Jeera Mint)", "Dairy", 22, 1.8, 2.5, 0.5, 0.0),
                createFood("Desi Cow Ghee (Pure Clarified Butter)", "Fats", 897, 0.0, 0.0, 99.5, 0.0),

                // --- Indian Rice, Grains, Breakfast & Snacks ---
                createFood("Cooked White Basmati Rice", "Carbs", 130, 2.7, 28.0, 0.3, 0.4),
                createFood("Cooked Brown Rice", "Carbs", 112, 2.6, 23.5, 0.9, 1.8),
                createFood("Steamed Jeera Rice", "Carbs", 135, 2.8, 28.0, 2.0, 0.4),
                createFood("Poha (Cooked with Peanuts & Veggies)", "Carbs", 160, 3.2, 28.0, 4.2, 2.1),
                createFood("Vegetable Upma (Suji Semolina)", "Carbs", 140, 3.5, 24.0, 3.5, 1.8),
                createFood("Vegetable Dalia (Broken Wheat)", "Carbs", 135, 4.0, 24.0, 2.8, 3.5),
                createFood("Masala Oats (Indian Spiced)", "Carbs", 145, 4.5, 22.0, 3.8, 3.2),
                createFood("Idli (Steamed Rice & Urad)", "Carbs", 132, 4.0, 28.0, 0.4, 1.5),
                createFood("Plain Dosa (Fermented Crepe)", "Carbs", 165, 4.0, 29.0, 4.0, 1.2),
                createFood("Roasted Makhana (Fox Nuts / Lotus Seeds)", "Carbs", 350, 9.7, 76.0, 0.5, 14.5),
                createFood("Bhuna Chana with Skin (Roasted Black Gram)", "Protein", 369, 18.6, 58.0, 5.0, 16.8),
                createFood("Boiled Sweet Potato (Shakarkandi)", "Carbs", 86, 1.6, 20.0, 0.1, 3.0),
                createFood("Boiled Potato (Aloo)", "Carbs", 87, 2.0, 17.0, 0.1, 2.0),

                // --- Indian Non-Veg Proteins (Poultry, Eggs, Fish, Meat) ---
                createFood("Chicken Breast (Boneless Skinless)", "Protein", 165, 31.0, 0.0, 3.6, 0.0),
                createFood("Homestyle Chicken Curry", "Protein", 175, 22.0, 4.0, 7.5, 1.0),
                createFood("Tandoori Chicken (Roasted Skinless)", "Protein", 195, 25.0, 3.0, 8.5, 0.5),
                createFood("Chicken Keema (Minced Chicken with Peas)", "Protein", 180, 20.0, 5.0, 8.5, 1.5),
                createFood("Whole Eggs", "Protein", 143, 12.6, 0.7, 9.5, 0.0),
                createFood("Egg Whites", "Protein", 52, 11.0, 0.7, 0.2, 0.0),
                createFood("Egg Bhurji (Indian 2-Egg Scramble)", "Protein", 160, 13.0, 3.0, 10.5, 0.8),
                createFood("Egg Curry (Homestyle 2-Egg)", "Protein", 180, 12.0, 6.0, 11.5, 1.0),
                createFood("Rohu / Katla Fish Curry (Bengali Style)", "Protein", 145, 18.0, 3.5, 6.5, 0.5),
                createFood("Pomfret / Surmai Fish (Pan Seared)", "Protein", 160, 21.0, 2.0, 7.0, 0.0),
                createFood("Mutton Curry (Lean Homestyle Goat Meat)", "Protein", 210, 19.0, 3.0, 13.5, 0.5),

                // --- Indian Veggies & Healthy Fats ---
                createFood("Bhindi Masala (Sautéed Okra)", "Veggies", 75, 2.2, 8.0, 3.5, 3.5),
                createFood("Lauki Sabzi (Bottle Gourd Light Curry)", "Veggies", 45, 1.0, 4.0, 2.5, 2.0),
                createFood("Mix Veg Sabzi (Carrots, Beans, Peas, Gobhi)", "Veggies", 85, 2.5, 9.0, 4.0, 3.0),
                createFood("Cucumber Tomato Kachumber Salad", "Veggies", 20, 1.0, 3.5, 0.2, 1.5),
                createFood("Amla (Indian Gooseberry - Vitamin C)", "Fruits", 44, 0.9, 10.0, 0.5, 4.3),
                createFood("Roasted Peanuts (Moongfali)", "Fats", 567, 26.0, 16.0, 49.0, 8.5),
                createFood("Almonds (Badam)", "Fats", 579, 21.2, 21.6, 49.9, 12.5),
                createFood("Walnuts (Akhrot)", "Fats", 654, 15.0, 14.0, 65.0, 6.7),
                createFood("Pumpkin Seeds (Kaddu Beej)", "Fats", 559, 30.0, 10.0, 49.0, 6.0),
                createFood("Jaggery (Gur - Iron Rich)", "Carbs", 383, 0.4, 98.0, 0.1, 0.0),

                // --- Core Gym & Fitness Supplements ---
                createFood("Whey Protein Powder (Isolate)", "Supplements", 380, 78.0, 6.0, 4.0, 2.0),
                createFood("Non-Fat Greek Yogurt", "Dairy", 59, 10.0, 3.6, 0.4, 0.0),
                createFood("Tofu (Extra Firm)", "Protein", 83, 10.0, 1.9, 5.3, 0.9),
                createFood("Rolled Oats (Raw)", "Carbs", 389, 16.9, 66.3, 6.9, 10.6),
                createFood("Broccoli (Raw)", "Veggies", 34, 2.8, 6.6, 0.4, 2.6),
                createFood("Spinach (Raw)", "Veggies", 23, 2.9, 3.6, 0.4, 2.2),
                createFood("Banana", "Fruits", 89, 1.1, 22.8, 0.3, 2.6),
                createFood("Apple", "Fruits", 52, 0.3, 13.8, 0.2, 2.4),
                createFood("Peanut Butter (Natural)", "Fats", 588, 25.0, 20.0, 50.0, 6.0),
                createFood("Olive Oil", "Fats", 884, 0.0, 0.0, 100.0, 0.0)
        );

        List<FoodItem> existingFoods = foodItemRepository.findAll();
        int addedCount = 0;

        for (FoodItem food : initialFoods) {
            boolean exists = existingFoods.stream()
                    .anyMatch(e -> e.getName().equalsIgnoreCase(food.getName()));
            if (!exists) {
                foodItemRepository.save(food);
                addedCount++;
            }
        }

        log.info("Nutritional food catalog ready. Seeded {} new staple food items (Total items: {}).",
                addedCount, foodItemRepository.count());
    }

    private FoodItem createFood(String name, String category, double cals, double p, double c, double f, double fiber) {
        return FoodItem.builder()
                .name(name)
                .category(category)
                .caloriesPer100g(BigDecimal.valueOf(cals))
                .proteinPer100g(BigDecimal.valueOf(p))
                .carbsPer100g(BigDecimal.valueOf(c))
                .fatsPer100g(BigDecimal.valueOf(f))
                .fiberPer100g(BigDecimal.valueOf(fiber))
                .isCustom(false)
                .build();
    }

    private void seedDemoUser() {
        if (userRepository.existsByEmail("demo@fittrack.com")) return;

        BigDecimal weight = BigDecimal.valueOf(75.0);
        BigDecimal height = BigDecimal.valueOf(178.0);
        int age = 26;
        String gender = "MALE";
        String activity = "MODERATE";

        BigDecimal bmr = bmrCalculator.calculateBmr(weight, height, age, gender);
        BigDecimal tdee = bmrCalculator.calculateTdee(bmr, activity);
        BigDecimal targetCalories = tdee.subtract(BigDecimal.valueOf(500.0)); // 500 kcal deficit

        var macros = bmrCalculator.calculateTargetMacros(targetCalories, weight);

        User demoUser = User.builder()
                .email("demo@fittrack.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Alex Morgan")
                .age(age)
                .gender(gender)
                .heightCm(height)
                .currentWeightKg(weight)
                .targetWeightKg(BigDecimal.valueOf(70.0))
                .activityLevel(activity)
                .bmrKcal(bmr)
                .tdeeKcal(tdee)
                .targetDailyCalories(targetCalories)
                .targetProteinGrams(macros.proteinGrams())
                .targetCarbsGrams(macros.carbsGrams())
                .targetFatsGrams(macros.fatsGrams())
                .build();

        userRepository.save(demoUser);
        log.info("Seeded demo user: demo@fittrack.com (Password: password123)");
    }
}
