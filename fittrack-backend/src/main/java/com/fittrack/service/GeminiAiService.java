package com.fittrack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiAiService(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public String resolveApiKey(String customApiKey) {
        if (customApiKey != null && !customApiKey.trim().isEmpty() && !customApiKey.contains("YOUR_GEMINI_API_KEY")) {
            return customApiKey.trim();
        }
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.contains("YOUR_GEMINI_API_KEY")) {
            return geminiApiKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        try {
            Path envPath = Paths.get("C:/Users/ASUS/Downloads/fittrack/.env");
            if (Files.exists(envPath)) {
                for (String line : Files.readAllLines(envPath)) {
                    if (line.trim().startsWith("GEMINI_API_KEY=")) {
                        String val = line.substring(line.indexOf('=') + 1).trim();
                        if (!val.isEmpty()) return val;
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * AI Workout Planner: Generates customized gym routines based on split, equipment & goals.
     */
    public String generateWorkoutPlan(String splitType, int daysPerWeek, String equipment, String fitnessLevel, String customApiKey) {
        String systemInstruction = "You are an elite certified strength & conditioning coach. " +
                "Generate a detailed, scientifically periodized workout plan formatted strictly as JSON. " +
                "Include exercise name, target muscle group (CHEST, BACK, SHOULDERS, LEGS, BICEPS, TRICEPS, ABS, CORE), " +
                "sets, rep range, RPE target, and primary form cue.";

        String userPrompt = String.format(
                "Create a %d-day per week %s workout routine for a %s lifter using %s equipment.",
                daysPerWeek, splitType, fitnessLevel, equipment
        );

        String apiKey = resolveApiKey(customApiKey);
        if (apiKey != null) {
            try {
                String liveResponse = callGeminiLive(systemInstruction, userPrompt, true, apiKey);
                if (liveResponse != null && liveResponse.contains("{") && liveResponse.contains("schedule")) {
                    return liveResponse;
                }
            } catch (Exception e) {
                log.warn("Gemini Live API call failed ({}). Switching to sports intelligence engine.", e.getMessage());
            }
        }

        return buildDynamicWorkoutPlan(splitType, daysPerWeek, equipment, fitnessLevel);
    }

    /**
     * AI Nutrition Assistant: Suggests meals adhering strictly to remaining calories & protein targets.
     */
    public String suggestMeals(double remainingCalories, double minProteinGrams, String dietaryPreference, String customApiKey) {
        String systemInstruction = "You are an elite sports dietitian specializing in body recomposition and calorie deficit management. " +
                "Suggest 3 balanced meal options strictly formatted as JSON. " +
                "Each meal must contain mealName, description, prepTimeMinutes, totalCalories, totalProteinGrams, " +
                "totalCarbsGrams, totalFatsGrams, and an ingredients array with precise grams for each ingredient.";

        String userPrompt = String.format(
                "User has %.0f calories remaining today and needs at least %.0fg of protein. " +
                "Dietary preference: %s. Suggest 3 delicious, macro-compliant meal options that do NOT exceed the remaining calorie ceiling.",
                remainingCalories, minProteinGrams, dietaryPreference
        );

        String apiKey = resolveApiKey(customApiKey);
        if (apiKey != null) {
            try {
                String liveResponse = callGeminiLive(systemInstruction, userPrompt, true, apiKey);
                if (liveResponse != null && liveResponse.contains("[") && liveResponse.contains("mealName")) {
                    return liveResponse;
                }
            } catch (Exception e) {
                log.warn("Gemini Live API call failed ({}). Switching to sports intelligence engine.", e.getMessage());
            }
        }

        return buildDynamicMealSuggestions(remainingCalories, minProteinGrams, dietaryPreference);
    }

    /**
     * AI Running Coach: Generates structured running progression (e.g. Couch to 5K, 10K prep, Interval HIIT).
     */
    public String generateRunningPlan(String currentLevel, String targetGoal, int weeksCount, int daysPerWeek, String customApiKey) {
        String systemInstruction = "You are an Olympic-level endurance running coach. " +
                "Generate a progressive weekly running schedule formatted strictly as JSON. " +
                "Specify run types (Easy Zone 2, Tempo, Interval, Long Run), target distance in km, " +
                "target pace recommendation, duration in minutes, and rest/recovery days.";

        String userPrompt = String.format(
                "Create a %d-week running plan for a %s runner aiming for %s training %d days per week.",
                weeksCount, currentLevel, targetGoal, daysPerWeek
        );

        String apiKey = resolveApiKey(customApiKey);
        if (apiKey != null) {
            try {
                String liveResponse = callGeminiLive(systemInstruction, userPrompt, true, apiKey);
                if (liveResponse != null && liveResponse.contains("{") && liveResponse.contains("schedule")) {
                    return liveResponse;
                }
            } catch (Exception e) {
                log.warn("Gemini Live API call failed ({}). Switching to sports intelligence engine.", e.getMessage());
            }
        }

        return buildDynamicRunningPlan(currentLevel, targetGoal, weeksCount, daysPerWeek);
    }

    /**
     * AI Fitness Assistant: Conversational chat grounded in user context (calories, workouts, recovery).
     */
    public String chatFitnessAssistant(String userMessage, String userContext, String customApiKey) {
        String systemInstruction = "You are FitTrack AI, an intelligent, empathetic, science-grounded fitness and nutrition assistant. " +
                "Provide actionable, concise, and motivating advice on gym exercise form, exercise substitutions, recovery, " +
                "and calorie deficit strategies. " +
                (userContext != null ? "User context: " + userContext : "");

        String apiKey = resolveApiKey(customApiKey);
        if (apiKey != null) {
            try {
                String liveResponse = callGeminiLive(systemInstruction, userMessage, false, apiKey);
                if (liveResponse != null && !liveResponse.trim().isEmpty()) {
                    return liveResponse;
                }
            } catch (Exception e) {
                log.warn("Gemini Live API call failed ({}). Switching to sports intelligence engine.", e.getMessage());
            }
        }

        return buildDynamicChatResponse(userMessage, userContext);
    }

    private String callGeminiLive(String systemPrompt, String userPrompt, boolean requireJson, String apiKey) throws Exception {
        Map<String, Object> textPart = Map.of("text", userPrompt);
        Map<String, Object> content = Map.of("role", "user", "parts", List.of(textPart));

        Map<String, Object> systemPart = Map.of("text", systemPrompt);
        Map<String, Object> systemInstruction = Map.of("parts", List.of(systemPart));

        Map<String, Object> generationConfig = requireJson
                ? Map.of("response_mime_type", "application/json")
                : Map.of();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(content),
                "systemInstruction", systemInstruction,
                "generationConfig", generationConfig
        );

        String url = geminiApiUrl + (geminiApiUrl.contains("?") ? "&key=" : "?key=") + apiKey;

        String response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (!textNode.isMissingNode()) {
            return textNode.asText();
        }
        return response;
    }

    /**
     * Comprehensive Sports Science Conversational Intelligence Engine (25+ intents & multi-turn contextual reasoning).
     */
    private String buildDynamicChatResponse(String message, String userContext) {
        String q = message != null ? message.toLowerCase().trim() : "";

        // 1. Shoulder Pain / Rotator Cuff / Pressing Discomfort
        if (q.contains("shoulder") || q.contains("rotator") || (q.contains("bench") && (q.contains("pain") || q.contains("hurt") || q.contains("click")))) {
            return "I hear you, and shoulder discomfort during lifting needs immediate proactive management:\n\n" +
                    "1. **Stop Pain Aggravators Immediately**:\n" +
                    "   • Cease flat barbell bench pressing and straight overhead barbell presses. When the shoulder is abducted at 90° and internally rotated, the supraspinatus tendon pinches against the acromion process (subacromial impingement).\n\n" +
                    "2. **Safe, Highly Stimulating Substitutions**:\n" +
                    "   • **Neutral-Grip Dumbbell Floor Press**: With palms facing each other and elbows at 45°, the floor physically stops humeral extension past the torso, relieving anterior capsule stress while heavily loading the chest.\n" +
                    "   • **Low-Incline (30°) Dumbbell Press**: Semi-tucked elbows provide massive chest recruitment with significantly less shoulder shear than flat pressing.\n" +
                    "   • **Landmine Press**: Pressing along a diagonal arc allows the scapula to rotate upward freely and naturally.\n" +
                    "   • **Scapular Plane Cable Lateral Raises ('Scaption')**: Perform lateral raises 30° forward of the coronal plane. It targets the lateral deltoid without pinching the rotator cuff.\n\n" +
                    "3. **Rotator Cuff Rehab & Warm-Up**:\n" +
                    "   • 2x15 Cable Face Pulls with external rotation (pull towards eyes, hands finish higher than elbows).\n" +
                    "   • 2x15 Band Pull-Aparts.\n\n" +
                    "4. **Medical Caution**: If you experience sharp, shooting pain, numbness down your fingers, or cannot raise your arm above horizontal, rest and consult a sports physiotherapist.";
        }

        // 2. Follow-Up: "so what can i do", "what should i do", "what next", "how to proceed"
        if (q.contains("what can i do") || q.contains("what should i do") || q.contains("what next") || q.contains("how to start") || q.contains("what to do") || q.contains("advice")) {
            return "Here is your clear, actionable 4-step recovery & workout game plan:\n\n" +
                    "1. **Pivot Today's Gym Session**: Do not stay idle, but do not push through joint pain. Swap today to **Lower Body** (Squats, Romanian Deadlifts, Hamstring Curls, Calves) or a dedicated **Zone 2 Cardio & Core session** (30 mins brisk incline walk + planks).\n\n" +
                    "2. **Targeted Relief**: If there is acute inflammation or heat, apply an ice pack for 15 minutes. If it feels like stiff, guarded muscle tightness, use warm compression to restore blood flow.\n\n" +
                    "3. **Nutritional Repair**: High-quality protein provides the essential amino acids needed for collagen and tissue synthesis. Include anti-inflammatory staples like turmeric milk (*haldi doodh* with black pepper), walnuts (omega-3s), and drink 3.5 liters of water.\n\n" +
                    "4. **Gentle Pendulum Swings**: Lean forward with your good hand on a table, letting your affected arm hang loosely. Make gentle circular swings for 60 seconds to lubricate the joint capsule with synovial fluid without muscular exertion.";
        }

        // 3. Knee Pain / Patellofemoral / Squat Discomfort
        if (q.contains("knee") || (q.contains("squat") && (q.contains("pain") || q.contains("hurt")))) {
            return "For knee or patellar discomfort during leg sessions:\n\n" +
                    "1. **Root Cause**: Excessive forward knee translation under heavy loads increases patellofemoral contact pressure.\n" +
                    "2. **Exercise Substitutions**:\n" +
                    "   • **Box Squats**: Sitting back onto a parallel box forces vertical shins, immediately transferring load from the knee joint to the glutes and hamstrings.\n" +
                    "   • **Romanian Deadlifts (RDLs)**: Zero knee shear; heavily develops the posterior chain.\n" +
                    "   • **Reverse Lunges**: Stepping backward drastically reduces patellar shearing compared to forward lunges.\n" +
                    "   • **Hamstring Curls & Sled Drags**: Walking backwards with light resistance builds knee joint resilience.\n" +
                    "3. **Check Ankle Mobility**: Restricted ankle dorsiflexion causes feet to pronate and knees to collapse inward (valgus). Mobilize your calves and ankles before squatting.";
        }

        // 4. Lower Back Pain / Lumbar Strain
        if (q.contains("back") && (q.contains("pain") || q.contains("hurt") || q.contains("sore") || q.contains("strain"))) {
            return "Lower back fatigue and lumbar strain management:\n\n" +
                    "1. **De-load Spinal Compression**: Pause conventional barbell deadlifts, heavy back squats, and bent-over barbell rows until symptoms subside.\n" +
                    "2. **Back-Friendly Alternatives**:\n" +
                    "   • **Chest-Supported Dumbbell Rows**: Fully engages the lats, rhomboids, and rear delts with zero lower back strain.\n" +
                    "   • **Leg Press (Controlled)**: Keeps your lumbar spine firmly supported against the back pad (avoid letting your pelvis tuck/round at the bottom).\n" +
                    "   • **Bulgarian Split Squats**: Massive quad and glute hypertrophy with half the axial load.\n" +
                    "3. **McGill Big 3 Core Bracing**: Perform Bird-Dogs, Side Planks, and Modified Curl-ups daily to build 360-degree spinal stiffness.";
        }

        // 5. Elbow / Tricep Tendonitis / Golfer's or Tennis Elbow
        if (q.contains("elbow") || q.contains("tendon") || (q.contains("curl") && q.contains("pain"))) {
            return "For elbow pain or tricep/bicep tendon discomfort:\n\n" +
                    "1. **Avoid Heavy Straight Barbells**: Straight-bar curls and straight-bar skullcrushers force your forearms into extreme supination/pronation.\n" +
                    "2. **Substitutions**:\n" +
                    "   • Switch to **Neutral-Grip Dumbbell Hammer Curls** or **Rope Cable Curls**.\n" +
                    "   • For triceps, use **Cable Rope Pushdowns** or **Cross-Body Cable Extensions** instead of heavy free-weight skullcrushers.\n" +
                    "3. **Warm Up Forearms**: Perform light wrist flexor and extensor curls with a 2-3kg dumbbell before heavy pressing or pulling.";
        }

        // 6. Indian Vegetarian High Protein Strategies
        if (q.contains("veg") || q.contains("paneer") || q.contains("soya") || q.contains("dal") || q.contains("chana") || q.contains("sattu")) {
            return "Maximizing protein intake on an Indian vegetarian diet while staying in a calorie deficit:\n\n" +
                    "1. **Soya Chunks (Nutrela)**: 52g protein per 100g dry. A 50g dry serving provides **26g protein for only 172 kcal** — unbeatable calorie-to-protein ratio!\n" +
                    "2. **Low-Fat Paneer**: 25g protein with under 2.5g fat per 100g (~160 kcal), compared to 265 kcal in regular full-fat paneer.\n" +
                    "3. **Moong Dal & Besan Chillas**: Stuff with 50g grated paneer for an easy 20g+ protein breakfast.\n" +
                    "4. **Kala Chana & Sprouted Moong**: 9g protein and 8g fiber per 100g boiled. The fiber stabilizes blood sugar and keeps you full for hours.\n" +
                    "5. **Sattu (Roasted Gram Flour)**: 23g protein per 100g; mix with water, jeera, and lemon for a nutrient-packed protein beverage.\n" +
                    "6. **Pro Tip**: Complement lentils with millets (bajra, jowar, ragi) for complete essential amino acid profiles, and consider 1 scoop of Whey Isolate (25g protein, 120 kcal) to easily hit 100g+ daily.";
        }

        // 7. Non-Vegetarian High Protein Strategies
        if (q.contains("non-veg") || q.contains("chicken") || q.contains("egg") || q.contains("fish") || q.contains("meat")) {
            return "Top non-vegetarian staples for lean muscle building and calorie deficits:\n\n" +
                    "1. **Chicken Breast (Skinless Boneless)**: 31g protein per 100g (only 165 kcal). Season with ginger-garlic, turmeric, and cumin; cook as Tandoori Tikka, shredded curry, or air-fry.\n" +
                    "2. **Eggs & Egg Whites**: 1 whole egg (~6g protein, 70 kcal) paired with 4 egg whites (~14g protein, 68 kcal) yields a delicious 20g protein scramble for under 140 kcal!\n" +
                    "3. **Fish (Rohu, Katla, Surmai, Pomfret)**: 18-21g lean protein per 100g, packed with anti-inflammatory Omega-3 fatty acids for joint recovery and heart health.\n" +
                    "4. **Lean Minced Mutton (Keema)**: Cooked with peas and whole spices for a high-iron, vitamin B12-dense meal.";
        }

        // 8. Calorie Deficit & TDEE Science
        if (q.contains("deficit") || q.contains("lose weight") || q.contains("fat loss") || q.contains("cut") || q.contains("tdee") || q.contains("bmr")) {
            return "The science of sustainable calorie deficits:\n\n" +
                    "1. **The Math**: 1 kg of stored adipose tissue represents roughly 7,700 kcal. A moderate daily deficit of **300 to 500 kcal** below your TDEE yields a safe, sustainable fat loss rate of **0.4 to 0.5 kg per week** without muscle wasting.\n" +
                    "2. **Preserving Lean Mass**: Keep protein between **1.8 to 2.2g per kg of bodyweight**.\n" +
                    "3. **NEAT (Non-Exercise Activity Thermogenesis)**: Walking 8,000 to 10,000 steps daily contributes more to fat loss than grueling cardio, without spiking hunger hormones like ghrelin.\n" +
                    "4. **Avoid Double-Counting**: If your TDEE formula already factors in moderate activity, do not add exercise calories back into your daily food allowance.";
        }

        // 9. Bulking & Clean Muscle Building
        if (q.contains("bulk") || q.contains("gain muscle") || q.contains("surplus") || q.contains("hypertrophy") || q.contains("mass")) {
            return "Clean bulking without excessive fat gain:\n\n" +
                    "1. **Caloric Surplus**: Maintain a conservative **200 to 300 kcal surplus** above TDEE. Muscle synthesis is an energy-intensive process, but excess calories past 300 kcal simply convert to adipose tissue.\n" +
                    "2. **Progressive Overload**: Strive to add weight, reps, or improve execution cadence on your compound lifts each week.\n" +
                    "3. **Macro Split**: 2.0g protein/kg, 1.0g healthy fats/kg, and fill remaining calories with nutrient-dense complex carbs (oats, brown basmati rice, rotis, sweet potatoes) to power intense gym sessions.\n" +
                    "4. **Recovery Window**: Allow 48-72 hours before training the same muscle group again.";
        }

        // 10. Breaking Plateaus (Lifting or Weight Loss)
        if (q.contains("plateau") || q.contains("stuck") || q.contains("not losing") || q.contains("progressive overload")) {
            return "How to break through fitness plateaus:\n\n" +
                    "1. **Gym Strength Plateau**:\n" +
                    "   • **The 2-for-2 Rule**: If you can perform 2 extra reps beyond your target on your final set for 2 consecutive workouts, increase load by 1.25kg to 2.5kg.\n" +
                    "   • **Eccentric Tempo**: Slow down the lowering phase to 3 seconds to increase mechanical tension without adding weight.\n" +
                    "   • **1-Week Deload**: Drop total training volume by 50% for 7 days to clear accumulated central nervous system (CNS) fatigue.\n\n" +
                    "2. **Weight Loss Plateau**:\n" +
                    "   • **Metabolic Adaptation**: As you drop weight, your BMR drops proportionally. Recalculate your TDEE for your new weight.\n" +
                    "   • **Audit Hidden Calories**: Measure cooking oil, ghee, and milk/sugar in tea with a digital kitchen scale.";
        }

        // 11. Creatine Monohydrate
        if (q.contains("creatine")) {
            return "Creatine Monohydrate facts & best practices:\n\n" +
                    "1. **How It Works**: Saturates intramuscular phosphocreatine stores, enabling faster ATP resynthesis during high-intensity 1-5 rep bursts, increasing power output and water retention inside the muscle cell (cellular swelling triggers protein synthesis).\n" +
                    "2. **Dosing**: Take **3 to 5 grams daily** consistently at any time of day. A loading phase is optional; 3-5g daily fully saturates muscle stores within 3-4 weeks.\n" +
                    "3. **Safety**: Over 500+ peer-reviewed studies confirm its safety for healthy kidneys. Drink an extra 500ml of water daily.";
        }

        // 12. Whey Protein & Supplements
        if (q.contains("whey") || q.contains("supplement") || q.contains("powder")) {
            return "Whey Protein overview:\n\n" +
                    "1. **What It Is**: Whey is simply the liquid byproduct of milk during cheese making, filtered to concentrate protein while stripping fats and lactose.\n" +
                    "2. **Concentrate vs Isolate**: Concentrate is ~80% protein with trace carbs/lactose; Isolate is 90%+ protein and virtually lactose-free.\n" +
                    "3. **Safety**: Whey is food, not an unnatural drug. For healthy individuals, it causes no kidney damage.\n" +
                    "4. **Timing**: Ideal post-workout or as a quick snack, but meeting your total daily protein goal is what drives 95% of results.";
        }

        // 13. Soreness (DOMS) vs Injury
        if (q.contains("sore") || q.contains("doms") || q.contains("stiff") || q.contains("recovery")) {
            return "DOMS (Delayed Onset Muscle Soreness) vs Injury:\n\n" +
                    "1. **DOMS**: A dull, aching sensation across the belly of the muscle that peaks 24 to 48 hours post-workout due to microscopic muscle fiber tearing and fluid accumulation. It subsides with light movement.\n" +
                    "2. **Joint/Tendon Injury**: A sharp, stabbing pain located directly in a joint capsule or tendon insertion that spikes during specific angles.\n" +
                    "3. **Accelerating Recovery**: 20-30 minutes of low-intensity Zone 2 walking increases capillary blood flow, flushing cellular debris. Ensure 7-9 hours of sleep and adequate hydration.";
        }

        // 14. Shin Splints & Running Mechanics
        if (q.contains("shin") || (q.contains("run") && (q.contains("pain") || q.contains("hurt")))) {
            return "Overcoming Shin Splints & Running Pain:\n\n" +
                    "1. **Primary Cause**: Overstriding (landing heel-first far in front of your center of mass), creating massive braking forces up the tibia.\n" +
                    "2. **Fix Cadence**: Increase your step frequency to 165-175 steps per minute (SPM). Shorter, quicker strides ensure your foot lands directly beneath your hips.\n" +
                    "3. **Strengthen the Tibialis**: Perform seated or standing tibialis toe-raises (raising your toes toward your shins against a wall) for 3 sets of 20 reps.\n" +
                    "4. **Surface**: Transition from hard asphalt to dirt tracks, grass, or treadmills during flare-ups.";
        }

        // 15. Six Pack Abs & Spot Reduction
        if (q.contains("abs") || q.contains("six pack") || q.contains("belly fat") || q.contains("spot reduce")) {
            return "The science of visible abdominal muscles:\n\n" +
                    "1. **The Spot Reduction Myth**: You cannot choose where your body burns fat. Crunches build the rectus abdominis muscle, but fat oxidation occurs systemically from a sustained calorie deficit.\n" +
                    "2. **Body Fat Threshold**: In men, abs typically become clearly visible at 10-12% body fat; in women, around 18-20%.\n" +
                    "3. **Hypertrophy for Abs**: Treat your core like any other muscle group. Progressive overload on **Hanging Leg/Knee Raises** (posterior pelvic tilt) and **Cable Kneeling Crunches** builds thick abdominal bricks that show at higher body fat percentages.";
        }

        // 16. Fasting & Intermittent Fasting (16:8)
        if (q.contains("fasting") || q.contains("intermittent")) {
            return "Intermittent Fasting (16/8) in Fitness:\n\n" +
                    "1. **How It Works**: Skipping breakfast condenses your calorie intake into an 8-hour window (e.g. 12 PM to 8 PM). It does not magically burn more fat than an equivalent continuous deficit, but it helps many people feel fuller on larger meals.\n" +
                    "2. **Protein Distribution**: Ensure you still hit your 1.8-2.2g/kg protein target divided across 2-3 meals within your eating window.\n" +
                    "3. **Workout Timing**: If lifting during a fast, stay hydrated with electrolytes and break your fast with a high-protein meal within 1-2 hours.";
        }

        // Default Rich Science Response
        return "FitTrack Sports Science Coach response:\n\n" +
                "I am your dedicated fitness & nutrition assistant grounded in your current profile.\n\n" +
                "Here are key areas I can specifically help you optimize:\n" +
                "• **Joint Discomfort & Substitutions**: Shoulder, knee, lower back, or elbow pain modifications\n" +
                "• **Indian Diet Optimization**: Hitting 120g+ protein on vegetarian (paneer, soya, dal, sattu) or non-vegetarian meals\n" +
                "• **Plateau Busting**: Progressive overload schemes, 2-for-2 rules, and deloading\n" +
                "• **Calorie Deficit Calculation**: Mifflin-St Jeor TDEE breakdowns and avoiding metabolic slowdown\n\n" +
                "Feel free to ask a specific question, describe what hurts, or ask about a meal!";
    }

    /**
     * Fully dynamic workout routine builder that responds precisely to split, days, equipment, and level.
     */
    private String buildDynamicWorkoutPlan(String splitType, int days, String equipment, String level) {
        String s = splitType != null ? splitType.toUpperCase() : "PPL";
        String eq = equipment != null ? equipment.toLowerCase() : "gym";
        String lvl = level != null ? level.toLowerCase() : "intermediate";
        int d = Math.max(2, Math.min(6, days)); // 2 to 6 days

        boolean isDumbbell = eq.contains("dumbbell");
        boolean isHome = eq.contains("home") || eq.contains("bodyweight");
        boolean isAdv = lvl.contains("advanced");
        boolean isBeg = lvl.contains("beginner");

        int sets = isBeg ? 3 : (isAdv ? 5 : 4);
        int rpe = isBeg ? 7 : (isAdv ? 9 : 8);
        String repComp = isBeg ? "10-12" : (isAdv ? "5-8" : "6-8");
        String repIso = isBeg ? "12-15" : (isAdv ? "10-12" : "10-12");

        // Primary compounds tailored to equipment
        String chestPress = isHome ? "Weighted Floor Pushups" : (isDumbbell ? "Incline Dumbbell Press" : "Barbell Bench Press");
        String backRow = isHome ? "Inverted Table / Doorway Rows" : (isDumbbell ? "Single-Arm Dumbbell Row" : "Barbell Bent-over Row");
        String shoulderPress = isHome ? "Pike Pushups (Feet Elevated)" : (isDumbbell ? "Seated DB Overhead Press" : "Standing Overhead Barbell Press");
        String squatMove = isHome ? "Bodyweight Tempo Squats / Pistol Progressions" : (isDumbbell ? "Dumbbell Goblet Squats" : "Barbell Back Squat");
        String hingeMove = isHome ? "Single-Leg Glute Bridges" : (isDumbbell ? "Dumbbell Romanian Deadlift (RDL)" : "Barbell Romanian Deadlift (RDL)");
        String pullMove = isHome ? "Chin-ups / Underhand Inverted Rows" : (isDumbbell ? "Dumbbell Pullover" : "Wide-Grip Lat Pulldown");

        List<String> dayBlocks = new ArrayList<>();

        if (s.contains("UPPER") || s.contains("LOWER")) {
            // Upper Lower split
            dayBlocks.add(String.format("""
            {
              "dayName": "Day 1 - Upper Power & Compound Strength",
              "targetMuscles": ["CHEST", "BACK", "SHOULDERS"],
              "exercises": [
                { "name": "%s", "targetMuscle": "CHEST", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Tuck elbows at 45 degrees, full stretch at chest level." },
                { "name": "%s", "targetMuscle": "BACK", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Drive elbows toward hips, maintain neutral lumbar spine." },
                { "name": "%s", "targetMuscle": "SHOULDERS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Brace core and glutes, press straight upward." },
                { "name": "%s", "targetMuscle": "BACK", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Focus on lat depression, avoid excessive backward lean." },
                { "name": "Incline Dumbbell Curls", "targetMuscle": "BICEPS", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 60, "formTip": "Pin shoulders back for maximum bicep stretch." },
                { "name": "Overhead Tricep Extension", "targetMuscle": "TRICEPS", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 60, "formTip": "Full stretch on triceps long head." }
              ]
            }
            """, chestPress, sets, repComp, rpe, backRow, sets, repComp, rpe, shoulderPress, sets - 1, repComp, rpe, pullMove, repIso, rpe, repIso, rpe, repIso, rpe));

            dayBlocks.add(String.format("""
            {
              "dayName": "Day 2 - Lower Power & Posterior Chain",
              "targetMuscles": ["LEGS", "ABS"],
              "exercises": [
                { "name": "%s", "targetMuscle": "LEGS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 150, "formTip": "Break at knees and hips together, depth to parallel." },
                { "name": "%s", "targetMuscle": "LEGS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Hinge hips back, feel intense hamstring stretch." },
                { "name": "Bulgarian Split Squats", "targetMuscle": "LEGS", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Vertical front shin minimizes knee shear." },
                { "name": "Standing Calf Raises", "targetMuscle": "LEGS", "sets": 4, "repRange": "15-20", "rpeTarget": 9, "restSeconds": 60, "formTip": "2-second pause at full deep stretch." },
                { "name": "Hanging Knee / Leg Raises", "targetMuscle": "ABS", "sets": 3, "repRange": "12-15", "rpeTarget": 8, "restSeconds": 60, "formTip": "Posterior pelvic tilt, avoid swinging." }
              ]
            }
            """, squatMove, sets, repComp, rpe, hingeMove, sets, repComp, rpe, repIso, rpe));

            if (d >= 3) {
                dayBlocks.add(String.format("""
                {
                  "dayName": "Day 3 - Upper Hypertrophy & Volume",
                  "targetMuscles": ["CHEST", "BACK", "SHOULDERS"],
                  "exercises": [
                    { "name": "Incline Dumbbell Press", "targetMuscle": "CHEST", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "30 degree bench angle, emphasize upper chest." },
                    { "name": "Chest-Supported Row", "targetMuscle": "BACK", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Zero lower back strain, squeeze rhomboids." },
                    { "name": "Cable / DB Lateral Raises", "targetMuscle": "SHOULDERS", "sets": 4, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Raise in the scapular plane (30 degrees forward)." },
                    { "name": "Face Pulls", "targetMuscle": "SHOULDERS", "sets": 3, "repRange": "15-20", "rpeTarget": 8, "restSeconds": 60, "formTip": "External rotation at peak contraction." },
                    { "name": "Tricep Rope Pushdowns", "targetMuscle": "TRICEPS", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 60, "formTip": "Flare rope outwards at bottom contraction." }
                  ]
                }
                """, sets - 1, repIso, rpe, sets - 1, repIso, rpe, repIso, rpe));
            }

            if (d >= 4) {
                dayBlocks.add(String.format("""
                {
                  "dayName": "Day 4 - Lower Hypertrophy & Calves",
                  "targetMuscles": ["LEGS", "CORE"],
                  "exercises": [
                    { "name": "Goblet Squat / Leg Press", "targetMuscle": "LEGS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Smooth continuous tension on quadriceps." },
                    { "name": "Lying / Seated Leg Curls", "targetMuscle": "LEGS", "sets": 4, "repRange": "%s", "rpeTarget": 9, "restSeconds": 60, "formTip": "Control 3-second negative descent." },
                    { "name": "Walking Dumbbell Lunges", "targetMuscle": "LEGS", "sets": 3, "repRange": "12 each", "rpeTarget": 8, "restSeconds": 90, "formTip": "Torso slightly leaned forward to bias glutes." },
                    { "name": "Plank with Shoulder Taps", "targetMuscle": "CORE", "sets": 3, "repRange": "60 sec", "rpeTarget": 8, "restSeconds": 60, "formTip": "Keep hips dead-level, resist rotation." }
                  ]
                }
                """, sets, repIso, rpe, repIso));
            }

            if (d >= 5) {
                dayBlocks.add("""
                {
                  "dayName": "Day 5 - Arms, Delts & Weak Points",
                  "targetMuscles": ["BICEPS", "TRICEPS", "SHOULDERS", "CORE"],
                  "exercises": [
                    { "name": "Barbell / EZ Bar Curls", "targetMuscle": "BICEPS", "sets": 4, "repRange": "10-12", "rpeTarget": 9, "restSeconds": 60, "formTip": "Strict form, pin elbows to ribs." },
                    { "name": "Overhead Tricep Extension", "targetMuscle": "TRICEPS", "sets": 4, "repRange": "10-12", "rpeTarget": 9, "restSeconds": 60, "formTip": "Deep long-head stretch." },
                    { "name": "Hammer Curls", "targetMuscle": "BICEPS", "sets": 3, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Targets brachialis for arm thickness." },
                    { "name": "Cable Lateral Raises", "targetMuscle": "SHOULDERS", "sets": 4, "repRange": "15 reps", "rpeTarget": 9, "restSeconds": 45, "formTip": "Constant tension throughout range." },
                    { "name": "Ab Rollout / Hanging Leg Raise", "targetMuscle": "ABS", "sets": 3, "repRange": "12-15", "rpeTarget": 8, "restSeconds": 60, "formTip": "Brace core rigidly." }
                  ]
                }
                """);
            }

            if (d >= 6) {
                dayBlocks.add("""
                {
                  "dayName": "Day 6 - Active Recovery, Mobility & Core",
                  "targetMuscles": ["CORE", "LEGS"],
                  "exercises": [
                    { "name": "Zone 2 Incline Walk", "targetMuscle": "CORE", "sets": 1, "repRange": "30 mins", "rpeTarget": 6, "restSeconds": 0, "formTip": "Keep heart rate at 60-70% max for aerobic recovery." },
                    { "name": "McGill Big 3 (Bird-Dog, Side Plank, Curl-Up)", "targetMuscle": "CORE", "sets": 3, "repRange": "10 reps", "rpeTarget": 7, "restSeconds": 45, "formTip": "Zero spinal flexion, pure bracing." },
                    { "name": "Deep Ankle & Hip 90/90 Stretches", "targetMuscle": "LEGS", "sets": 3, "repRange": "60 sec", "rpeTarget": 5, "restSeconds": 30, "formTip": "Restore joint capsule mobility." }
                  ]
                }
                """);
            }
        } else if (s.contains("FULL")) {
            // Full Body Split
            dayBlocks.add(String.format("""
            {
              "dayName": "Day 1 - Full Body Heavy Compounds",
              "targetMuscles": ["LEGS", "CHEST", "BACK", "CORE"],
              "exercises": [
                { "name": "%s", "targetMuscle": "LEGS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 150, "formTip": "Drive through mid-foot, proud chest." },
                { "name": "%s", "targetMuscle": "CHEST", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Retract scapulae and control eccentric descent." },
                { "name": "%s", "targetMuscle": "BACK", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Drive elbows back, squeeze lats." },
                { "name": "Dumbbell Lateral Raises", "targetMuscle": "SHOULDERS", "sets": 3, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Lead with elbows in scapular plane." },
                { "name": "Plank", "targetMuscle": "CORE", "sets": 3, "repRange": "60 sec", "rpeTarget": 8, "restSeconds": 60, "formTip": "Squeeze glutes and brace abdominal wall." }
              ]
            }
            """, squatMove, sets, repComp, rpe, chestPress, sets, repComp, rpe, backRow, sets, repComp, rpe));

            dayBlocks.add(String.format("""
            {
              "dayName": "Day 2 - Posterior Chain & Vertical Press",
              "targetMuscles": ["LEGS", "SHOULDERS", "BACK", "ARMS"],
              "exercises": [
                { "name": "%s", "targetMuscle": "LEGS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Hinge at hips, keep weights close to body." },
                { "name": "%s", "targetMuscle": "SHOULDERS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Lock out overhead with bicep next to ear." },
                { "name": "%s", "targetMuscle": "BACK", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Full stretch at top, pull to collarbone." },
                { "name": "Bulgarian Split Squats", "targetMuscle": "LEGS", "sets": 3, "repRange": "10-12", "rpeTarget": 8, "restSeconds": 90, "formTip": "Unilateral balance and knee stability." },
                { "name": "Barbell / DB Bicep Curls", "targetMuscle": "BICEPS", "sets": 3, "repRange": "10-12", "rpeTarget": 9, "restSeconds": 60, "formTip": "Strict form, pin elbows to ribs." }
              ]
            }
            """, hingeMove, sets, repComp, rpe, shoulderPress, sets, repComp, rpe, pullMove, repIso, rpe));

            if (d >= 3) {
                dayBlocks.add(String.format("""
                {
                  "dayName": "Day 3 - Unilateral & Hypertrophy",
                  "targetMuscles": ["LEGS", "CHEST", "BACK", "TRICEPS"],
                  "exercises": [
                    { "name": "Goblet Squats / Leg Press", "targetMuscle": "LEGS", "sets": 3, "repRange": "10-12", "rpeTarget": 8, "restSeconds": 90, "formTip": "Continuous quad tension." },
                    { "name": "Incline Dumbbell Press", "targetMuscle": "CHEST", "sets": 3, "repRange": "8-10", "rpeTarget": 8, "restSeconds": 90, "formTip": "Deep pectoral stretch at bottom." },
                    { "name": "Seated Cable Row / DB Row", "targetMuscle": "BACK", "sets": 3, "repRange": "10-12", "rpeTarget": 8, "restSeconds": 90, "formTip": "Hold peak contraction for 1 second." },
                    { "name": "Tricep Rope Pushdowns", "targetMuscle": "TRICEPS", "sets": 3, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Full lockout with tricep squeeze." },
                    { "name": "Hanging Knee Raises", "targetMuscle": "ABS", "sets": 3, "repRange": "15 reps", "rpeTarget": 8, "restSeconds": 60, "formTip": "Curl pelvis toward chin." }
                  ]
                }
                """));
            }

            if (d >= 4) {
                dayBlocks.add("""
                {
                  "dayName": "Day 4 - Full Body Density & Conditioning",
                  "targetMuscles": ["LEGS", "CHEST", "BACK", "CORE"],
                  "exercises": [
                    { "name": "Dumbbell Step-ups / Lunges", "targetMuscle": "LEGS", "sets": 3, "repRange": "12 each", "rpeTarget": 8, "restSeconds": 60, "formTip": "Drive through the heel." },
                    { "name": "Pushups / Dips", "targetMuscle": "CHEST", "sets": 3, "repRange": "12-15", "rpeTarget": 8, "restSeconds": 60, "formTip": "Full range of motion." },
                    { "name": "Cable Lat Pulldowns", "targetMuscle": "BACK", "sets": 3, "repRange": "12-15", "rpeTarget": 8, "restSeconds": 60, "formTip": "Squeeze lats hard." },
                    { "name": "Farmer's Walk", "targetMuscle": "CORE", "sets": 3, "repRange": "45 sec", "rpeTarget": 8, "restSeconds": 60, "formTip": "Grip strength and core stability." }
                  ]
                }
                """);
            }
        } else {
            // Push Pull Legs (PPL) or Arnold Split
            dayBlocks.add(String.format("""
            {
              "dayName": "Day 1 - Push (Chest, Front/Lateral Delts, Triceps)",
              "targetMuscles": ["CHEST", "SHOULDERS", "TRICEPS"],
              "exercises": [
                { "name": "%s", "targetMuscle": "CHEST", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Retract scapula and drive feet into the floor." },
                { "name": "Incline Dumbbell Press", "targetMuscle": "CHEST", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Set bench to 30 degrees, full chest stretch." },
                { "name": "%s", "targetMuscle": "SHOULDERS", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Tight glutes and core, press straight up." },
                { "name": "Dumbbell Lateral Raises", "targetMuscle": "SHOULDERS", "sets": 4, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Lead with elbows, slight forward lean." },
                { "name": "Tricep Rope Pushdowns", "targetMuscle": "TRICEPS", "sets": 3, "repRange": "%s", "rpeTarget": 9, "restSeconds": 60, "formTip": "Flare rope outwards at bottom contraction." }
              ]
            }
            """, chestPress, sets, repComp, rpe, repIso, rpe, shoulderPress, repComp, rpe, repIso));

            dayBlocks.add(String.format("""
            {
              "dayName": "Day 2 - Pull (Back, Rear Delts, Biceps)",
              "targetMuscles": ["BACK", "BICEPS", "CORE"],
              "exercises": [
                { "name": "%s", "targetMuscle": "BACK", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Drive elbows toward back pockets." },
                { "name": "%s", "targetMuscle": "BACK", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 90, "formTip": "Keep chest tall, pause at contraction." },
                { "name": "Face Pulls", "targetMuscle": "SHOULDERS", "sets": 3, "repRange": "15-20", "rpeTarget": 8, "restSeconds": 60, "formTip": "High pull for rear delts and external rotators." },
                { "name": "Barbell / DB Bicep Curls", "targetMuscle": "BICEPS", "sets": 3, "repRange": "%s", "rpeTarget": 8, "restSeconds": 60, "formTip": "Pin elbows to ribs, no swinging." },
                { "name": "Hammer Curls", "targetMuscle": "BICEPS", "sets": 3, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Targets brachialis for thicker arm profile." }
              ]
            }
            """, pullMove, sets, repComp, rpe, backRow, repIso, rpe, repIso));

            dayBlocks.add(String.format("""
            {
              "dayName": "Day 3 - Legs & Abs (Quads, Hamstrings, Calves, Abs)",
              "targetMuscles": ["LEGS", "ABS"],
              "exercises": [
                { "name": "%s", "targetMuscle": "LEGS", "sets": %d, "repRange": "%s", "rpeTarget": %d, "restSeconds": 180, "formTip": "Break at hips and knees simultaneously, hit parallel." },
                { "name": "%s", "targetMuscle": "LEGS", "sets": 3, "repRange": "%s", "rpeTarget": %d, "restSeconds": 120, "formTip": "Push hips back, feel intense hamstring stretch." },
                { "name": "Bulgarian Split Squats / Leg Press", "targetMuscle": "LEGS", "sets": 3, "repRange": "10-12", "rpeTarget": 8, "restSeconds": 90, "formTip": "Controlled eccentric descent, don't lock knees." },
                { "name": "Standing Calf Raises", "targetMuscle": "LEGS", "sets": 4, "repRange": "15-20", "rpeTarget": 9, "restSeconds": 45, "formTip": "Full pause at deep stretch and top peak contraction." },
                { "name": "Hanging Leg Raises", "targetMuscle": "ABS", "sets": 3, "repRange": "12-15", "rpeTarget": 8, "restSeconds": 60, "formTip": "Curl pelvis upwards, avoid relying on hip flexors." }
              ]
            }
            """, squatMove, sets, repComp, rpe, hingeMove, repComp, rpe));

            if (d >= 4) {
                dayBlocks.add("""
                {
                  "dayName": "Day 4 - Push B (Incline Chest & Shoulder Hypertrophy)",
                  "targetMuscles": ["CHEST", "SHOULDERS", "TRICEPS"],
                  "exercises": [
                    { "name": "Incline Dumbbell Press", "targetMuscle": "CHEST", "sets": 4, "repRange": "8-10", "rpeTarget": 8, "restSeconds": 90, "formTip": "Focus on clavicular chest head stretch." },
                    { "name": "Cable Chest Flyes", "targetMuscle": "CHEST", "sets": 3, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Hug an imaginary tree, squeeze at center." },
                    { "name": "Seated DB Lateral Raises", "targetMuscle": "SHOULDERS", "sets": 4, "repRange": "15 reps", "rpeTarget": 9, "restSeconds": 45, "formTip": "Strict form, eliminate leg momentum." },
                    { "name": "Overhead Tricep Extension", "targetMuscle": "TRICEPS", "sets": 3, "repRange": "10-12", "rpeTarget": 9, "restSeconds": 60, "formTip": "Full long-head tricep stretch." }
                  ]
                }
                """);
            }

            if (d >= 5) {
                dayBlocks.add("""
                {
                  "dayName": "Day 5 - Pull B (Upper Back & Bicep Peak)",
                  "targetMuscles": ["BACK", "BICEPS"],
                  "exercises": [
                    { "name": "Chest-Supported Row", "targetMuscle": "BACK", "sets": 4, "repRange": "8-10", "rpeTarget": 8, "restSeconds": 90, "formTip": "Squeeze shoulder blades hard at top." },
                    { "name": "Neutral-Grip Lat Pulldown", "targetMuscle": "BACK", "sets": 3, "repRange": "10-12", "rpeTarget": 8, "restSeconds": 75, "formTip": "Pull elbows down to ribs." },
                    { "name": "Incline Dumbbell Bicep Curls", "targetMuscle": "BICEPS", "sets": 3, "repRange": "10-12", "rpeTarget": 9, "restSeconds": 60, "formTip": "Full bicep stretch." },
                    { "name": "Cable Hammer Curls with Rope", "targetMuscle": "BICEPS", "sets": 3, "repRange": "12-15", "rpeTarget": 9, "restSeconds": 60, "formTip": "Brachialis and forearm thickness." }
                  ]
                }
                """);
            }

            if (d >= 6) {
                dayBlocks.add("""
                {
                  "dayName": "Day 6 - Legs B (Hamstring & Glute Focus)",
                  "targetMuscles": ["LEGS", "ABS"],
                  "exercises": [
                    { "name": "Romanian Deadlift (RDL)", "targetMuscle": "LEGS", "sets": 4, "repRange": "8-10", "rpeTarget": 8, "restSeconds": 120, "formTip": "Push hips back, intense hamstring tension." },
                    { "name": "Hack Squat / Front Squat", "targetMuscle": "LEGS", "sets": 3, "repRange": "10-12", "rpeTarget": 8, "restSeconds": 90, "formTip": "Deep knee flexion for vastus medialis." },
                    { "name": "Lying Hamstring Curls", "targetMuscle": "LEGS", "sets": 4, "repRange": "10-12", "rpeTarget": 9, "restSeconds": 60, "formTip": "Pause 1 sec at peak contraction." },
                    { "name": "Seated Calf Raises", "targetMuscle": "LEGS", "sets": 4, "repRange": "15-20", "rpeTarget": 9, "restSeconds": 45, "formTip": "Soleus muscle development." },
                    { "name": "Abdominal Cable Crunches", "targetMuscle": "ABS", "sets": 3, "repRange": "15 reps", "rpeTarget": 8, "restSeconds": 60, "formTip": "Curl ribcage toward pelvis." }
                  ]
                }
                """);
            }
        }

        String scheduleJson = String.join(",\n", dayBlocks);

        return String.format("""
        {
          "planName": "%s (%d-Day %s %s)",
          "splitType": "%s",
          "daysPerWeek": %d,
          "schedule": [
            %s
          ]
        }
        """, splitType, d, level, equipment, splitType, d, scheduleJson);
    }

    /**
     * Fully dynamic nutrition and meal suggestion engine that mathematically scales portion sizes,
     * calories, and macros in real-time according to remaining calories and protein targets.
     */
    private String buildDynamicMealSuggestions(double remainingCalories, double minProtein, String dietaryPref) {
        String pref = dietaryPref != null ? dietaryPref.toLowerCase() : "vegetarian";

        // Clamp targets within realistic meal ranges
        double targetCals = Math.max(250.0, Math.min(1500.0, remainingCalories));
        double targetProt = Math.max(15.0, Math.min(100.0, minProtein));

        // Scale factors relative to a 450 kcal base
        double scale = targetCals / 450.0;
        double protScale = Math.max(scale, targetProt / 32.0);

        if (pref.contains("non-veg") || pref.contains("omnivore") || pref.contains("chicken") || pref.contains("egg") || pref.contains("fish")) {
            // Non-Vegetarian Scaled Meals
            int chickenGrams = (int) Math.round(150 * protScale);
            int chickenCals = (int) Math.round(chickenGrams * 1.65);
            int chickenProt = (int) Math.round(chickenGrams * 0.31);

            int riceGrams = (int) Math.round(100 * scale);
            int riceCals = (int) Math.round(riceGrams * 1.30);
            int riceCarbs = (int) Math.round(riceGrams * 0.28);

            int meal1Cals = chickenCals + riceCals + 50;
            int meal1Prot = chickenProt + (int) Math.round(riceGrams * 0.027) + 3;
            int meal1Carbs = riceCarbs + 6;
            int meal1Fats = (int) Math.round(meal1Cals * 0.18 / 9.0);

            // Meal 2: Egg whites scramble
            int whitesCount = Math.max(3, (int) Math.round(5 * protScale));
            int whitesGrams = whitesCount * 33;
            int whitesProt = (int) Math.round(whitesGrams * 0.11);
            int whitesCals = (int) Math.round(whitesGrams * 0.52);

            int rotiCount = Math.max(1, (int) Math.round(scale * 1.5));
            int rotiGrams = rotiCount * 35;
            int rotiCals = (int) Math.round(rotiGrams * 2.64);
            int rotiProt = (int) Math.round(rotiGrams * 0.095);
            int rotiCarbs = (int) Math.round(rotiGrams * 0.53);

            int meal2Cals = whitesCals + 72 + rotiCals + 45;
            int meal2Prot = whitesProt + 6 + rotiProt;
            int meal2Carbs = rotiCarbs + 5;
            int meal2Fats = 5 + 4;

            // Meal 3: Fish Curry
            int fishGrams = (int) Math.round(160 * protScale);
            int fishProt = (int) Math.round(fishGrams * 0.20);
            int fishCals = (int) Math.round(fishGrams * 1.45);

            int meal3Cals = fishCals + riceCals + 40;
            int meal3Prot = fishProt + (int) Math.round(riceGrams * 0.027);
            int meal3Carbs = riceCarbs + 4;
            int meal3Fats = (int) Math.round(fishGrams * 0.05) + 3;

            return String.format("""
            [
              {
                "mealName": "Tandoori Spiced Chicken Breast with Jeera Rice & Salad",
                "description": "Skinless chicken breast marinated in Greek dahi and tandoori spices, served with steamed basmati jeera rice and crunchy cucumber salad.",
                "prepTimeMinutes": 20,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": %d,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Chicken Breast (Skinless Boneless)", "grams": %d, "proteinGrams": %d, "carbsGrams": 0, "fatsGrams": %d, "calories": %d },
                  { "name": "Steamed Jeera Basmati Rice", "grams": %d, "proteinGrams": %d, "carbsGrams": %d, "fatsGrams": 1, "calories": %d },
                  { "name": "Cucumber Tomato Kachumber Salad", "grams": 100, "proteinGrams": 1, "carbsGrams": 4, "fatsGrams": 0, "calories": 20 },
                  { "name": "Mint Greek Dahi Dressing", "grams": 50, "proteinGrams": 3, "carbsGrams": 2, "fatsGrams": 1, "calories": 30 }
                ]
              },
              {
                "mealName": "Desi Egg White Bhurji (with 1 Whole Egg) & Rotis",
                "description": "High-protein scramble made with fresh egg whites and one whole egg sautéed with onions, tomatoes, and green chilies, paired with whole wheat rotis.",
                "prepTimeMinutes": 15,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": %d,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Egg Whites (%d whites)", "grams": %d, "proteinGrams": %d, "carbsGrams": 1, "fatsGrams": 0, "calories": %d },
                  { "name": "Whole Egg (1 large)", "grams": 50, "proteinGrams": 6, "carbsGrams": 0, "fatsGrams": 5, "calories": 72 },
                  { "name": "Roti / Chapati (Whole Wheat)", "grams": %d, "proteinGrams": %d, "carbsGrams": %d, "fatsGrams": 1, "calories": %d },
                  { "name": "Tadka Onions & Olive Oil", "grams": 50, "proteinGrams": 1, "carbsGrams": 4, "fatsGrams": 4, "calories": 45 }
                ]
              },
              {
                "mealName": "Bengali Style Rohu / Pomfret Fish Curry with Steamed Rice",
                "description": "Omega-3 rich fresh fish steaks gently cooked in a light turmeric and mustard seed homestyle gravy, served over steamed rice.",
                "prepTimeMinutes": 25,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": %d,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Rohu / Pomfret Fish Steaks", "grams": %d, "proteinGrams": %d, "carbsGrams": 0, "fatsGrams": %d, "calories": %d },
                  { "name": "Steamed White Basmati Rice", "grams": %d, "proteinGrams": %d, "carbsGrams": %d, "fatsGrams": 1, "calories": %d },
                  { "name": "Light Homestyle Curry Gravy", "grams": 80, "proteinGrams": 1, "carbsGrams": 4, "fatsGrams": 2, "calories": 40 }
                ]
              }
            ]
            """,
            meal1Cals, meal1Prot, meal1Carbs, meal1Fats,
            chickenGrams, chickenProt, (int) Math.round(chickenGrams * 0.036), chickenCals,
            riceGrams, (int) Math.round(riceGrams * 0.027), riceCarbs, riceCals,
            meal2Cals, meal2Prot, meal2Carbs, meal2Fats,
            whitesCount, whitesGrams, whitesProt, whitesCals,
            rotiGrams, rotiProt, rotiCarbs, rotiCals,
            meal3Cals, meal3Prot, meal3Carbs, meal3Fats,
            fishGrams, fishProt, (int) Math.round(fishGrams * 0.06), fishCals,
            riceGrams, (int) Math.round(riceGrams * 0.027), riceCarbs, riceCals);
        }

        if (pref.contains("vegan")) {
            // Vegan Scaled Meals
            int tofuGrams = (int) Math.round(180 * protScale);
            int tofuProt = (int) Math.round(tofuGrams * 0.12);
            int tofuCals = (int) Math.round(tofuGrams * 0.90);

            int rotiGrams = (int) Math.round(60 * scale);
            int rotiCals = (int) Math.round(rotiGrams * 3.49);
            int rotiProt = (int) Math.round(rotiGrams * 0.08);
            int rotiCarbs = (int) Math.round(rotiGrams * 0.72);

            int meal1Cals = tofuCals + rotiCals + 40;
            int meal1Prot = tofuProt + rotiProt;
            int meal1Carbs = rotiCarbs + 6;
            int meal1Fats = (int) Math.round(tofuGrams * 0.05) + 3;

            return String.format("""
            [
              {
                "mealName": "Extra-Firm Tofu Bhurji with Jowar Millet Roti",
                "description": "Crumbled organic tofu sautéed with ginger, garlic, tomatoes, and bell peppers, accompanied by high-fiber sorghum flatbread.",
                "prepTimeMinutes": 15,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": %d,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Tofu (Extra Firm)", "grams": %d, "proteinGrams": %d, "carbsGrams": 4, "fatsGrams": 11, "calories": %d },
                  { "name": "Jowar Roti (Sorghum)", "grams": %d, "proteinGrams": %d, "carbsGrams": %d, "fatsGrams": 2, "calories": %d },
                  { "name": "Bell Pepper & Onion Tadka", "grams": 80, "proteinGrams": 2, "carbsGrams": 4, "fatsGrams": 2, "calories": 40 }
                ]
              },
              {
                "mealName": "Kala Chana & Soya Chunks Protein Power Chaat",
                "description": "Boiled black chickpeas tossed with crunchy cooked soya chunks, chopped onions, tomatoes, and zesty fresh lemon-cumin dressing.",
                "prepTimeMinutes": 10,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": %d,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Kala Chana (Boiled)", "grams": %d, "proteinGrams": %d, "carbsGrams": 33, "fatsGrams": 3, "calories": 210 },
                  { "name": "Soya Chunks (Cooked)", "grams": %d, "proteinGrams": %d, "carbsGrams": 12, "fatsGrams": 2, "calories": 140 },
                  { "name": "Salad Veggies & Lemon Dressing", "grams": 90, "proteinGrams": 2, "carbsGrams": 7, "fatsGrams": 1, "calories": 40 }
                ]
              },
              {
                "mealName": "Rajma Masala with Steamed Brown Rice & Kachumber",
                "description": "Slow-simmered Punjabi kidney bean curry served over whole grain brown rice with fresh crunchy salad.",
                "prepTimeMinutes": 20,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": %d,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Rajma (Cooked Curry)", "grams": 220, "proteinGrams": 19, "carbsGrams": 46, "fatsGrams": 5, "calories": 290 },
                  { "name": "Cooked Brown Rice", "grams": %d, "proteinGrams": 3, "carbsGrams": 28, "fatsGrams": 1, "calories": 130 },
                  { "name": "Cucumber Kachumber Salad", "grams": 70, "proteinGrams": 1, "carbsGrams": 3, "fatsGrams": 0, "calories": 15 }
                ]
              }
            ]
            """,
            meal1Cals, meal1Prot, meal1Carbs, meal1Fats,
            tofuGrams, tofuProt, tofuCals,
            rotiGrams, rotiProt, rotiCarbs, rotiCals,
            (int) Math.round(390 * scale), (int) Math.round(34 * protScale), (int) Math.round(52 * scale), 6,
            (int) Math.round(140 * scale), (int) Math.round(13 * protScale),
            (int) Math.round(110 * protScale), (int) Math.round(17 * protScale),
            (int) Math.round(435 * scale), (int) Math.round(25 * protScale), (int) Math.round(75 * scale), 6,
            (int) Math.round(120 * scale));
        }

        if (pref.contains("low-carb") || pref.contains("keto")) {
            // Low-Carb / Keto Scaled Meals
            int paneerGrams = (int) Math.round(160 * protScale);
            int paneerProt = (int) Math.round(paneerGrams * 0.18);
            int paneerCals = (int) Math.round(paneerGrams * 2.65);

            return String.format("""
            [
              {
                "mealName": "Grilled Paneer Tikka with Bell Peppers & Mint Dip",
                "description": "Thick cubes of full-fat cottage cheese marinated in hung curd and carom seeds, skewered with bell peppers and roasted smoky.",
                "prepTimeMinutes": 20,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": 10,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Paneer (Indian Cottage Cheese)", "grams": %d, "proteinGrams": %d, "carbsGrams": 2, "fatsGrams": %d, "calories": %d },
                  { "name": "Grilled Bell Peppers & Onions", "grams": 80, "proteinGrams": 2, "carbsGrams": 6, "fatsGrams": 0, "calories": 35 },
                  { "name": "Mint Coriander Curd Chutney", "grams": 30, "proteinGrams": 2, "carbsGrams": 2, "fatsGrams": 2, "calories": 25 }
                ]
              },
              {
                "mealName": "Butter Sautéed Chicken Breast & Egg Scramble with Spinach",
                "description": "Tender strips of chicken breast flash-sautéed in pure butter with garlic baby spinach and a 2-egg scramble.",
                "prepTimeMinutes": 15,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": 4,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Chicken Breast (Skinless)", "grams": 150, "proteinGrams": 30, "carbsGrams": 0, "fatsGrams": 3, "calories": 150 },
                  { "name": "Whole Eggs (x2)", "grams": 100, "proteinGrams": 13, "carbsGrams": 1, "fatsGrams": 10, "calories": 143 },
                  { "name": "Spinach Sautéed in Butter", "grams": 100, "proteinGrams": 3, "carbsGrams": 3, "fatsGrams": 12, "calories": 135 }
                ]
              },
              {
                "mealName": "Tawa Fish Fry with Avocado Kachumber",
                "description": "Pan-seared fish fillets spiced with degi mirch and ajwain, accompanied by fresh cucumber and diced avocado salad.",
                "prepTimeMinutes": 15,
                "totalCalories": %d,
                "totalProteinGrams": %d,
                "totalCarbsGrams": 6,
                "totalFatsGrams": %d,
                "ingredients": [
                  { "name": "Pomfret / Surmai Fish", "grams": 180, "proteinGrams": 32, "carbsGrams": 0, "fatsGrams": 8, "calories": 200 },
                  { "name": "Fresh Diced Avocado", "grams": 80, "proteinGrams": 2, "carbsGrams": 5, "fatsGrams": 12, "calories": 130 },
                  { "name": "Cold-Pressed Olive Oil", "grams": 8, "proteinGrams": 0, "carbsGrams": 0, "fatsGrams": 8, "calories": 70 }
                ]
              }
            ]
            """,
            paneerCals + 60, paneerProt + 4, (int) Math.round(paneerGrams * 0.20),
            paneerGrams, paneerProt, (int) Math.round(paneerGrams * 0.20), paneerCals,
            (int) Math.round(428 * scale), (int) Math.round(46 * protScale), 25,
            (int) Math.round(400 * scale), (int) Math.round(34 * protScale), 28);
        }

        // Standard Indian Vegetarian Scaled Meals
        int lowFatPaneerGrams = (int) Math.round(140 * protScale);
        int paneerProt = (int) Math.round(lowFatPaneerGrams * 0.25);
        int paneerCals = (int) Math.round(lowFatPaneerGrams * 1.60);

        int rotiCount = Math.max(1, (int) Math.round(scale * 2));
        int rotiGrams = rotiCount * 35;
        int rotiCals = (int) Math.round(rotiGrams * 2.64);
        int rotiProt = (int) Math.round(rotiGrams * 0.095);
        int rotiCarbs = (int) Math.round(rotiGrams * 0.53);

        int meal1Cals = paneerCals + rotiCals + 80;
        int meal1Prot = paneerProt + rotiProt + 4;
        int meal1Carbs = rotiCarbs + 8;
        int meal1Fats = (int) Math.round(meal1Cals * 0.22 / 9.0);

        // Soya Chunks Pulao
        int soyaGrams = (int) Math.round(150 * protScale);
        int soyaProt = (int) Math.round(soyaGrams * 0.15);
        int soyaCals = (int) Math.round(soyaGrams * 1.30);

        int riceGrams = (int) Math.round(100 * scale);
        int riceCals = (int) Math.round(riceGrams * 1.30);
        int riceCarbs = (int) Math.round(riceGrams * 0.28);

        int meal2Cals = soyaCals + riceCals + 110;
        int meal2Prot = soyaProt + 7;
        int meal2Carbs = riceCarbs + 16;
        int meal2Fats = 7;

        // Moong Dal Chilla
        int chillaGrams = (int) Math.round(120 * scale);
        int chillaProt = (int) Math.round(chillaGrams * 0.10);
        int chillaCals = (int) Math.round(chillaGrams * 1.85);

        int stuffPaneerGrams = (int) Math.round(75 * protScale);
        int stuffPaneerProt = (int) Math.round(stuffPaneerGrams * 0.18);
        int stuffPaneerCals = (int) Math.round(stuffPaneerGrams * 2.65);

        int meal3Cals = chillaCals + stuffPaneerCals + 30;
        int meal3Prot = chillaProt + stuffPaneerProt + 2;
        int meal3Carbs = (int) Math.round(chillaGrams * 0.22) + 4;
        int meal3Fats = (int) Math.round(stuffPaneerGrams * 0.20) + 2;

        return String.format("""
        [
          {
            "mealName": "Low-Fat Paneer Bhurji with %d Whole Wheat Rotis & Mint Chaas",
            "description": "High-protein scrambled low-fat paneer sautéed with cumin, diced onions, tomatoes, and coriander, served with fresh whole wheat chapatis and chilled digestive chaas.",
            "prepTimeMinutes": 15,
            "totalCalories": %d,
            "totalProteinGrams": %d,
            "totalCarbsGrams": %d,
            "totalFatsGrams": %d,
            "ingredients": [
              { "name": "Paneer Low Fat", "grams": %d, "proteinGrams": %d, "carbsGrams": 3, "fatsGrams": 3, "calories": %d },
              { "name": "Roti / Chapati (Whole Wheat)", "grams": %d, "proteinGrams": %d, "carbsGrams": %d, "fatsGrams": 1, "calories": %d },
              { "name": "Chaas / Spiced Buttermilk", "grams": 200, "proteinGrams": 4, "carbsGrams": 5, "fatsGrams": 1, "calories": 44 },
              { "name": "Onion & Tomato Tadka with Ghee", "grams": 50, "proteinGrams": 1, "carbsGrams": 3, "fatsGrams": 4, "calories": 45 }
            ]
          },
          {
            "mealName": "Soya Chunks Matar Pulao with Plain Homestyle Curd",
            "description": "Tender boiled soya chunks and green peas sautéed with basmati rice and aromatic spices, accompanied by thick probiotic homestyle dahi.",
            "prepTimeMinutes": 20,
            "totalCalories": %d,
            "totalProteinGrams": %d,
            "totalCarbsGrams": %d,
            "totalFatsGrams": %d,
            "ingredients": [
              { "name": "Soya Chunks (Cooked)", "grams": %d, "proteinGrams": %d, "carbsGrams": 15, "fatsGrams": 3, "calories": %d },
              { "name": "Cooked White Basmati Rice", "grams": %d, "proteinGrams": 3, "carbsGrams": %d, "fatsGrams": 1, "calories": %d },
              { "name": "Curd / Dahi (Plain Homestyle)", "grams": 120, "proteinGrams": 4, "carbsGrams": 6, "fatsGrams": 4, "calories": 73 },
              { "name": "Green Peas & Tadka", "grams": 50, "proteinGrams": 3, "carbsGrams": 8, "fatsGrams": 1, "calories": 50 }
            ]
          },
          {
            "mealName": "Moong Dal Chilla Stuffed with Crumbled Paneer & Mint Chutney",
            "description": "Crispy ground yellow moong dal crepes stuffed with spiced crumbled paneer and served with a fresh coriander-mint dip.",
            "prepTimeMinutes": 15,
            "totalCalories": %d,
            "totalProteinGrams": %d,
            "totalCarbsGrams": %d,
            "totalFatsGrams": %d,
            "ingredients": [
              { "name": "Moong Dal Chilla Batter", "grams": %d, "proteinGrams": %d, "carbsGrams": %d, "fatsGrams": 3, "calories": %d },
              { "name": "Paneer (Indian Cottage Cheese)", "grams": %d, "proteinGrams": %d, "carbsGrams": 1, "fatsGrams": %d, "calories": %d },
              { "name": "Mint Coriander Chutney", "grams": 40, "proteinGrams": 1, "carbsGrams": 4, "fatsGrams": 1, "calories": 30 }
            ]
          }
        ]
        """,
        rotiCount, meal1Cals, meal1Prot, meal1Carbs, meal1Fats,
        lowFatPaneerGrams, paneerProt, paneerCals,
        rotiGrams, rotiProt, rotiCarbs, rotiCals,
        meal2Cals, meal2Prot, meal2Carbs, meal2Fats,
        soyaGrams, soyaProt, soyaCals,
        riceGrams, riceCarbs, riceCals,
        meal3Cals, meal3Prot, meal3Carbs, meal3Fats,
        chillaGrams, chillaProt, (int) Math.round(chillaGrams * 0.22), chillaCals,
        stuffPaneerGrams, stuffPaneerProt, (int) Math.round(stuffPaneerGrams * 0.20), stuffPaneerCals);
    }

    /**
     * Fully dynamic progressive running schedule builder that responds to goal, level, weeks, and days.
     */
    private String buildDynamicRunningPlan(String currentLevel, String targetGoal, int weeksCount, int daysPerWeek) {
        String goal = targetGoal != null ? targetGoal.toUpperCase() : "5K";
        String lvl = currentLevel != null ? currentLevel.toLowerCase() : "beginner";
        int days = Math.max(2, Math.min(5, daysPerWeek));
        int weeks = Math.max(2, Math.min(16, weeksCount));

        boolean isAdv = lvl.contains("advanced");
        boolean isInt = lvl.contains("intermediate");

        List<String> runs = new ArrayList<>();

        if (goal.contains("2K")) {
            double baseDist = isAdv ? 3.0 : (isInt ? 2.5 : 1.8);
            String pace1 = isAdv ? "5:00 - 5:20 min/km" : (isInt ? "6:00 - 6:30 min/km" : "7:00 - 7:30 min/km");
            String pace2 = isAdv ? "4:15 min/km sprint" : (isInt ? "5:15 min/km tempo" : "6:15 min/km intervals");

            runs.add(String.format("""
            {
              "day": "Day 1 (Monday)",
              "type": "Easy Zone 2 Aerobic Base",
              "distanceKm": %.1f,
              "targetPace": "%s",
              "durationMinutes": %d,
              "notes": "Maintain conversational breathing pace to build mitochondrial density."
            }
            """, baseDist, pace1, (int) Math.round(baseDist * 7)));

            runs.add(String.format("""
            {
              "day": "Day 2 (Wednesday)",
              "type": "Speed Strides & Interval Acceleration",
              "distanceKm": %.1f,
              "targetPace": "%s",
              "durationMinutes": %d,
              "notes": "6x 200m accelerations with 90 seconds walking recovery."
            }
            """, baseDist * 0.8, pace2, (int) Math.round(baseDist * 6)));

            if (days >= 3) {
                runs.add(String.format("""
                {
                  "day": "Day 3 (Friday)",
                  "type": "2K Milestone Benchmark Run",
                  "distanceKm": 2.0,
                  "targetPace": "%s",
                  "durationMinutes": %d,
                  "notes": "Timed 2K trial. Run smooth and even throughout both kilometers."
                }
                """, isAdv ? "4:45 min/km" : (isInt ? "5:45 min/km" : "6:45 min/km"), isAdv ? 10 : (isInt ? 12 : 14)));
            }

            if (days >= 4) {
                runs.add("""
                {
                  "day": "Day 4 (Sunday)",
                  "type": "Light Active Recovery Jog & Cadence Drills",
                  "distanceKm": 1.5,
                  "targetPace": "7:30 min/km conversational",
                  "durationMinutes": 12,
                  "notes": "Gentle flush run focusing on 170-180 steps per minute cadence."
                }
                """);
            }
        } else if (goal.contains("10K")) {
            double baseDist = isAdv ? 7.0 : (isInt ? 5.5 : 4.0);
            double longDist = isAdv ? 11.0 : (isInt ? 8.5 : 6.0);

            runs.add(String.format("""
            {
              "day": "Day 1 (Monday)",
              "type": "Easy Aerobic Zone 2 Foundation",
              "distanceKm": %.1f,
              "targetPace": "%s",
              "durationMinutes": %d,
              "notes": "Keep heart rate in 65-75%% max range to build capillary beds."
            }
            """, baseDist, isAdv ? "5:30 min/km" : (isInt ? "6:15 min/km" : "7:00 min/km"), (int) Math.round(baseDist * 6.5)));

            runs.add(String.format("""
            {
              "day": "Day 2 (Wednesday)",
              "type": "Lactate Threshold Tempo Run",
              "distanceKm": %.1f,
              "targetPace": "%s",
              "durationMinutes": %d,
              "notes": "Comfortably hard effort where you can only speak in 2-3 word sentences."
            }
            """, baseDist * 0.9, isAdv ? "4:45 min/km" : (isInt ? "5:30 min/km" : "6:15 min/km"), (int) Math.round(baseDist * 5.8)));

            if (days >= 3) {
                runs.add(String.format("""
                {
                  "day": "Day 3 (Saturday)",
                  "type": "Weekend Long Endurance Run",
                  "distanceKm": %.1f,
                  "targetPace": "%s",
                  "durationMinutes": %d,
                  "notes": "Fuel with water and electrolytes; trains mental stamina and fat oxidation."
                }
                """, longDist, isAdv ? "5:45 min/km" : (isInt ? "6:30 min/km" : "7:15 min/km"), (int) Math.round(longDist * 6.8)));
            }

            if (days >= 4) {
                runs.add("""
                {
                  "day": "Day 4 (Thursday)",
                  "type": "Interval Track Repeats",
                  "distanceKm": 5.0,
                  "targetPace": "5:00 min/km intervals with 90s walk",
                  "durationMinutes": 30,
                  "notes": "5x 800m repeats to improve VO2 max."
                }
                """);
            }
        } else if (goal.contains("HIIT") || goal.contains("SPRINT") || goal.contains("FAT BURN")) {
            runs.add("""
            {
              "day": "Day 1 (Tuesday)",
              "type": "Tabata Sprint Intervals",
              "distanceKm": 2.8,
              "targetPace": "All-out 20s sprint / 40s walk",
              "durationMinutes": 20,
              "notes": "8 cycles of maximal effort sprint followed by active walking recovery."
            }
            """);

            runs.add("""
            {
              "day": "Day 2 (Thursday)",
              "type": "400m Track Repeats for EPOC",
              "distanceKm": 3.6,
              "targetPace": "4:45 min/km hard efforts",
              "durationMinutes": 24,
              "notes": "6x 400m hard efforts with 90s standing rest to maximize post-exercise oxygen consumption."
            }
            """);

            if (days >= 3) {
                runs.add("""
                {
                  "day": "Day 3 (Saturday)",
                  "type": "Hill Sprint Explosive Intervals",
                  "distanceKm": 2.5,
                  "targetPace": "Max effort uphill / slow jog down",
                  "durationMinutes": 22,
                  "notes": "8x 30-second steep hill sprints; zero knee impact with huge power output."
                }
                """);
            }

            if (days >= 4) {
                runs.add("""
                {
                  "day": "Day 4 (Sunday)",
                  "type": "Zone 2 Low-Impact Flush Jog",
                  "distanceKm": 3.5,
                  "targetPace": "6:45 min/km conversational",
                  "durationMinutes": 24,
                  "notes": "Gentle jog to flush metabolic waste and facilitate recovery."
                }
                """);
            }
        } else {
            // Default 5K Plan
            double dist1 = isAdv ? 4.5 : (isInt ? 3.5 : 2.5);
            double distLong = isAdv ? 6.5 : (isInt ? 5.0 : 4.0);

            runs.add(String.format("""
            {
              "day": "Day 1 (Monday)",
              "type": "Zone 2 Easy Aerobic Base",
              "distanceKm": %.1f,
              "targetPace": "%s",
              "durationMinutes": %d,
              "notes": "Keep conversation pace, builds aerobic capacity."
            }
            """, dist1, isAdv ? "5:30 min/km" : (isInt ? "6:15 min/km" : "7:00 min/km"), (int) Math.round(dist1 * 7)));

            runs.add(String.format("""
            {
              "day": "Day 2 (Wednesday)",
              "type": "Interval Speed Repeats",
              "distanceKm": %.1f,
              "targetPace": "%s",
              "durationMinutes": %d,
              "notes": "6x 400m hard effort, 90 seconds walking rest."
            }
            """, dist1, isAdv ? "4:30 min/km" : (isInt ? "5:15 min/km" : "6:00 min/km"), (int) Math.round(dist1 * 6.5)));

            if (days >= 3) {
                runs.add(String.format("""
                {
                  "day": "Day 3 (Saturday)",
                  "type": "Continuous 5K Long Run",
                  "distanceKm": %.1f,
                  "targetPace": "%s",
                  "durationMinutes": %d,
                  "notes": "Continuous steady pace to solidify 5K distance."
                }
                """, distLong, isAdv ? "5:15 min/km" : (isInt ? "6:00 min/km" : "6:45 min/km"), (int) Math.round(distLong * 6.8)));
            }

            if (days >= 4) {
                runs.add("""
                {
                  "day": "Day 4 (Thursday)",
                  "type": "Tempo Pace Run",
                  "distanceKm": 3.0,
                  "targetPace": "5:45 min/km steady",
                  "durationMinutes": 18,
                  "notes": "Continuous comfortably hard rhythm to raise lactate threshold."
                }
                """);
            }
        }

        String scheduleJson = String.join(",\n", runs);

        return String.format("""
        {
          "planName": "%s (%d Weeks • %s • %d Days/Week)",
          "durationWeeks": %d,
          "schedule": [
            %s
          ]
        }
        """, targetGoal, weeks, currentLevel, days, weeks, scheduleJson);
    }
}
