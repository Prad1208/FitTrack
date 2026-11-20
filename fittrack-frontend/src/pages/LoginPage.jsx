import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Flame, Lock, Mail, User, Activity, Sparkles, ArrowRight } from 'lucide-react';

export default function LoginPage() {
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [age, setAge] = useState(26);
  const [gender, setGender] = useState('MALE');
  const [heightCm, setHeightCm] = useState(178);
  const [currentWeightKg, setCurrentWeightKg] = useState(75);
  const [targetWeightKg, setTargetWeightKg] = useState(70);
  const [activityLevel, setActivityLevel] = useState('MODERATE');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        await register({
          email,
          password,
          fullName,
          age: Number(age),
          gender,
          heightCm: Number(heightCm),
          currentWeightKg: Number(currentWeightKg),
          targetWeightKg: Number(targetWeightKg),
          activityLevel,
        });
      } else {
        await login(email, password);
      }
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = async () => {
    setEmail('demo@fittrack.com');
    setPassword('password123');
    setLoading(true);
    try {
      await login('demo@fittrack.com', 'password123');
      navigate('/');
    } catch (err) {
      setError('Demo login failed. Make sure the backend seed is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center px-4 py-12">
      <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl space-y-6">
        
        {/* Logo & Headline */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-brand-600 to-emerald-400 flex items-center justify-center text-white mx-auto shadow-lg shadow-brand-500/20">
            <Flame className="w-7 h-7 fill-white" />
          </div>
          <h1 className="text-2xl font-black text-white">
            {isRegister ? 'Join FitTrack' : 'Welcome to FitTrack'}
          </h1>
          <p className="text-xs text-slate-400">
            {isRegister
              ? 'Calculate your BMR, TDEE & macro targets with clinical precision'
              : 'Log in to track your calorie deficit, workouts, and AI plans'}
          </p>
        </div>

        {/* Demo User Quick Button */}
        {!isRegister && (
          <button
            type="button"
            onClick={handleDemoLogin}
            disabled={loading}
            className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-brand-600/20 to-emerald-500/20 border border-brand-500/30 hover:border-brand-500 text-brand-300 text-xs font-bold transition-all flex items-center justify-center gap-2 group"
          >
            <Sparkles className="w-4 h-4 text-brand-400 group-hover:scale-110 transition-transform" />
            Instant One-Click Demo Sign In (Alex Morgan)
          </button>
        )}

        {/* Error Alert */}
        {error && (
          <div className="bg-rose-500/10 border border-rose-500/20 text-rose-400 px-4 py-2.5 rounded-xl text-xs font-medium">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {isRegister && (
            <div>
              <label className="text-xs text-slate-400 block mb-1">Full Name</label>
              <div className="relative">
                <User className="w-4 h-4 absolute left-3 top-3 text-slate-500" />
                <input
                  type="text"
                  required
                  placeholder="e.g. Alex Morgan"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
                />
              </div>
            </div>
          )}

          <div>
            <label className="text-xs text-slate-400 block mb-1">Email Address</label>
            <div className="relative">
              <Mail className="w-4 h-4 absolute left-3 top-3 text-slate-500" />
              <input
                type="email"
                required
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          <div>
            <label className="text-xs text-slate-400 block mb-1">Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 absolute left-3 top-3 text-slate-500" />
              <input
                type="password"
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-sm text-white focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          {/* Registration Physical Profile Fields */}
          {isRegister && (
            <div className="space-y-3 pt-2 border-t border-slate-800">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-xs text-slate-400 block mb-1">Age</label>
                  <input
                    type="number"
                    required
                    value={age}
                    onChange={(e) => setAge(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white font-mono"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-400 block mb-1">Gender</label>
                  <select
                    value={gender}
                    onChange={(e) => setGender(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white"
                  >
                    <option value="MALE">Male (+5 BMR)</option>
                    <option value="FEMALE">Female (-161 BMR)</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="text-[11px] text-slate-400 block mb-1">Height (cm)</label>
                  <input
                    type="number"
                    step="0.5"
                    required
                    value={heightCm}
                    onChange={(e) => setHeightCm(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-2 py-2 text-xs text-white font-mono"
                  />
                </div>
                <div>
                  <label className="text-[11px] text-slate-400 block mb-1">Weight (kg)</label>
                  <input
                    type="number"
                    step="0.5"
                    required
                    value={currentWeightKg}
                    onChange={(e) => setCurrentWeightKg(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-2 py-2 text-xs text-white font-mono"
                  />
                </div>
                <div>
                  <label className="text-[11px] text-slate-400 block mb-1">Target (kg)</label>
                  <input
                    type="number"
                    step="0.5"
                    required
                    value={targetWeightKg}
                    onChange={(e) => setTargetWeightKg(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-2 py-2 text-xs text-white font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="text-xs text-slate-400 block mb-1">Baseline Activity Level</label>
                <select
                  value={activityLevel}
                  onChange={(e) => setActivityLevel(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white"
                >
                  <option value="SEDENTARY">Sedentary (1.2× BMR - Desk Job)</option>
                  <option value="LIGHT">Light (1.375× BMR - 1-3 days)</option>
                  <option value="MODERATE">Moderate (1.55× BMR - 3-5 days)</option>
                  <option value="VERY_ACTIVE">Very Active (1.725× BMR - 6-7 days)</option>
                </select>
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-xl bg-brand-600 hover:bg-brand-500 text-white font-bold text-sm shadow-lg shadow-brand-500/20 transition-all flex items-center justify-center gap-2"
          >
            {loading ? 'Please wait...' : isRegister ? 'Compute BMR & Create Account' : 'Sign In'}
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="text-center pt-2">
          <button
            type="button"
            onClick={() => {
              setIsRegister(!isRegister);
              setError('');
            }}
            className="text-xs font-medium text-slate-400 hover:text-brand-400 transition-colors"
          >
            {isRegister
              ? 'Already have an account? Sign in here'
              : "Don't have an account? Register with BMR & TDEE calculation"}
          </button>
        </div>

      </div>
    </div>
  );
}
