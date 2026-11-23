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
     * Comprehensive Sports Science Conversational Intelligence Engine (28+ distinct intents, zero repetitive fallbacks).
     */
    private String buildDynamicChatResponse(String message, String userContext) {
        String q = message != null ? message.toLowerCase().trim() : "";

        // 1. Greetings & Bot Introduction
        if (q.matches(".*\\b(hi|hello|hey|namaste|greetings|good morning|good evening|good afternoon|sup|yo)\\b.*")
                || q.equals("hi") || q.equals("hello") || q.equals("hey") || q.contains("who are you") || q.contains("what can you do")) {
            return "Hey there! I am your FitTrack AI Coach. I'm here to help you dial in your training, nutrition, and daily recovery.\n\n" +
                    (userContext != null && !userContext.isEmpty() ? "**Your Active Stats**: " + userContext + "\n\n" : "") +
                    "Here are things you can ask me:\n" +
                    "• **Muscle Workouts**: Chest, Back, Arms, Delts, Legs, or Core routines & form cues\n" +
                    "• **Nutrition & Indian Foods**: High-protein vegetarian & non-vegetarian meal plans\n" +
                    "• **Fat Loss & Bulking**: Calorie deficit calculations, TDEE, macros, and breaking plateaus\n" +
                    "• **Pain Substitutions**: Alternatives for shoulder, knee, lower back, or elbow discomfort\n" +
                    "• **Supplements & Hydration**: Creatine timing, whey protein, water intake, and rest days\n\n" +
                    "What would you like to work on right now?";
        }

        // 2. Personal Profile / Stats / Calorie Targets
        if ((q.contains("my") && (q.contains("weight") || q.contains("calorie") || q.contains("target") || q.contains("profile") || q.contains("goal") || q.contains("stat")))
                || q.contains("how many calories should i") || q.contains("how much should i eat")) {
            return "Here is your profile breakdown based on your FitTrack setup:\n\n" +
                    (userContext != null && !userContext.isEmpty() ? "• **Active Stats**: " + userContext + "\n\n" : "") +
                    "1. **Calorie Deficit / Surplus**: Follow your target daily calories precisely. Weigh foods raw with a kitchen scale to eliminate tracking errors.\n" +
                    "2. **Protein Target**: Hit ~1.8g to 2.2g of protein per kg of bodyweight to maintain lean mass while stripping body fat.\n" +
                    "3. **Weekly Progress Metric**: Weigh yourself every morning under identical conditions (fasted, post-restroom). Compare 7-day rolling averages instead of day-to-day fluctuations.\n" +
                    "4. **Adjustments**: If your weight stalls for more than 14 consecutive days, trim 100-150 kcal or increase your daily step count by 2,000 steps.";
        }

        // 3. Shoulder Pain / Rotator Cuff / Impingement
        if (q.contains("shoulder") && (q.contains("pain") || q.contains("hurt") || q.contains("click") || q.contains("pinch") || q.contains("ache") || q.contains("injury"))) {
            return "Managing shoulder discomfort during pressing sessions:\n\n" +
                    "1. **Stop Pain Triggers**: Pause flat barbell bench press and behind-the-neck presses immediately. Straight 90° abduction pinches the supraspinatus tendon against the acromion (subacromial impingement).\n" +
                    "2. **Pain-Free Substitutions**:\n" +
                    "   • **Neutral-Grip Dumbbell Floor Press**: Palms face each other with elbows at 45°. The floor stops excessive humeral extension past your torso, removing anterior capsule strain while heavily loading the pecs.\n" +
                    "   • **Low-Incline (30°) Dumbbell Press**: Easier on the glenohumeral joint than flat pressing.\n" +
                    "   • **Landmine Chest Press**: The upward diagonal arc allows natural scapular upward rotation.\n" +
                    "   • **Scapular Plane Lateral Raises ('Scaption')**: Raise dumbbells 30° forward of the coronal plane.\n" +
                    "3. **Pre-Hab Warmup**: 2 sets of 15 Cable Face Pulls (with external rotation) + Band Pull-Aparts before lifting.";
        }

        // 4. Knee Pain / Squat Substitutions / Patellofemoral Discomfort
        if (q.contains("knee") || (q.contains("squat") && (q.contains("pain") || q.contains("hurt") || q.contains("injury")))) {
            return "Resolving knee or patellar discomfort during leg day:\n\n" +
                    "1. **Primary Cause**: Forward knee translation combined with tight ankles creates high shear force at the patellar tendon.\n" +
                    "2. **Safe, Highly Effective Substitutions**:\n" +
                    "   • **Box Squats**: Sitting back onto a box forces vertical shins, immediately shifting load from knees to glutes and hamstrings.\n" +
                    "   • **Romanian Deadlifts (RDLs)**: Near-zero knee flexion; massive posterior chain stimulus.\n" +
                    "   • **Reverse Lunges**: Stepping backward produces ~40% less patellofemoral shear than forward lunges.\n" +
                    "   • **Backward Sled Drags**: Walking backward under light load pumps synovial fluid into the knees and strengthens the vastus medialis (VMO).\n" +
                    "3. **Check Ankle Mobility**: Foam roll your calves and stretch the soleus to prevent knees caving inward (valgus).";
        }

        // 5. Lower Back Pain / Lumbar Strain / Deadlift Discomfort
        if (q.contains("back") && (q.contains("pain") || q.contains("hurt") || q.contains("strain") || q.contains("sore") || q.contains("spasm") || q.contains("injury"))) {
            return "Lower back fatigue and lumbar strain management:\n\n" +
                    "1. **De-load Spinal Compression**: Pause heavy conventional deadlifts and back squats until acute discomfort clears.\n" +
                    "2. **Spine-Spared Muscle Builders**:\n" +
                    "   • **Chest-Supported Dumbbell Rows**: Maximum lat, rhomboid, and rear delt engagement with zero shear force on the lumbar spine.\n" +
                    "   • **Bulgarian Split Squats**: Massive quad and glute stimulus with half the total spinal load.\n" +
                    "   • **Leg Press (Controlled)**: Keep your lower back firmly pressed into the pad; never let your pelvis curl up (butt wink) at the bottom.\n" +
                    "3. **The McGill Big 3 for Core Stiffness**: Perform Bird-Dogs, Side Planks, and Modified Curl-ups daily to build 360° spinal stability.";
        }

        // 6. Elbow & Wrist Pain / Tendonitis
        if ((q.contains("elbow") || q.contains("wrist")) && (q.contains("pain") || q.contains("hurt") || q.contains("tendon") || q.contains("ache"))) {
            return "Addressing elbow and wrist tendon discomfort:\n\n" +
                    "1. **Avoid Rigid Barbells**: Straight-bar bicep curls and straight skullcrushers force wrists and forearms into unnatural extreme angles.\n" +
                    "2. **Substitutions**:\n" +
                    "   • **Neutral-Grip Dumbbell Hammer Curls**: Transfers load to the brachialis and radialis with zero wrist strain.\n" +
                    "   • **Cross-Body Cable Tricep Extensions**: Far superior to skullcrushers for elbow comfort.\n" +
                    "   • **EZ-Curl Bar**: The angled grips eliminate torque on the medial epicondyle (Golfer's/Tennis elbow).\n" +
                    "3. **Wrist Curls & Extensors**: Do light 2-3kg dumbbell wrist curls to strengthen the forearm flexor tendons.";
        }

        // 7. Chest / Pectorals / Bench Press / Push Day
        if (q.contains("chest") || q.contains("pecs") || q.contains("pectoral") || q.contains("push day")
                || (q.contains("bench") && !q.contains("pain")) || q.contains("pushup") || q.contains("push up") || q.contains("dips")) {
            return "Complete Science-Based Chest Workout Guide:\n\n" +
                    "1. **Upper Clavicular Head (Creates the 'Full' Look)**:\n" +
                    "   • **30° Incline Dumbbell Press**: 3-4 sets x 6-10 reps (RPE 8). Keep incline modest (30°); steep angles shift work to front delts.\n\n" +
                    "2. **Mid Sternal Head (Raw Thickness & Power)**:\n" +
                    "   • **Flat Barbell or Dumbbell Bench Press**: 3-4 sets x 6-8 reps. Arch slightly, retract scapulae, and tuck elbows to 45°.\n\n" +
                    "3. **Lower Pectorals & Stretch**:\n" +
                    "   • **Weighted or Bodyweight Dips**: Lean forward at 30° to target pecs rather than triceps.\n" +
                    "   • **Cable Crossovers (High-to-Low)**: 3 sets x 12-15 reps. Squeeze hard at the peak contraction for maximum metabolic burn.\n\n" +
                    "4. **Form Tip**: Lower the weight over 2-3 seconds for maximum mechanical tension and stretch under load.";
        }

        // 8. Back / Lats / Pull Day / Rows / V-Taper
        if ((q.contains("back") && !q.contains("pain")) || q.contains("lat") || q.contains("pullup") || q.contains("pull up")
                || q.contains("chinup") || q.contains("chin up") || q.contains("pulldown") || q.contains("rowing")
                || q.contains("barbell row") || q.contains("pull day") || q.contains("v-taper") || q.contains("traps")) {
            return "Science-Based Back & Lat Hypertrophy Guide:\n\n" +
                    "1. **Lat Width (The V-Taper)**:\n" +
                    "   • **Neutral-Grip Lat Pulldown or Weighted Pull-ups**: 3-4 sets x 6-10 reps. Drive elbows down into your pockets; don't pull with forearms.\n" +
                    "   • **Single-Arm Dumbbell Row or Ilio-Lumbar Cable Row**: Keeps tension purely on the lower lat fibers.\n\n" +
                    "2. **Mid-Back & Upper Back Thickness (Rhomboids & Mid Traps)**:\n" +
                    "   • **Chest-Supported T-Bar / DB Row**: 3 sets x 8-12 reps with flared elbows (60°) to heavily load upper back without spinal shear.\n" +
                    "   • **Face Pulls**: 3 sets x 15 reps for rear delts and lower traps.\n\n" +
                    "3. **Lower Back & Posterior Chain**:\n" +
                    "   • **Romanian Deadlifts (RDLs)**: 3 sets x 8 reps for erector spinae, glutes, and hamstrings.\n\n" +
                    "4. **Execution Cue**: Think of your hands purely as hooks—initiate every pull by retracting your shoulder blades and driving your elbows.";
        }

        // 9. Arms: Biceps & Triceps Hypertrophy
        if (q.contains("bicep") || q.contains("tricep") || q.contains("arm") || q.contains("arms")
                || q.contains("curl") || q.contains("skullcrusher") || q.contains("pushdown") || q.contains("hammer curl")) {
            return "Complete Arm Hypertrophy Protocol (Biceps & Triceps):\n\n" +
                    "1. **Triceps (65% of Total Arm Mass)**:\n" +
                    "   • **Long Head (Stretch Position)**: **Overhead Cable Rope Extension** (3 sets x 10-12 reps). The long head only fully stretches when arms are overhead!\n" +
                    "   • **Lateral & Medial Head**: **Cable Straight-Bar Pushdowns** (3 sets x 10-12 reps). Lock elbows at your ribs and flare slightly at the bottom.\n\n" +
                    "2. **Biceps (Full Peak & Width)**:\n" +
                    "   • **Long Head**: **Incline Dumbbell Curls** (3 sets x 8-10 reps). Incline bench places biceps in a deep stretch behind the body.\n" +
                    "   • **Short Head & Peak**: **Preacher Curls or Cable Curls** (3 sets x 10-12 reps).\n" +
                    "   • **Brachialis & Forearm Thickness**: **Cross-Body Hammer Curls** (3 sets x 10-12 reps). Pushes the biceps upward for greater overall arm circumference.\n\n" +
                    "3. **Frequency**: Train arms 2x weekly with 10-14 total sets per week for optimal growth.";
        }

        // 10. Shoulders & Delts (Development & Routine)
        if (q.contains("shoulder") || q.contains("delt") || q.contains("overhead press")
                || q.contains("lateral raise") || q.contains("military press") || q.contains("face pull")) {
            return "3D Deltoid Growth Strategy (Anterior, Lateral & Rear Heads):\n\n" +
                    "1. **Lateral Delts (Creates Shoulder Width)**:\n" +
                    "   • **Cable Lateral Raises**: 4 sets x 12-15 reps. Cables maintain uniform tension at the bottom where dumbbells provide zero resistance.\n" +
                    "   • **Execution Cue**: Raise dumbbells in the scapular plane (30° forward of your sides) with hands angled slightly downward (like pouring a jug).\n\n" +
                    "2. **Rear Delts (Posture & 3D Roundness)**:\n" +
                    "   • **Reverse Pec-Deck Flyes**: 3 sets x 15 reps. Keep arms straight and lead with elbows.\n" +
                    "   • **Cable Face Pulls with External Rotation**: 3 sets x 15 reps.\n\n" +
                    "3. **Front Delts (Overhead Strength)**:\n" +
                    "   • **Seated Dumbbell Overhead Press**: 3 sets x 6-8 reps. Heavy incline and flat benching already heavily stimulate front delts, so 3-4 sets of direct pressing per week is plenty.";
        }

        // 11. Legs: Quads, Hamstrings, Glutes & Calves
        if (q.contains("leg") || q.contains("quad") || q.contains("hamstring") || q.contains("glute")
                || q.contains("calf") || q.contains("calves") || q.contains("leg press") || q.contains("lunge")
                || q.contains("squat") || q.contains("leg day")) {
            return "Complete Lower Body Architecture (Leg Day):\n\n" +
                    "1. **Quads (Knee Flexion Hypertrophy)**:\n" +
                    "   • **Barbell Back Squat or Hack Squat**: 3-4 sets x 6-8 reps. Focus on deep knee flexion and controlled descent.\n" +
                    "   • **Leg Extensions**: 3 sets x 12-15 reps with a 1-second pause at peak extension.\n\n" +
                    "2. **Hamstrings (Hip Extension & Knee Flexion)**:\n" +
                    "   • **Romanian Deadlifts (RDLs)**: 3-4 sets x 8 reps. Push hips back as far as possible until you feel a deep hamstring stretch.\n" +
                    "   • **Seated Leg Curls**: 3 sets x 10-12 reps (research shows seated curls produce superior hypertrophy over lying curls).\n\n" +
                    "3. **Glutes & Single-Leg Stability**:\n" +
                    "   • **Bulgarian Split Squats**: 3 sets x 8-10 reps per leg.\n\n" +
                    "4. **Calves**:\n" +
                    "   • **Standing Calf Raises**: 3 sets x 12-15 reps with a 2-second stretch at the bottom to target the gastrocnemius.";
        }

        // 12. Abs, Core, Belly Fat & Spot Reduction
        if (q.contains("abs") || q.contains("core") || q.contains("six pack") || q.contains("belly")
                || q.contains("tummy") || q.contains("love handles") || q.contains("plank") || q.contains("crunch") || q.contains("spot reduce")) {
            return "The Science of Visible Abs & Core Development:\n\n" +
                    "1. **The Spot Reduction Myth**: You cannot burn fat exclusively from your belly by doing crunches. Fat loss is systemic and controlled entirely by an overall calorie deficit.\n" +
                    "2. **Body Fat Percentages for Visibility**:\n" +
                    "   • Men: Abs become visibly defined between **10% to 12% body fat**.\n" +
                    "   • Women: Abs appear between **18% to 20% body fat**.\n" +
                    "3. **Direct Hypertrophy for Thicker Abs**:\n" +
                    "   • **Hanging Leg / Knee Raises**: 3 sets x 12-15 reps. Focus on posterior pelvic tilt (curling your pelvis up toward your chest, not just swinging legs).\n" +
                    "   • **Cable Kneeling Crunches**: 3 sets x 12-15 reps with progressive overload.\n" +
                    "   • **Planks / Ab-Wheel Rollouts**: 3 sets for deep transverse abdominis bracing.";
        }

        // 13. Indian Vegetarian Diet & High Protein
        if (q.contains("veg") || q.contains("paneer") || q.contains("soya") || q.contains("soy")
                || q.contains("dal") || q.contains("chana") || q.contains("sattu") || q.contains("tofu")
                || q.contains("besan") || q.contains("makhana") || q.contains("sprouts")) {
            return "Hitting 100g to 130g Protein on an Indian Vegetarian Diet (in a Calorie Deficit):\n\n" +
                    "1. **Soya Chunks (Nutrela)**: 52g protein per 100g dry. A 50g dry portion gives **26g protein for only 172 kcal**—the best protein-to-calorie ratio in Indian food!\n" +
                    "2. **Low-Fat Paneer**: 25g protein per 100g (~160 kcal). Swapping regular full-fat paneer (265 kcal) saves over 100 calories per 100g.\n" +
                    "3. **Moong Dal & Besan Chillas**: Blend sprouted moong dal with green chilies, spread on tawa, and stuff with 50g grated paneer for an easy 20g+ protein breakfast.\n" +
                    "4. **Kala Chana (Boiled / Chaat)**: 9g protein + 8g gut-friendly dietary fiber per 100g boiled. Keeps you satiated for hours.\n" +
                    "5. **Sattu (Roasted Chana Flour)**: 23g protein per 100g. Mix 40g sattu in cold water with roasted jeera and lemon for a cooling 9g protein drink.\n" +
                    "6. **Pro Tip**: Complement lentils with bajra, jowar, or ragi rotis for complete amino acid profiles, and take 1 scoop of Whey Protein (~25g protein, 120 kcal) to effortlessly reach 120g daily.";
        }

        // 14. Indian Non-Vegetarian High Protein
        if (q.contains("non-veg") || q.contains("non veg") || q.contains("chicken") || q.contains("egg")
                || q.contains("fish") || q.contains("mutton") || q.contains("meat") || q.contains("keema") || q.contains("tandoori")) {
            return "Top Indian Non-Vegetarian Staples for Lean Muscle & Fat Loss:\n\n" +
                    "1. **Chicken Breast (Skinless Boneless)**: 31g protein per 100g for only 165 kcal. Marinate in curd, ginger-garlic paste, kasuri methi, and tandoori spices; cook in an air-fryer, pan, or oven.\n" +
                    "2. **The Whole Egg + Egg Whites Strategy**: 1 whole egg (6g protein, 70 kcal) paired with 4 liquid egg whites (14g protein, 68 kcal) yields a massive 20g protein scramble for under 140 calories!\n" +
                    "3. **Fish (Rohu, Katla, Surmai, Pomfret)**: 18-22g protein per 100g, packed with natural Omega-3 fatty acids for joint and heart health.\n" +
                    "4. **Chicken Keema (Minced Breast)**: Cook with onions, tomatoes, and peas for an easy meal prep bowl you can eat with rotis or rice.";
        }

        // 15. Diet Plan / What to Eat / Meal Plan / Breakfast, Lunch, Dinner
        if (q.contains("what to eat") || q.contains("what should i eat") || q.contains("diet plan")
                || q.contains("meal plan") || q.contains("breakfast") || q.contains("lunch") || q.contains("dinner")
                || q.contains("snack") || q.contains("eating") || q.contains("food plan") || q.contains("nutrition")) {
            return "Sample 1-Day Balanced Meal Plan (1600 - 1800 kcal Deficit Plan):\n\n" +
                    "• **Breakfast (8:30 AM)**: 2 Moong dal chillas stuffed with 40g grated paneer (or 1 whole egg + 3 egg whites scramble) + 1 cup green tea / black coffee (~320 kcal, 22g protein).\n\n" +
                    "• **Lunch (1:30 PM)**: 1 bowl dal + 100g cooked soya chunks curry or 150g grilled chicken breast + 1 whole wheat/bajra roti + 1 large bowl cucumber & tomato salad (~480 kcal, 38g protein).\n\n" +
                    "• **Evening Snack (5:00 PM)**: 30g roasted makhana or 35g bhuna chana + 1 glass chaas (buttermilk) with roasted jeera (~160 kcal, 9g protein).\n\n" +
                    "• **Dinner (8:00 PM)**: 1 bowl light vegetable dalia or brown rice with 100g paneer tikka or grilled fish fillet (~420 kcal, 28g protein).\n\n" +
                    "• **Post-Workout / Before Bed**: 1 scoop Whey Protein in water or 200ml warm turmeric milk (~130 kcal, 25g protein).\n" +
                    "**Total**: ~1,510 kcal | ~122g Protein | High fiber & satiety.";
        }

        // 16. Weight Loss / Fat Loss / Calorie Deficit / Cutting
        if (q.contains("lose weight") || q.contains("weight loss") || q.contains("fat loss")
                || q.contains("cut") || q.contains("cutting") || q.contains("deficit") || q.contains("calorie deficit")
                || q.contains("burn fat") || q.contains("tdee") || q.contains("bmr")) {
            return "The Exact Math Behind Sustainable Fat Loss:\n\n" +
                    "1. **The Energy Equation**: 1 kg of stored human fat represents approximately 7,700 kcal. Creating a daily deficit of **400 to 500 kcal** below your TDEE produces a safe, reliable loss of **0.4 to 0.5 kg per week** without muscle breakdown.\n" +
                    "2. **Preserve Your Muscle**: Eat 1.8g to 2.2g protein per kg of bodyweight and continue lifting heavy. If you drop protein or stop lifting, up to 40% of weight lost will be valuable muscle tissue instead of fat!\n" +
                    "3. **Harness NEAT**: Walking 8,000 to 10,000 steps daily burns 300+ extra calories without triggering the ravenous hunger caused by grueling cardio.\n" +
                    "4. **Audit Liquid & Cooking Calories**: Measure cooking oil and ghee with a teaspoon (1 tsp = ~45 kcal; free pouring easily adds 200 uncounted calories).";
        }

        // 17. Weight Gain / Muscle Building / Bulking / Skinny
        if (q.contains("gain weight") || q.contains("weight gain") || q.contains("gain muscle")
                || q.contains("bulk") || q.contains("bulking") || q.contains("skinny") || q.contains("hardgainer") || q.contains("mass")) {
            return "Clean Bulking Protocol for Hardgainers (Gain Muscle, Not Fat):\n\n" +
                    "1. **Controlled Caloric Surplus**: Eat **250 to 350 kcal above your TDEE**. A massive 1,000 kcal dirty bulk simply stores fat because muscle synthesis has a biological speed limit (~1 to 1.5 kg muscle/month for beginners).\n" +
                    "2. **Calorie-Dense Healthy Staples**:\n" +
                    "   • Add 2 tablespoons of peanut butter (190 kcal) to oatmeal or rotis.\n" +
                    "   • Blend an easy high-calorie shake: 300ml milk, 1 banana, 40g oats, 1 scoop whey, 1 tbsp peanut butter (~550 kcal, 40g protein).\n" +
                    "   • Snack on almonds, walnuts, and dates throughout the day.\n" +
                    "3. **Progressive Overload**: Strive to add 1 rep or 1.25kg to your compound lifts every week. Extra calories only turn into muscle if there is an adaptive mechanical stimulus!";
        }

        // 18. Creatine Monohydrate Facts & Dosing
        if (q.contains("creatine")) {
            return "Creatine Monohydrate: The Gold Standard Supplement:\n\n" +
                    "1. **Mechanism**: Saturates intramuscular phosphocreatine stores, accelerating ATP resynthesis during 1-5 rep heavy bursts. Increases strength, power output, and intracellular muscle hydration.\n" +
                    "2. **Dosing**: Take **3 to 5 grams daily** consistently at any convenient time. A 20g loading phase is optional; 3-5g daily fully saturates muscle stores in 3-4 weeks.\n" +
                    "3. **Safety**: Over 600 peer-reviewed scientific studies confirm its safety for healthy kidneys. Drink an extra 500ml of water daily.\n" +
                    "4. **Weight Scale Note**: You may gain 1-2 kg of scale weight in week 1. This is intracellular water inside the muscle fibers, NOT body fat!";
        }

        // 19. Whey Protein & Supplements
        if (q.contains("whey") || q.contains("protein powder") || q.contains("supplement")
                || q.contains("bcaa") || q.contains("pre workout") || q.contains("preworkout") || q.contains("fish oil") || q.contains("multivitamin")) {
            return "Guide to Gym Supplements (What Works vs What to Skip):\n\n" +
                    "1. **Whey Protein (Essential Tool)**: Naturally extracted from milk during cheese making. Concentrate is ~80% protein; Isolate is 90%+ and virtually lactose-free. It is 100% real food, not steroids.\n" +
                    "2. **Creatine Monohydrate (Highly Recommended)**: The most proven strength and power enhancer in sports science.\n" +
                    "3. **Caffeine / Pre-Workout (Optional)**: 150-200mg caffeine 30 mins before lifting increases focus and reduces perceived exertion.\n" +
                    "4. **Skip BCAAs**: If you hit your daily protein goal, BCAAs are completely redundant because whole protein already contains all 9 essential amino acids.\n" +
                    "5. **Omega-3 Fish Oil & Vitamin D3**: Essential for joint recovery, hormone production, and immunity.";
        }

        // 20. Water & Daily Hydration
        if (q.contains("water") || q.contains("hydration") || q.contains("drink") || q.contains("liters") || q.contains("litres")) {
            return "Hydration Guidelines for Lifting & Fat Loss:\n\n" +
                    "1. **Baseline Target**: Drink **35 to 40 ml per kg of bodyweight** (typically 3.0 to 4.0 liters per day for active lifters).\n" +
                    "2. **During Workout**: Drink 500 to 750 ml of water with a pinch of pink Himalayan salt (sodium electrolyte) to prevent muscle cramping and maintain intra-cellular pump.\n" +
                    "3. **Fat Loss Benefit**: Dehydration is frequently misdiagnosed by the brain as sugar cravings. Drinking a large glass of water 15 minutes before meals decreases spontaneous overeating.\n" +
                    "4. **Color Check**: Your urine should be pale straw yellow. Dark yellow indicates dehydration; completely clear means you may be flushing electrolytes.";
        }

        // 21. Sleep, Rest & Soreness (DOMS) vs Injury
        if (q.contains("sleep") || q.contains("rest") || q.contains("recovery") || q.contains("doms")
                || q.contains("sore") || q.contains("soreness") || q.contains("stiff") || q.contains("rest day")) {
            return "Recovery, Sleep & Muscle Soreness (DOMS):\n\n" +
                    "1. **DOMS vs Real Injury**:\n" +
                    "   • **DOMS**: A dull, diffuse ache across the muscle belly peaking 24 to 48 hours post-workout from microscopic micro-tears. It gets better with a light warm-up.\n" +
                    "   • **Injury**: Sharp, localized, stabbing pain in a joint or tendon that spikes under load.\n" +
                    "2. **Sleep is Non-Negotiable**: Human Growth Hormone (HGH) and protein synthesis peak during deep slow-wave sleep. Aim for **7.5 to 9 hours nightly**.\n" +
                    "3. **Active Recovery**: On rest days, do a 30-minute easy outdoor walk or light mobility work. Light movement promotes capillary blood flow and flushes metabolic waste far faster than lying stationary in bed.";
        }

        // 22. Running, Cardio & Stamina
        if (q.contains("run") || q.contains("running") || q.contains("cardio") || q.contains("stamina")
                || q.contains("endurance") || q.contains("5k") || q.contains("10k") || q.contains("treadmill") || q.contains("hiit") || q.contains("shin")) {
            return "Cardio, Running & Stamina Optimization:\n\n" +
                    "1. **The 80/20 Aerobic Rule**:\n" +
                    "   • **80% Zone 2 Cardio**: Run or power-walk at a conversational pace (heart rate ~60-70% max). This builds mitochondrial density and burns fat without fatiguing your nervous system.\n" +
                    "   • **20% High Intensity / Speed**: 1 weekly session of intervals or hill sprints.\n" +
                    "2. **Preventing Shin Splints**: Increase your cadence to 165-175 steps per minute. Shorter strides prevent heel-striking far ahead of your hips, eliminating tibial impact.\n" +
                    "3. **Cardio vs Weights**: Always perform resistance training FIRST, followed by cardio, so you do not deplete glycogen needed for heavy lifting.";
        }

        // 23. Workout Splits & Routine Scheduling
        if (q.contains("split") || q.contains("routine") || q.contains("workout plan") || q.contains("how many days")
                || q.contains("ppl") || q.contains("push pull legs") || q.contains("upper lower") || q.contains("bro split") || q.contains("schedule")) {
            return "Selecting the Optimal Workout Split for Your Schedule:\n\n" +
                    "1. **3 Days per Week**: **Full Body (Mon / Wed / Fri)**. Every muscle is hit 3x weekly with high frequency and ample recovery.\n" +
                    "2. **4 Days per Week**: **Upper / Lower Split (Mon, Tue, Thu, Fri)**. The most balanced split for busy lifters. Hits every muscle group 2x weekly.\n" +
                    "3. **5 to 6 Days per Week**: **Push / Pull / Legs (PPL)**. Allows high volume per muscle group with dedicated arm and shoulder specialization.\n" +
                    "4. **The Science Rule**: Training a muscle group **2 times per week** produces ~30% greater hypertrophy over time than hitting it only once in a typical 'Bro Split'.";
        }

        // 24. Breaking Plateaus & Progressive Overload
        if (q.contains("plateau") || q.contains("stuck") || q.contains("progressive overload")
                || q.contains("2 for 2") || q.contains("deload") || q.contains("not losing")) {
            return "Breaking Through Strength & Fat Loss Plateaus:\n\n" +
                    "1. **Strength Plateau**: Apply the **2-for-2 Rule**:\n" +
                    "   • If you can complete 2 additional reps past your target on your last set for 2 consecutive sessions, increase the weight by 1.25kg to 2.5kg.\n" +
                    "   • Take a **1-Week Deload**: Reduce training volume by 50% for 7 days. This allows accumulated systemic CNS fatigue to dissipate while preserving muscle.\n\n" +
                    "2. **Weight Loss Plateau**:\n" +
                    "   • **Metabolic Adaptation**: As you drop weight, your body requires fewer calories to operate. Recalculate your TDEE for your new lower weight.\n" +
                    "   • **Refeed Day**: Have 1 day eating at maintenance calories (boost carbs, keep fats low) to stimulate leptin and thyroid function.";
        }

        // 25. Motivation & Consistency
        if (q.contains("motivation") || q.contains("consistent") || q.contains("lazy")
                || q.contains("discipline") || q.contains("give up") || q.contains("hard")) {
            return "Building Unshakable Fitness Consistency:\n\n" +
                    "1. **Action Precedes Motivation**: You will rarely 'feel' motivated before walking into the gym. Motivation is the byproduct of taking action and seeing small wins, not the prerequisite.\n" +
                    "2. **The 2-Minute Rule**: On days you feel exhausted, commit to doing just 5 minutes or 1 exercise. 90% of the time, once you warm up, momentum carries you through the full session.\n" +
                    "3. **Never Miss Twice**: Missing 1 workout or 1 meal won't derail you; missing 2 in a row creates a downward spiral. Reset on your very next meal.\n" +
                    "4. **Track Everything**: Use FitTrack's nutrition and workout logs. What gets measured gets managed!";
        }

        // 26. Exercise Form & Breathing Technique
        if (q.contains("form") || q.contains("technique") || q.contains("breathe") || q.contains("breathing") || q.contains("brace")) {
            return "Universal Gym Form & Intra-Abdominal Bracing Cue:\n\n" +
                    "1. **The Valsalva Maneuver (Core Bracing)**: Take a deep diaphragmatic breath into your belly (expand 360°), brace your abs as if expecting a punch, and hold during the descent. Exhale through pursed lips past the sticking point.\n" +
                    "2. **Scapular Control**: On chest and overhead presses, retract and depress your shoulder blades (pull them back and down into your back pockets) to lock the glenohumeral joint in place.\n" +
                    "3. **Control the Eccentric**: Lower every lift over 2-3 seconds under active muscular control; never drop the weight freely.";
        }

        // 27. Specific Follow-Up / Rehab steps
        if ((q.contains("what can i do") || q.contains("what should i do") || q.contains("what next"))
                && (q.contains("recover") || q.contains("heal") || q.contains("swelling") || q.contains("rehab"))) {
            return "Targeted Active Recovery Protocol:\n\n" +
                    "1. **Active Rest**: Shift today's training to an unaffected muscle group (e.g. if upper body is sore, do legs or Zone 2 cardio).\n" +
                    "2. **Temperature Therapy**: Use cold compression for 15 minutes if there is acute swelling; use warm showers or heating pads for tight, guarded muscles to boost circulation.\n" +
                    "3. **Hydration & Micronutrients**: Drink 3.5 liters of water and consume anti-inflammatory staples (haldi doodh with black pepper, omega-3s, and zinc).\n" +
                    "4. **Light Mobilization**: Gentle unloaded range of motion exercises (pendulum swings, cat-cows) circulate synovial fluid into the joint capsule.";
        }

        // 28. Smart Context-Aware Dynamic Fallback
        return "FitTrack Coach Analysis for: \"" + message + "\"\n\n" +
                (userContext != null && !userContext.isEmpty() ? "**Your Profile Data**: " + userContext + "\n\n" : "") +
                "Here are actionable suggestions tailored to your goals:\n" +
                "1. **Nutrition & Deficit**: Align your meals with your target daily calories and protein goals. Try high-protein Indian staples like soya chunks, low-fat paneer, eggs, and moong dal.\n" +
                "2. **Training Progression**: Apply progressive overload across your compound lifts (bench, rows, squats, overhead presses) by tracking your weights and reps.\n" +
                "3. **Recovery**: Ensure 7-8 hours of sleep and 3.5L of daily water intake.\n\n" +
                "Tip: Ask me specifically about **'chest workout'**, **'Indian veg protein'**, **'shoulder pain'**, or **'calorie deficit math'** for instant deep-dive protocols!";
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
