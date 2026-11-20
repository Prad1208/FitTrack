import React, { useState, useEffect } from 'react';
import api from '../api/client';
import RestTimer from '../components/RestTimer';
import { 
  Dumbbell, 
  Plus, 
  Check, 
  Clock, 
  Flame, 
  TrendingUp, 
  Award, 
  Layers,
  ChevronDown
} from 'lucide-react';

export default function WorkoutPage() {
  const [activeSession, setActiveSession] = useState(null);
  const [routineName, setRoutineName] = useState('Push Day (Chest, Shoulders & Triceps)');
  const [selectedMuscle, setSelectedMuscle] = useState('CHEST');
  const [exerciseName, setExerciseName] = useState('Barbell Bench Press');
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  // Set Inputs
  const [setNumber, setSetNumber] = useState(1);
  const [weightKg, setWeightKg] = useState(60);
  const [reps, setReps] = useState(10);
  const [rpe, setRpe] = useState(8);
  const [selectedExerciseId, setSelectedExerciseId] = useState(null);

  const muscleExercises = {
    CHEST: ['Barbell Bench Press', 'Incline Dumbbell Press', 'Cable Crossover', 'Dips', 'Push-ups'],
    BACK: ['Lat Pulldown', 'Seated Cable Row', 'Barbell Bent-over Row', 'Pull-ups', 'Face Pulls'],
    SHOULDERS: ['Overhead Barbell Press', 'Seated Dumbbell Press', 'Dumbbell Lateral Raises', 'Rear Delt Flyes'],
    LEGS: ['Barbell Back Squat', 'Romanian Deadlift (RDL)', 'Leg Press', 'Bulgarian Split Squats', 'Leg Curls'],
    BICEPS: ['Barbell Bicep Curl', 'Hammer Curls', 'Incline Dumbbell Curl', 'Preacher Curls'],
    TRICEPS: ['Tricep Rope Pushdowns', 'Close-Grip Bench Press', 'Skull Crushers', 'Overhead Tricep Extension'],
    ABS: ['Hanging Leg Raises', 'Cable Crunches', 'Floor Crunches', 'Ab Wheel Rollouts'],
    CORE: ['Plank (Weighted)', 'Russian Twists', 'Pallof Press', 'Deadbugs']
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const res = await api.get('/workouts/history');
      setHistory(res.data);
    } catch (err) {
      console.error('Failed to load workout history', err);
    }
  };

  const handleStartWorkout = async () => {
    try {
      setLoading(true);
      const res = await api.post('/workouts/sessions', {
        routineName,
        sessionDate: new Date().toISOString().split('T')[0],
      });
      setActiveSession(res.data);
    } catch (err) {
      console.error('Failed to start workout', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddExercise = async () => {
    if (!activeSession) return;
    try {
      const res = await api.post(`/workouts/sessions/${activeSession.id}/exercises`, {
        exerciseName,
        muscleGroup: selectedMuscle,
      });
      setActiveSession(res.data);
      if (res.data.exercises.length > 0) {
        const lastEx = res.data.exercises[res.data.exercises.length - 1];
        setSelectedExerciseId(lastEx.id);
        setSetNumber(1);
      }
    } catch (err) {
      console.error('Failed to add exercise', err);
    }
  };

  const handleLogSet = async (exId) => {
    if (!activeSession) return;
    try {
      const res = await api.post(`/workouts/sessions/${activeSession.id}/exercises/${exId}/sets`, {
        setNumber: Number(setNumber),
        weightKg: Number(weightKg),
        reps: Number(reps),
        rpe: Number(rpe),
        isWarmup: false,
      });
      setActiveSession(res.data);
      setSetNumber((prev) => prev + 1);
    } catch (err) {
      console.error('Failed to log set', err);
    }
  };

  const handleCompleteWorkout = async () => {
    if (!activeSession) return;
    try {
      const res = await api.put(`/workouts/sessions/${activeSession.id}/complete`, {
        durationMinutes: 60,
        notes: 'Great workout with progressive overload achieved!',
      });
      setActiveSession(null);
      fetchHistory();
    } catch (err) {
      console.error('Failed to complete workout', err);
    }
  };

  // Epley 1RM formula calculation
  const est1Rm = reps > 0 ? (weightKg * (1 + reps / 30)).toFixed(1) : 0;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      {/* Header & Rest Timer */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">Gym Workout Tracker</h1>
          <p className="text-sm text-slate-400 mt-1">
            Target Chest, Back, Shoulders, Legs, Biceps, Triceps, Abs & Core. Log sets, reps, and track 1RM progressive overload.
          </p>
        </div>

        {/* Global Rest Timer */}
        <div className="w-full lg:w-auto">
          <RestTimer />
        </div>
      </div>

      {/* Active Workout Session Section */}
      {!activeSession ? (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-xl text-center max-w-xl mx-auto space-y-5">
          <div className="w-16 h-16 rounded-3xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400 mx-auto">
            <Dumbbell className="w-8 h-8" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-white">Start New Gym Session</h2>
            <p className="text-xs text-slate-400 mt-1">Choose a workout split or create a custom routine.</p>
          </div>

          <div className="space-y-3 text-left">
            <label className="text-xs font-semibold text-slate-300 block">Routine Name</label>
            <input
              type="text"
              value={routineName}
              onChange={(e) => setRoutineName(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:border-brand-500"
            />
          </div>

          <div className="flex gap-2 justify-center flex-wrap">
            {['Push Day (Chest/Shoulders/Triceps)', 'Pull Day (Back/Biceps)', 'Legs & Abs', 'Full Body Strength'].map((preset) => (
              <button
                key={preset}
                onClick={() => setRoutineName(preset)}
                className="px-3 py-1.5 rounded-lg text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700/60 transition-colors"
              >
                {preset.split(' ')[0]}
              </button>
            ))}
          </div>

          <button
            onClick={handleStartWorkout}
            disabled={loading}
            className="w-full py-3 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-bold text-sm shadow-lg shadow-purple-600/20 transition-all flex items-center justify-center gap-2"
          >
            <Dumbbell className="w-5 h-5" /> Begin Workout Session
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Active Session Status Bar */}
          <div className="bg-purple-950/40 border border-purple-800/60 p-6 rounded-3xl shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-purple-400">Active Session In Progress</span>
              <h2 className="text-2xl font-black text-white mt-1">{activeSession.routineName}</h2>
              <div className="text-xs text-slate-400 mt-1">
                Total Volume: <span className="font-mono font-bold text-purple-300">{Math.round(activeSession.totalVolumeKg || 0)} kg</span>
              </div>
            </div>

            <button
              onClick={handleCompleteWorkout}
              className="px-6 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm shadow-lg shadow-emerald-600/30 transition-all self-start sm:self-auto flex items-center gap-2"
            >
              <Check className="w-4 h-4" /> Finish & Save Workout
            </button>
          </div>

          {/* Add Exercise Panel */}
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Plus className="w-4 h-4 text-purple-400" /> Add Exercise to Session
            </h3>

            {/* Muscle Group Chips */}
            <div className="flex gap-2 overflow-x-auto pb-1">
              {Object.keys(muscleExercises).map((muscle) => (
                <button
                  key={muscle}
                  onClick={() => {
                    setSelectedMuscle(muscle);
                    setExerciseName(muscleExercises[muscle][0]);
                  }}
                  className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                    selectedMuscle === muscle
                      ? 'bg-purple-600 text-white shadow-md'
                      : 'bg-slate-800 text-slate-400 hover:text-white'
                  }`}
                >
                  {muscle}
                </button>
              ))}
            </div>

            {/* Exercise Selector & Button */}
            <div className="flex flex-col sm:flex-row gap-3">
              <select
                value={exerciseName}
                onChange={(e) => setExerciseName(e.target.value)}
                className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-purple-500"
              >
                {muscleExercises[selectedMuscle].map((ex) => (
                  <option key={ex} value={ex}>{ex}</option>
                ))}
              </select>
              <button
                onClick={handleAddExercise}
                className="px-5 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-semibold text-xs transition-colors flex items-center justify-center gap-2"
              >
                <Plus className="w-4 h-4" /> Add Exercise
              </button>
            </div>
          </div>

          {/* Active Exercises & Sets Table */}
          <div className="space-y-6">
            {activeSession.exercises.map((exercise) => (
              <div key={exercise.id} className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                  <div>
                    <h4 className="text-lg font-bold text-white">{exercise.exerciseName}</h4>
                    <span className="text-[11px] font-semibold text-purple-400 uppercase tracking-wider">
                      {exercise.muscleGroup}
                    </span>
                  </div>
                </div>

                {/* Logged Sets Table */}
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead>
                      <tr className="border-b border-slate-800 text-slate-400 uppercase font-semibold">
                        <th className="py-2 px-3">Set</th>
                        <th className="py-2 px-3">Weight (kg)</th>
                        <th className="py-2 px-3">Reps</th>
                        <th className="py-2 px-3">RPE</th>
                        <th className="py-2 px-3">Est. 1RM</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/60 font-mono">
                      {exercise.sets.map((s) => (
                        <tr key={s.id} className="hover:bg-slate-800/30">
                          <td className="py-2 px-3 text-slate-300 font-bold">{s.setNumber}</td>
                          <td className="py-2 px-3 text-purple-300 font-bold">{s.weightKg} kg</td>
                          <td className="py-2 px-3 text-white">{s.reps}</td>
                          <td className="py-2 px-3 text-amber-400">{s.rpe || '-'}</td>
                          <td className="py-2 px-3 text-emerald-400 font-bold">{Math.round(s.estimated1RmKg)} kg</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Log Set Input Row */}
                <div className="bg-slate-950/70 p-4 rounded-2xl border border-slate-800/80 flex flex-wrap items-center gap-3">
                  <div className="w-20">
                    <label className="text-[10px] text-slate-400 block mb-1">Set #</label>
                    <input
                      type="number"
                      value={setNumber}
                      onChange={(e) => setSetNumber(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white font-mono"
                    />
                  </div>

                  <div className="w-24">
                    <label className="text-[10px] text-slate-400 block mb-1">Weight (kg)</label>
                    <input
                      type="number"
                      step="0.5"
                      value={weightKg}
                      onChange={(e) => setWeightKg(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white font-mono"
                    />
                  </div>

                  <div className="w-20">
                    <label className="text-[10px] text-slate-400 block mb-1">Reps</label>
                    <input
                      type="number"
                      value={reps}
                      onChange={(e) => setReps(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white font-mono"
                    />
                  </div>

                  <div className="w-20">
                    <label className="text-[10px] text-slate-400 block mb-1">RPE (1-10)</label>
                    <input
                      type="number"
                      min="1"
                      max="10"
                      value={rpe}
                      onChange={(e) => setRpe(e.target.value)}
                      className="w-full bg-slate-900 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-white font-mono"
                    />
                  </div>

                  <div className="w-28 text-center bg-slate-900/90 py-1.5 px-2 rounded-lg border border-slate-800">
                    <div className="text-[9px] text-slate-400">Est. 1RM</div>
                    <div className="text-xs font-mono font-bold text-emerald-400">{est1Rm} kg</div>
                  </div>

                  <button
                    onClick={() => handleLogSet(exercise.id)}
                    className="ml-auto px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-semibold text-xs shadow-md transition-all flex items-center gap-1.5"
                  >
                    <Plus className="w-3.5 h-3.5" /> Log Set
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Workout History Section */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
        <h3 className="text-base font-bold text-white flex items-center gap-2">
          <Layers className="w-4 h-4 text-purple-400" /> Past Workout History
        </h3>

        {history.length === 0 ? (
          <p className="text-xs text-slate-500 italic py-4">No completed workouts yet. Start your first session above!</p>
        ) : (
          <div className="space-y-3">
            {history.map((ws) => (
              <div
                key={ws.id}
                className="bg-slate-950/70 border border-slate-800 p-4 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-slate-700 transition-colors"
              >
                <div>
                  <div className="text-sm font-bold text-white">{ws.routineName}</div>
                  <div className="text-xs text-slate-400 mt-0.5">
                    {ws.sessionDate} • {ws.exercises?.length || 0} exercises
                  </div>
                </div>

                <div className="flex items-center gap-6 font-mono text-xs">
                  <div>
                    <span className="text-slate-400 block text-[10px]">Total Volume</span>
                    <span className="text-purple-400 font-bold">{Math.round(ws.totalVolumeKg || 0)} kg</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block text-[10px]">Duration</span>
                    <span className="text-slate-200">{ws.durationMinutes || 60}m</span>
                  </div>
                  <div>
                    <span className="text-slate-400 block text-[10px]">Burn</span>
                    <span className="text-rose-400 font-bold">{Math.round(ws.estimatedCaloriesBurnt || 0)} kcal</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
}
