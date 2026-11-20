import React, { useState, useEffect } from 'react';
import api from '../api/client';
import { 
  AreaChart, 
  Area, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  Legend
} from 'recharts';
import { 
  TrendingDown, 
  Dumbbell, 
  Footprints, 
  Calendar,
  Layers,
  Sparkles
} from 'lucide-react';

export default function AnalyticsPage() {
  const [data, setData] = useState(null);
  const [days, setDays] = useState(14);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
  }, [days]);

  const fetchAnalytics = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/analytics/dashboard?days=${days}`);
      setData(res.data);
    } catch (err) {
      console.error('Failed to load analytics', err);
    } finally {
      setLoading(false);
    }
  };

  const trends = data?.trends || [];
  const muscleVolume = data?.muscleVolumeBreakdown || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      {/* Header & Date Range Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white">Fitness Progress & Analytics</h1>
          <p className="text-sm text-slate-400 mt-1">
            Visual analysis of calorie deficit consistency, protein sparing, and muscle volume distribution.
          </p>
        </div>

        <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 p-1 rounded-xl self-start">
          {[7, 14, 30].map((d) => (
            <button
              key={d}
              onClick={() => setDays(d)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                days === d
                  ? 'bg-brand-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              {d} Days
            </button>
          ))}
        </div>
      </div>

      {/* Top 3 Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Average Daily Deficit</div>
          <div className="text-3xl font-black text-emerald-400 mt-2 font-mono">
            {Math.round(data?.averageDeficitKcal || 0)} <span className="text-sm font-normal text-slate-400 font-sans">kcal/day</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Steady fat loss trajectory</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Workouts Completed</div>
          <div className="text-3xl font-black text-purple-400 mt-2 font-mono">
            {data?.totalWorkoutsLogged || 0} <span className="text-sm font-normal text-slate-400 font-sans">sessions</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Resistance sessions in past {days} days</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Running Mileage</div>
          <div className="text-3xl font-black text-cyan-400 mt-2 font-mono">
            {data?.totalKmRan || 0} <span className="text-sm font-normal text-slate-400 font-sans">km</span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Aerobic & interval volume</p>
        </div>

      </div>

      {/* Chart 1: Calorie Intake vs TDEE Deficit Area Chart */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-white">Calorie Deficit Progression (Intake vs TDEE)</h2>
            <p className="text-xs text-slate-400">Green shaded area represents daily calorie deficit for fat burning.</p>
          </div>
        </div>

        <div className="h-72 w-full pt-4">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={trends} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="colorIntake" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.4}/>
                  <stop offset="95%" stopColor="#f59e0b" stopOpacity={0.0}/>
                </linearGradient>
                <linearGradient id="colorTdee" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#22c55e" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#22c55e" stopOpacity={0.0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
              <XAxis dataKey="date" stroke="#64748b" textAnchor="end" tick={{ fontSize: 11 }} />
              <YAxis stroke="#64748b" tick={{ fontSize: 11 }} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }}
                itemStyle={{ fontSize: '12px' }}
              />
              <Legend verticalAlign="top" height={36} wrapperStyle={{ fontSize: '12px' }} />
              <Area type="monotone" dataKey="tdeeKcal" name="TDEE Ceiling" stroke="#22c55e" strokeWidth={2} fillOpacity={1} fill="url(#colorTdee)" />
              <Area type="monotone" dataKey="calorieIntake" name="Calorie Intake" stroke="#f59e0b" strokeWidth={2} fillOpacity={1} fill="url(#colorIntake)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Two-Column Grid: Muscle Volume Breakdown & Daily Protein Chart */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        
        {/* Muscle Volume Breakdown Bar Chart */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Dumbbell className="w-4 h-4 text-purple-400" /> Muscle Group Sets Distribution
            </h3>
            <p className="text-xs text-slate-400">Total working sets logged across muscle groups to ensure balanced development.</p>
          </div>

          <div className="h-64 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={muscleVolume} layout="vertical" margin={{ top: 5, right: 30, left: 40, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" horizontal={false} />
                <XAxis type="number" stroke="#64748b" tick={{ fontSize: 11 }} />
                <YAxis dataKey="muscleGroup" type="category" stroke="#64748b" tick={{ fontSize: 11 }} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }}
                  itemStyle={{ fontSize: '12px' }}
                />
                <Bar dataKey="totalSets" name="Total Working Sets" fill="#a855f7" radius={[0, 6, 6, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Daily Protein Consumption Consistency */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-indigo-400" /> Daily Protein Target Consistency
            </h3>
            <p className="text-xs text-slate-400">Grams of protein consumed daily to prevent muscle wasting during deficit.</p>
          </div>

          <div className="h-64 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={trends} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="date" stroke="#64748b" tick={{ fontSize: 11 }} />
                <YAxis stroke="#64748b" tick={{ fontSize: 11 }} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }}
                  itemStyle={{ fontSize: '12px' }}
                />
                <Bar dataKey="proteinGrams" name="Protein (g)" fill="#6366f1" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>

    </div>
  );
}
