import React, { useState, useEffect } from 'react';
import api from '../api/client';
import { 
  Footprints, 
  Flame, 
  Timer, 
  Trophy, 
  Plus, 
  Calendar,
  Zap,
  TrendingUp
} from 'lucide-react';

export default function RunningPage() {
  const [runs, setRuns] = useState([]);
  const [stats, setStats] = useState(null);
  const [distanceKm, setDistanceKm] = useState(5.0);
  const [durationMinutes, setDurationMinutes] = useState(28);
  const [durationSeconds, setDurationSeconds] = useState(30);
  const [targetGoal, setTargetGoal] = useState('5K');
  const [notes, setNotes] = useState('Morning endurance run in zone 2');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchRunningData();
  }, []);

  const fetchRunningData = async () => {
    try {
      const [runsRes, statsRes] = await Promise.all([
        api.get('/running/logs'),
        api.get('/running/stats'),
      ]);
      setRuns(runsRes.data);
      setStats(statsRes.data);
    } catch (err) {
      console.error('Failed to load running data', err);
    }
  };

  const totalSeconds = (Number(durationMinutes) || 0) * 60 + (Number(durationSeconds) || 0);
  const totalMinutes = totalSeconds / 60.0;
  const calculatedPace = (distanceKm > 0) ? (totalMinutes / distanceKm).toFixed(2) : '0.00';

  const handleLogRun = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      await api.post('/running/logs', {
        runDate: new Date().toISOString().split('T')[0],
        distanceKm: Number(distanceKm),
        durationSeconds: totalSeconds,
        targetGoal,
        notes,
      });
      await fetchRunningData();
    } catch (err) {
      console.error('Failed to log run', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-black text-white">Running Tracker</h1>
        <p className="text-sm text-slate-400 mt-1">
          Record outdoor runs and interval sprints. Track average pace, distance milestones (2K, 5K, 10K), and calories burnt.
        </p>
      </div>

      {/* Top 3 Running Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-400 uppercase tracking-wider">
            <span>Weekly Mileage</span>
            <div className="w-8 h-8 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center">
              <Footprints className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-3xl font-black text-white font-mono">
            {stats?.weeklyDistanceKm || 0} <span className="text-sm font-normal text-slate-400 font-sans">km</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Last 7 days volume</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-400 uppercase tracking-wider">
            <span>Total Runs Logged</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
              <Zap className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-3xl font-black text-white font-mono">
            {stats?.totalRuns || 0} <span className="text-sm font-normal text-slate-400 font-sans">sessions</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Lifetime distance: {stats?.totalDistanceKm || 0} km
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-400 uppercase tracking-wider">
            <span>Fastest Pace Record</span>
            <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center">
              <Trophy className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-3xl font-black text-white font-mono">
            {stats?.fastestPaceMinPerKm > 0 ? stats.fastestPaceMinPerKm : '--'} <span className="text-sm font-normal text-slate-400 font-sans">min/km</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Personal Best Pace</p>
        </div>

      </div>

      {/* Main Grid: Run Logging Form on Left, History on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        
        {/* Left: Log Run Form (5 cols) */}
        <div className="lg:col-span-5">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-5">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Footprints className="w-4 h-4 text-cyan-400" /> Log Running Session
            </h2>

            <form onSubmit={handleLogRun} className="space-y-4">
              
              <div>
                <label className="text-xs font-medium text-slate-400 block mb-1">Target Milestone / Goal</label>
                <div className="grid grid-cols-4 gap-2">
                  {['2K', '5K', '10K', 'Intervals'].map((goal) => (
                    <button
                      type="button"
                      key={goal}
                      onClick={() => setTargetGoal(goal)}
                      className={`py-2 rounded-xl text-xs font-bold transition-colors ${
                        targetGoal === goal
                          ? 'bg-cyan-600 text-white shadow-md'
                          : 'bg-slate-950 text-slate-400 border border-slate-800 hover:text-white'
                      }`}
                    >
                      {goal}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-xs font-medium text-slate-400 block mb-1">Distance (Kilometers)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.1"
                  required
                  value={distanceKm}
                  onChange={(e) => setDistanceKm(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs font-medium text-slate-400 block mb-1">Minutes</label>
                  <input
                    type="number"
                    min="0"
                    required
                    value={durationMinutes}
                    onChange={(e) => setDurationMinutes(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:outline-none focus:border-cyan-500"
                  />
                </div>
                <div>
                  <label className="text-xs font-medium text-slate-400 block mb-1">Seconds</label>
                  <input
                    type="number"
                    min="0"
                    max="59"
                    required
                    value={durationSeconds}
                    onChange={(e) => setDurationSeconds(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              {/* Calculated Pace Preview Badge */}
              <div className="bg-slate-950/80 p-4 rounded-2xl border border-slate-800 flex items-center justify-between">
                <div>
                  <span className="text-[11px] text-slate-400 block">Computed Pace</span>
                  <span className="text-xl font-black font-mono text-cyan-400">{calculatedPace} min/km</span>
                </div>
                <div className="text-right">
                  <span className="text-[11px] text-slate-400 block">Est. Calorie Burn</span>
                  <span className="text-xl font-black font-mono text-rose-400">
                    {Math.round(distanceKm * 75 * 1.036)} kcal
                  </span>
                </div>
              </div>

              <div>
                <label className="text-xs font-medium text-slate-400 block mb-1">Run Notes / Terrain</label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="e.g. Outdoor trail, tempo interval"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-cyan-500"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white font-bold text-sm shadow-lg shadow-cyan-600/20 transition-all flex items-center justify-center gap-2"
              >
                <Plus className="w-4 h-4" /> Save Run Log
              </button>
            </form>
          </div>
        </div>

        {/* Right: Running History (7 cols) */}
        <div className="lg:col-span-7">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Calendar className="w-4 h-4 text-cyan-400" /> Recent Running History
            </h2>

            {runs.length === 0 ? (
              <p className="text-xs text-slate-500 italic py-6 text-center">No runs recorded yet. Log your first run on the left!</p>
            ) : (
              <div className="space-y-3">
                {runs.map((r) => (
                  <div
                    key={r.id}
                    className="bg-slate-950/70 border border-slate-800 p-4 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-slate-700 transition-colors"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-white">{r.distanceKm} km Run</span>
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                          {r.targetGoal}
                        </span>
                      </div>
                      <div className="text-xs text-slate-400 mt-1">
                        {r.runDate} {r.notes && `• ${r.notes}`}
                      </div>
                    </div>

                    <div className="flex items-center gap-6 font-mono text-xs">
                      <div>
                        <span className="text-[10px] text-slate-500 block uppercase">Pace</span>
                        <span className="text-cyan-300 font-bold">{r.avgPaceMinPerKm} min/km</span>
                      </div>
                      <div>
                        <span className="text-[10px] text-slate-500 block uppercase">Time</span>
                        <span className="text-slate-200 font-medium">
                          {Math.floor(r.durationSeconds / 60)}m {r.durationSeconds % 60}s
                        </span>
                      </div>
                      <div>
                        <span className="text-[10px] text-slate-500 block uppercase">Burn</span>
                        <span className="text-rose-400 font-bold">{Math.round(r.estimatedCaloriesBurnt || 0)} kcal</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </div>

    </div>
  );
}
