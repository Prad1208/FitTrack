import React, { useState, useEffect } from 'react';
import api from '../api/client';
import { 
  Sparkles, 
  Dumbbell, 
  UtensilsCrossed, 
  Footprints, 
  Bot, 
  Send, 
  RefreshCw,
  Key,
  ShieldCheck,
  Cpu,
  X,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

export default function AiCoachPage() {
  const [activeTab, setActiveTab] = useState('workout'); // workout, nutrition, running, chat

  // Gemini API Key State
  const [geminiKey, setGeminiKey] = useState('');
  const [showKeyModal, setShowKeyModal] = useState(false);
  const [inputKey, setInputKey] = useState('');

  useEffect(() => {
    const saved = localStorage.getItem('fittrack_gemini_key') || '';
    setGeminiKey(saved);
    setInputKey(saved);
  }, []);

  const handleSaveKey = () => {
    const trimmed = inputKey.trim();
    if (trimmed) {
      localStorage.setItem('fittrack_gemini_key', trimmed);
      setGeminiKey(trimmed);
    } else {
      localStorage.removeItem('fittrack_gemini_key');
      setGeminiKey('');
    }
    setShowKeyModal(false);
  };

  const handleRemoveKey = () => {
    localStorage.removeItem('fittrack_gemini_key');
    setGeminiKey('');
    setInputKey('');
    setShowKeyModal(false);
  };

  // AI Workout State
  const [splitType, setSplitType] = useState('Push Pull Legs (PPL)');
  const [daysPerWeek, setDaysPerWeek] = useState(3);
  const [equipment, setEquipment] = useState('Full Commercial Gym');
  const [fitnessLevel, setFitnessLevel] = useState('Intermediate');
  const [workoutPlan, setWorkoutPlan] = useState(null);
  const [loadingWorkout, setLoadingWorkout] = useState(false);

  // AI Nutrition State
  const [remainingCalories, setRemainingCalories] = useState(500);
  const [minProtein, setMinProtein] = useState(25);
  const [dietaryPref, setDietaryPref] = useState('Vegetarian');
  const [mealSuggestions, setMealSuggestions] = useState(null);
  const [loadingNutrition, setLoadingNutrition] = useState(false);

  // AI Running State
  const [runGoal, setRunGoal] = useState('5K');
  const [runLevel, setRunLevel] = useState('Beginner');
  const [runWeeks, setRunWeeks] = useState(4);
  const [runDays, setRunDays] = useState(3);
  const [runningPlan, setRunningPlan] = useState(null);
  const [loadingRunning, setLoadingRunning] = useState(false);

  // AI Chat Assistant State
  const [chatMessages, setChatMessages] = useState([
    {
      sender: 'ai',
      text: 'Hello! I am your FitTrack AI Coach. Ask me anything about exercise substitutions (e.g. if your shoulders or knees hurt), Indian vegetarian/non-vegetarian high protein meal strategies, progressive overload, or calorie deficit tracking!'
    }
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [loadingChat, setLoadingChat] = useState(false);

  // Quick Prompt Chips
  const promptSuggestions = [
    "Today my shoulder is paining, what can I do?",
    "How to hit 100g protein on an Indian vegetarian diet?",
    "Best exercises to substitute squats if my knees hurt?",
    "I'm stuck in a bench press plateau, how to apply 2-for-2 rule?",
    "How much water and creatine should I take daily?"
  ];

  // Handlers
  const handleGenerateWorkout = async () => {
    try {
      setLoadingWorkout(true);
      const res = await api.post('/ai/generate-workout', {
        splitType,
        daysPerWeek: Number(daysPerWeek),
        equipment,
        fitnessLevel,
      });
      const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
      setWorkoutPlan(data);
    } catch (err) {
      console.error('Failed to generate workout plan', err);
    } finally {
      setLoadingWorkout(false);
    }
  };

  const handleSuggestMeals = async () => {
    try {
      setLoadingNutrition(true);
      const res = await api.post('/ai/suggest-meals', {
        remainingCalories: Number(remainingCalories),
        minProteinGrams: Number(minProtein),
        dietaryPreference: dietaryPref,
      });
      const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
      setMealSuggestions(data);
    } catch (err) {
      console.error('Failed to suggest meals', err);
    } finally {
      setLoadingNutrition(false);
    }
  };

  const handleGenerateRunningPlan = async () => {
    try {
      setLoadingRunning(true);
      const res = await api.post('/ai/generate-running-plan', {
        currentLevel: runLevel,
        targetGoal: runGoal,
        weeksCount: Number(runWeeks),
        daysPerWeek: Number(runDays),
      });
      const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
      setRunningPlan(data);
    } catch (err) {
      console.error('Failed to generate running plan', err);
    } finally {
      setLoadingRunning(false);
    }
  };

  const sendChatMessage = async (msgText) => {
    const textToSend = msgText || inputMessage;
    if (!textToSend.trim() || loadingChat) return;

    setInputMessage('');
    setChatMessages((prev) => [...prev, { sender: 'user', text: textToSend }]);

    try {
      setLoadingChat(true);
      const res = await api.post('/ai/assistant-chat', { message: textToSend });
      setChatMessages((prev) => [...prev, { sender: 'ai', text: res.data.response }]);
    } catch (err) {
      console.error('Chat error', err);
      setChatMessages((prev) => [
        ...prev, 
        { sender: 'ai', text: 'Apologies, I encountered an issue communicating with the AI intelligence server. Please try again.' }
      ]);
    } finally {
      setLoadingChat(false);
    }
  };

  const handleSendMessage = (e) => {
    e.preventDefault();
    sendChatMessage();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      {/* Header & Engine Status Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-brand-400">
            <Sparkles className="w-4 h-4" /> FitTrack Intelligence Suite
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white mt-1">AI Fitness & Coaching Suite</h1>
          <p className="text-sm text-slate-400 mt-1">
            Periodized gym routines, macro-aligned meal options, running schedules, and sports-science chat assistance.
          </p>
        </div>

        {/* Engine Status Badge & Key Button */}
        <div className="flex items-center gap-3">
          {geminiKey ? (
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              Google Gemini 2.5 Live Active
            </div>
          ) : (
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-brand-500/10 border border-brand-500/20 text-brand-400 text-xs font-semibold">
              <Cpu className="w-3.5 h-3.5" />
              FitTrack Sports Engine Active
            </div>
          )}

          <button
            onClick={() => setShowKeyModal(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 hover:text-white text-xs font-semibold transition-all shadow-sm"
          >
            <Key className="w-3.5 h-3.5 text-amber-400" />
            {geminiKey ? 'Gemini Key Configured' : 'Connect Gemini API Key'}
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 gap-2 overflow-x-auto pb-1">
        {[
          { id: 'workout', label: 'AI Gym Planner', icon: Dumbbell },
          { id: 'nutrition', label: 'AI Nutrition & Meals', icon: UtensilsCrossed },
          { id: 'running', label: 'AI Running Coach', icon: Footprints },
          { id: 'chat', label: 'AI Fitness Assistant', icon: Bot },
        ].map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-5 py-3 rounded-2xl text-xs sm:text-sm font-bold whitespace-nowrap transition-all ${
                activeTab === tab.id
                  ? 'bg-slate-800 text-brand-400 border border-slate-700/80 shadow-md'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
              }`}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* TAB 1: AI GYM WORKOUT PLANNER */}
      {activeTab === 'workout' && (
        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Dumbbell className="w-4 h-4 text-brand-400" /> Configure Workout Routine
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Split Type</label>
                <select
                  value={splitType}
                  onChange={(e) => setSplitType(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                >
                  <option value="Push Pull Legs (PPL)">Push Pull Legs (PPL)</option>
                  <option value="Upper Lower Split">Upper Lower Split</option>
                  <option value="Arnold Bro Split">Arnold / Bro Split</option>
                  <option value="Full Body Routine">Full Body Routine</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Days Per Week</label>
                <select
                  value={daysPerWeek}
                  onChange={(e) => setDaysPerWeek(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                >
                  <option value={2}>2 Days</option>
                  <option value={3}>3 Days</option>
                  <option value={4}>4 Days</option>
                  <option value={5}>5 Days</option>
                  <option value={6}>6 Days</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Equipment</label>
                <select
                  value={equipment}
                  onChange={(e) => setEquipment(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                >
                  <option value="Full Commercial Gym">Full Commercial Gym</option>
                  <option value="Dumbbells & Bench Only">Dumbbells & Bench Only</option>
                  <option value="Home / Calisthenics Bodyweight">Home / Bodyweight Only</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Fitness Level</label>
                <select
                  value={fitnessLevel}
                  onChange={(e) => setFitnessLevel(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                >
                  <option value="Beginner">Beginner (Linear progression, RPE 7)</option>
                  <option value="Intermediate">Intermediate (Wave periodization, RPE 8)</option>
                  <option value="Advanced">Advanced (High volume / RPE 9-10)</option>
                </select>
              </div>
            </div>

            <button
              onClick={handleGenerateWorkout}
              disabled={loadingWorkout}
              className="px-6 py-3 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-bold text-sm shadow-lg shadow-brand-500/20 transition-all flex items-center gap-2"
            >
              {loadingWorkout ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
              Generate AI Periodized Routine
            </button>
          </div>

          {/* Generated Routine Schedule */}
          {workoutPlan && (
            <div className="space-y-6">
              <div className="bg-slate-900 border border-slate-800 p-6 rounded-3xl shadow-xl flex items-center justify-between">
                <div>
                  <h3 className="text-xl font-black text-white">{workoutPlan.planName}</h3>
                  <div className="text-xs text-slate-400 mt-1">
                    Split: {workoutPlan.splitType} • {workoutPlan.daysPerWeek || daysPerWeek} Days/Week • {equipment}
                  </div>
                </div>
                <span className="text-xs font-bold px-3 py-1.5 rounded-full bg-brand-500/10 text-brand-400 border border-brand-500/20">
                  {geminiKey ? 'Gemini 2.5 Flash' : 'Calculated Routine'}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {workoutPlan.schedule?.map((day, idx) => (
                  <div key={idx} className="bg-slate-900 border border-slate-800 rounded-3xl p-5 shadow-lg space-y-4">
                    <div className="border-b border-slate-800 pb-3">
                      <div className="text-xs font-bold text-brand-400 uppercase tracking-wider">
                        {day.targetMuscles?.join(' • ')}
                      </div>
                      <h4 className="text-base font-bold text-white mt-1">{day.dayName}</h4>
                    </div>

                    <div className="space-y-3">
                      {day.exercises?.map((ex, exIdx) => (
                        <div key={exIdx} className="bg-slate-950/70 border border-slate-800/80 p-3 rounded-2xl space-y-1">
                          <div className="flex items-center justify-between">
                            <span className="text-xs font-bold text-white">{ex.name}</span>
                            <span className="text-[10px] font-mono font-bold text-purple-400">
                              {ex.sets} sets × {ex.repRange}
                            </span>
                          </div>
                          {ex.formTip && (
                            <p className="text-[11px] text-slate-400 italic">Cue: {ex.formTip}</p>
                          )}
                          <div className="flex gap-2 text-[10px] text-slate-500 font-mono">
                            <span>Rest: {ex.restSeconds}s</span>
                            {ex.rpeTarget && <span>• RPE {ex.rpeTarget}</span>}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: AI NUTRITION & MEAL SUGGESTIONS */}
      {activeTab === 'nutrition' && (
        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <UtensilsCrossed className="w-4 h-4 text-amber-400" /> Suggest Meals Aligned with Calorie Deficit
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Remaining Calorie Ceiling (kcal)</label>
                <input
                  type="number"
                  value={remainingCalories}
                  onChange={(e) => setRemainingCalories(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:outline-none focus:border-amber-500"
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Minimum Protein Target (g)</label>
                <input
                  type="number"
                  value={minProtein}
                  onChange={(e) => setMinProtein(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:outline-none focus:border-amber-500"
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Dietary Preference</label>
                <select
                  value={dietaryPref}
                  onChange={(e) => setDietaryPref(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
                >
                  <option value="Vegetarian">Vegetarian (Paneer, Soya, Chana, Dal)</option>
                  <option value="High-Protein Omnivore">High-Protein Omnivore (Chicken, Eggs, Fish)</option>
                  <option value="Vegan">Vegan (Plant-based: Tofu, Soya, Millets)</option>
                  <option value="Low-Carb / Keto">Low-Carb / Keto (High-Protein & Healthy Fats)</option>
                </select>
              </div>
            </div>

            <button
              onClick={handleSuggestMeals}
              disabled={loadingNutrition}
              className="px-6 py-3 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-bold text-sm shadow-lg shadow-amber-600/20 transition-all flex items-center gap-2"
            >
              {loadingNutrition ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
              Generate Macro-Compliant Meals
            </button>
          </div>

          {/* Render Suggested Meals */}
          {mealSuggestions && Array.isArray(mealSuggestions) && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {mealSuggestions.map((meal, idx) => (
                <div key={idx} className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4 flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">
                        {meal.prepTimeMinutes || 15} mins prep
                      </span>
                      <span className="font-mono text-sm font-bold text-amber-400">
                        {meal.totalCalories} kcal
                      </span>
                    </div>

                    <h3 className="text-base font-bold text-white mt-2">{meal.mealName}</h3>
                    <p className="text-xs text-slate-400 mt-1">{meal.description}</p>

                    {/* Macro Pill Row */}
                    <div className="grid grid-cols-3 gap-2 mt-4 text-center text-xs font-mono">
                      <div className="bg-slate-950 p-2 rounded-xl border border-slate-800">
                        <span className="text-[10px] text-slate-500 block">Protein</span>
                        <span className="font-bold text-indigo-400">{meal.totalProteinGrams}g</span>
                      </div>
                      <div className="bg-slate-950 p-2 rounded-xl border border-slate-800">
                        <span className="text-[10px] text-slate-500 block">Carbs</span>
                        <span className="font-bold text-cyan-400">{meal.totalCarbsGrams}g</span>
                      </div>
                      <div className="bg-slate-950 p-2 rounded-xl border border-slate-800">
                        <span className="text-[10px] text-slate-500 block">Fats</span>
                        <span className="font-bold text-amber-300">{meal.totalFatsGrams}g</span>
                      </div>
                    </div>

                    {/* Ingredients List with Grams */}
                    <div className="mt-4 space-y-1.5">
                      <span className="text-xs font-semibold text-slate-300 block">Ingredients:</span>
                      {meal.ingredients?.map((ing, iIdx) => (
                        <div key={iIdx} className="flex justify-between text-xs text-slate-400 bg-slate-950/50 px-2.5 py-1.5 rounded-lg font-mono">
                          <span>{ing.name}</span>
                          <span className="text-slate-200">{ing.grams}g ({ing.calories} kcal)</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 3: AI RUNNING COACH */}
      {activeTab === 'running' && (
        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Footprints className="w-4 h-4 text-cyan-400" /> Plan Running Milestones
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
              <div>
                <label className="text-xs text-slate-400 block mb-1">Target Milestone</label>
                <select
                  value={runGoal}
                  onChange={(e) => setRunGoal(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                >
                  <option value="2K Speed Builder">2K Speed Milestone</option>
                  <option value="5K Finish / Foundation">5K Foundation Run</option>
                  <option value="10K Endurance Builder">10K Endurance</option>
                  <option value="Fat Burn HIIT Sprint Intervals">Fat Burn HIIT Sprints</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Current Runner Level</label>
                <select
                  value={runLevel}
                  onChange={(e) => setRunLevel(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                >
                  <option value="Beginner">Beginner (Walk/Jog Intervals)</option>
                  <option value="Intermediate">Intermediate (Consistent Pace)</option>
                  <option value="Advanced">Advanced (Lactate Threshold)</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Duration (Weeks)</label>
                <select
                  value={runWeeks}
                  onChange={(e) => setRunWeeks(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                >
                  <option value={4}>4 Weeks</option>
                  <option value={8}>8 Weeks</option>
                  <option value={12}>12 Weeks</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Runs Per Week</label>
                <select
                  value={runDays}
                  onChange={(e) => setRunDays(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
                >
                  <option value={2}>2 Days</option>
                  <option value={3}>3 Days</option>
                  <option value={4}>4 Days</option>
                </select>
              </div>
            </div>

            <button
              onClick={handleGenerateRunningPlan}
              disabled={loadingRunning}
              className="px-6 py-3 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-sm shadow-lg shadow-cyan-600/20 transition-all flex items-center gap-2"
            >
              {loadingRunning ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
              Generate Progressive Running Plan
            </button>
          </div>

          {/* Render Running Plan Schedule */}
          {runningPlan && (
            <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-6">
              <h3 className="text-xl font-black text-white">{runningPlan.planName}</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {runningPlan.schedule?.map((s, idx) => (
                  <div key={idx} className="bg-slate-950/70 border border-slate-800 p-5 rounded-2xl space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-cyan-400 uppercase">{s.day}</span>
                      <span className="text-xs font-mono font-bold text-white">{s.distanceKm} km</span>
                    </div>
                    <div className="text-sm font-bold text-white">{s.type}</div>
                    <div className="text-xs text-slate-400 font-mono">Pace: {s.targetPace}</div>
                    <p className="text-xs text-slate-400 mt-2">{s.notes}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 4: AI FITNESS ASSISTANT CHATBOT */}
      {activeTab === 'chat' && (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl shadow-xl flex flex-col h-[700px] overflow-hidden">
          
          {/* Chat Header */}
          <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-950/40">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-400">
                <Bot className="w-5 h-5" />
              </div>
              <div>
                <div className="text-sm font-bold text-white flex items-center gap-2">
                  FitTrack AI Coach
                  {geminiKey ? (
                    <span className="text-[10px] bg-emerald-500/20 text-emerald-400 px-2 py-0.5 rounded-full border border-emerald-500/30">
                      Gemini 2.5 Live
                    </span>
                  ) : (
                    <span className="text-[10px] bg-brand-500/20 text-brand-400 px-2 py-0.5 rounded-full border border-brand-500/30">
                      Sports Intelligence Engine
                    </span>
                  )}
                </div>
                <div className="text-xs text-slate-400 flex items-center gap-1.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  Grounded with your profile, joint biomechanics & calorie deficit targets
                </div>
              </div>
            </div>

            <button
              onClick={() => setShowKeyModal(true)}
              className="text-xs text-slate-400 hover:text-white flex items-center gap-1 px-2.5 py-1 rounded-lg bg-slate-800/80 hover:bg-slate-800 border border-slate-700 transition-all"
            >
              <Key className="w-3 h-3 text-amber-400" />
              {geminiKey ? 'API Key Active' : 'API Key'}
            </button>
          </div>

          {/* Quick Suggestion Chips */}
          <div className="px-4 py-2 bg-slate-950/60 border-b border-slate-800/80 flex items-center gap-2 overflow-x-auto no-scrollbar">
            <span className="text-[11px] text-slate-500 whitespace-nowrap font-medium">Quick Prompts:</span>
            {promptSuggestions.map((prompt, idx) => (
              <button
                key={idx}
                onClick={() => sendChatMessage(prompt)}
                disabled={loadingChat}
                className="text-[11px] text-slate-300 hover:text-white bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-full px-3 py-1 whitespace-nowrap transition-colors"
              >
                {prompt}
              </button>
            ))}
          </div>

          {/* Chat Messages Log */}
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {chatMessages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex gap-3 max-w-2xl ${msg.sender === 'user' ? 'ml-auto justify-end' : 'mr-auto'}`}
              >
                {msg.sender === 'ai' && (
                  <div className="w-8 h-8 rounded-lg bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-400 flex-shrink-0 mt-1">
                    <Sparkles className="w-4 h-4" />
                  </div>
                )}
                <div
                  className={`p-4 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap ${
                    msg.sender === 'user'
                      ? 'bg-brand-600 text-white shadow-md'
                      : 'bg-slate-950/90 border border-slate-800 text-slate-200'
                  }`}
                >
                  {msg.text}
                </div>
              </div>
            ))}
            {loadingChat && (
              <div className="flex items-center gap-2 text-xs text-slate-400 italic">
                <Sparkles className="w-3.5 h-3.5 animate-spin text-brand-400" />
                FitTrack AI Coach is formulating your personalized guidance...
              </div>
            )}
          </div>

          {/* Input Form */}
          <form onSubmit={handleSendMessage} className="p-4 border-t border-slate-800 bg-slate-950/60 flex gap-3">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="e.g. Today my shoulder is paining, what should I substitute? Or suggest 40g veg protein..."
              className="flex-1 bg-slate-900 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-brand-500"
            />
            <button
              type="submit"
              disabled={loadingChat || !inputMessage.trim()}
              className="px-5 py-3 rounded-xl bg-brand-600 hover:bg-brand-500 disabled:opacity-50 text-white font-semibold transition-colors flex items-center justify-center shadow-lg shadow-brand-500/20"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>

        </div>
      )}

      {/* GEMINI API KEY MODAL */}
      {showKeyModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-lg w-full p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-white font-bold text-lg">
                <Key className="w-5 h-5 text-amber-400" /> Configure Google Gemini API Key
              </div>
              <button
                onClick={() => setShowKeyModal(false)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-400 leading-relaxed">
              Connect your free Gemini API key to enable 100% unconstrained cloud AI generation with <strong>Gemini 2.5 Flash</strong>. Without a key, FitTrack automatically runs on its built-in certified sports science calculation engine.
            </p>

            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-300 block">Gemini API Key</label>
              <input
                type="password"
                value={inputKey}
                onChange={(e) => setInputKey(e.target.value)}
                placeholder="AIzaSy..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white font-mono focus:outline-none focus:border-brand-500"
              />
              <p className="text-[11px] text-slate-500">
                Get a free key in 10 seconds from{' '}
                <a
                  href="https://aistudio.google.com/app/apikey"
                  target="_blank"
                  rel="noreferrer"
                  className="text-brand-400 hover:underline"
                >
                  Google AI Studio (aistudio.google.com)
                </a>. Your key stays private in your browser.
              </p>
            </div>

            <div className="flex items-center justify-between pt-2">
              {geminiKey ? (
                <button
                  type="button"
                  onClick={handleRemoveKey}
                  className="text-xs text-rose-400 hover:text-rose-300 font-semibold"
                >
                  Remove Key
                </button>
              ) : <div />}

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowKeyModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSaveKey}
                  className="px-5 py-2 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-xs font-bold shadow-lg shadow-brand-500/20 transition-all"
                >
                  Save & Activate
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
