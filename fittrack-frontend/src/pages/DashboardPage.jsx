import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';
import { 
  Flame, 
  Dumbbell, 
  Footprints, 
  Utensils, 
  TrendingDown, 
  PlusCircle, 
  Sparkles,
  Award,
  ChevronRight
} from 'lucide-react';

export default function DashboardPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const res = await api.get('/dashboard/summary');
      setSummary(res.data);
    } catch (err) {
      console.error('Failed to load dashboard summary', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[70vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-brand-500"></div>
      </div>
    );
  }

  const calorieIntake = summary?.calorieIntake || 0;
  const calorieTarget = summary?.calorieTarget || 2000;
  const tdee = summary?.tdeeKcal || 2500;
  const deficit = summary?.calorieDeficit || (tdee - calorieIntake);
  const totalBurn = summary?.estimatedCaloriesBurnt || 0;

  const protein = summary?.proteinConsumedGrams || 0;
  const proteinTarget = summary?.proteinTargetGrams || 150;
  const carbs = summary?.carbsConsumedGrams || 0;
  const carbsTarget = summary?.carbsTargetGrams || 200;
  const fats = summary?.fatsConsumedGrams || 0;
  const fatsTarget = summary?.fatsTargetGrams || 65;

  const proteinPct = Math.min(100, Math.round((protein / (proteinTarget || 1)) * 100));
  const caloriePct = Math.min(100, Math.round((calorieIntake / (calorieTarget || 1)) * 100));

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-slate-900 via-slate-850 to-slate-900 border border-slate-800 p-6 rounded-3xl shadow-xl">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-brand-400">
            <Sparkles className="w-4 h-4" /> Daily Fitness Overview
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-white mt-1">
            Welcome back, {user?.fullName?.split(' ')[0] || 'Athlete'}! 💪
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Targeting a <span className="text-emerald-400 font-semibold">500 kcal deficit</span> today for steady fat loss and lean muscle retention.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/nutrition"
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-500 text-white text-sm font-semibold shadow-lg shadow-brand-500/20 transition-all hover:scale-102"
          >
            <PlusCircle className="w-4 h-4" /> Log Food
          </Link>
          <Link
            to="/workouts"
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-sm font-semibold transition-all"
          >
            <Dumbbell className="w-4 h-4" /> Start Workout
          </Link>
        </div>
      </div>

      {/* Top 4 Core Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        
        {/* Calorie Deficit Engine Card */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden group">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Current Deficit</span>
            <div className="w-9 h-9 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <TrendingDown className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-3xl font-black text-white font-mono">
              {Math.round(deficit)} <span className="text-sm font-normal text-slate-400 font-sans">kcal</span>
            </div>
            <div className="text-xs text-slate-400 mt-1">
              TDEE ({Math.round(tdee)}) - Intake ({Math.round(calorieIntake)})
            </div>
          </div>
          <div className="mt-3 w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div 
              className="bg-emerald-500 h-full rounded-full transition-all duration-500" 
              style={{ width: `${Math.min(100, Math.max(10, (deficit / 500) * 100))}%` }}
            />
          </div>
        </div>

        {/* Calorie Intake Card */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Calorie Intake</span>
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
              <Utensils className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-3xl font-black text-white font-mono">
              {Math.round(calorieIntake)} <span className="text-sm font-normal text-slate-400 font-sans">/ {Math.round(calorieTarget)} kcal</span>
            </div>
            <div className="text-xs text-slate-400 mt-1">
              Remaining: <span className="text-amber-400 font-medium">{Math.max(0, Math.round(calorieTarget - calorieIntake))} kcal</span>
            </div>
          </div>
          <div className="mt-3 w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div 
              className={`h-full rounded-full transition-all duration-500 ${calorieIntake > calorieTarget ? 'bg-rose-500' : 'bg-amber-500'}`} 
              style={{ width: `${caloriePct}%` }}
            />
          </div>
        </div>

        {/* Protein Target Card */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Protein Target</span>
            <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
              <Award className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-3xl font-black text-white font-mono">
              {Math.round(protein)}g <span className="text-sm font-normal text-slate-400 font-sans">/ {Math.round(proteinTarget)}g</span>
            </div>
            <div className="text-xs text-slate-400 mt-1">
              {proteinPct}% of muscle preservation goal
            </div>
          </div>
          <div className="mt-3 w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div 
              className="bg-indigo-500 h-full rounded-full transition-all duration-500" 
              style={{ width: `${proteinPct}%` }}
            />
          </div>
        </div>

        {/* Estimated Calories Burnt Card */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Exercise Burn</span>
            <div className="w-9 h-9 rounded-xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center text-rose-400">
              <Flame className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <div className="text-3xl font-black text-white font-mono">
              {Math.round(totalBurn)} <span className="text-sm font-normal text-slate-400 font-sans">kcal</span>
            </div>
            <div className="text-xs text-slate-400 mt-1">
              From gym workouts & running sessions
            </div>
          </div>
          <div className="mt-3 w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
            <div 
              className="bg-rose-500 h-full rounded-full transition-all duration-500" 
              style={{ width: `${Math.min(100, (totalBurn / 600) * 100)}%` }}
            />
          </div>
        </div>

      </div>

      {/* Macronutrient Breakdown Bars */}
      <div className="bg-slate-900 border border-slate-800 p-6 rounded-3xl shadow-xl">
        <h2 className="text-base font-bold text-white mb-4">Macronutrient Distribution</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          
          <div>
            <div className="flex justify-between text-xs font-medium mb-1.5">
              <span className="text-indigo-400">Protein (4 kcal/g)</span>
              <span className="text-slate-300 font-mono">{Math.round(protein)} / {Math.round(proteinTarget)}g</span>
            </div>
            <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
              <div className="bg-indigo-500 h-full rounded-full" style={{ width: `${proteinPct}%` }}></div>
            </div>
          </div>

          <div>
            <div className="flex justify-between text-xs font-medium mb-1.5">
              <span className="text-cyan-400">Carbohydrates (4 kcal/g)</span>
              <span className="text-slate-300 font-mono">{Math.round(carbs)} / {Math.round(carbsTarget)}g</span>
            </div>
            <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
              <div 
                className="bg-cyan-500 h-full rounded-full" 
                style={{ width: `${Math.min(100, Math.round((carbs / (carbsTarget || 1)) * 100))}%` }}
              ></div>
            </div>
          </div>

          <div>
            <div className="flex justify-between text-xs font-medium mb-1.5">
              <span className="text-amber-400">Healthy Fats (9 kcal/g)</span>
              <span className="text-slate-300 font-mono">{Math.round(fats)} / {Math.round(fatsTarget)}g</span>
            </div>
            <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
              <div 
                className="bg-amber-500 h-full rounded-full" 
                style={{ width: `${Math.min(100, Math.round((fats / (fatsTarget || 1)) * 100))}%` }}
              ></div>
            </div>
          </div>

        </div>
      </div>

      {/* Two-Column: Today's Workout & Today's Running */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Workout Activity Card */}
        <div className="bg-slate-900 border border-slate-800 p-6 rounded-3xl shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
                  <Dumbbell className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-white">Today's Gym Workout</h3>
              </div>
              <span className="text-xs px-2.5 py-1 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                Resistance
              </span>
            </div>

            {summary?.todayWorkoutRoutine ? (
              <div className="space-y-3 bg-slate-950/60 p-4 rounded-2xl border border-slate-800/80">
                <div className="text-lg font-bold text-white">{summary.todayWorkoutRoutine}</div>
                <div className="grid grid-cols-2 gap-4 text-xs">
                  <div>
                    <span className="text-slate-400 block">Duration:</span>
                    <span className="text-white font-mono font-semibold">{summary.workoutDurationMinutes} mins</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block">Total Volume:</span>
                    <span className="text-white font-mono font-semibold">{Math.round(summary.workoutVolumeKg || 0)} kg</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-8 bg-slate-950/30 rounded-2xl border border-dashed border-slate-800">
                <p className="text-sm text-slate-400">No workout logged yet today.</p>
                <p className="text-xs text-slate-500 mt-1">Chest, Back, Shoulders, Legs, Abs or Arms?</p>
              </div>
            )}
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800 flex justify-between items-center">
            <span className="text-xs text-slate-400 font-medium">
              Weekly workouts: <span className="text-brand-400 font-bold">{summary?.weeklyWorkoutCount || 0} sessions</span>
            </span>
            <Link
              to="/workouts"
              className="text-xs font-semibold text-brand-400 hover:text-brand-300 flex items-center gap-1"
            >
              Open Gym Hub <ChevronRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>

        {/* Running Activity Card */}
        <div className="bg-slate-900 border border-slate-800 p-6 rounded-3xl shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
                  <Footprints className="w-4 h-4" />
                </div>
                <h3 className="font-bold text-white">Today's Running</h3>
              </div>
              <span className="text-xs px-2.5 py-1 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                Cardio & HIIT
              </span>
            </div>

            {summary?.runningDistanceKm && summary.runningDistanceKm > 0 ? (
              <div className="space-y-3 bg-slate-950/60 p-4 rounded-2xl border border-slate-800/80">
                <div className="text-2xl font-black text-cyan-400 font-mono">
                  {summary.runningDistanceKm} <span className="text-sm text-slate-400 font-sans">km completed</span>
                </div>
                <div className="text-xs text-slate-400">
                  Time: <span className="text-white font-mono font-medium">{Math.floor(summary.runningDurationSeconds / 60)}m {summary.runningDurationSeconds % 60}s</span>
                </div>
              </div>
            ) : (
              <div className="text-center py-8 bg-slate-950/30 rounded-2xl border border-dashed border-slate-800">
                <p className="text-sm text-slate-400">No running recorded today.</p>
                <p className="text-xs text-slate-500 mt-1">Ready for a 2K, 5K, or tempo interval run?</p>
              </div>
            )}
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800 flex justify-between items-center">
            <span className="text-xs text-slate-400 font-medium">
              Weekly distance: <span className="text-cyan-400 font-bold">{summary?.weeklyRunningTotalKm || 0} km</span>
            </span>
            <Link
              to="/running"
              className="text-xs font-semibold text-cyan-400 hover:text-cyan-300 flex items-center gap-1"
            >
              Open Running Log <ChevronRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>

      </div>

    </div>
  );
}
