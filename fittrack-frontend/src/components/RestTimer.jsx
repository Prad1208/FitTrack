import React, { useState, useEffect } from 'react';
import { Timer, Play, Pause, RotateCcw } from 'lucide-react';

export default function RestTimer() {
  const [secondsLeft, setSecondsLeft] = useState(90);
  const [isRunning, setIsRunning] = useState(false);
  const [selectedDuration, setSelectedDuration] = useState(90);

  useEffect(() => {
    let interval = null;
    if (isRunning && secondsLeft > 0) {
      interval = setInterval(() => {
        setSecondsLeft((prev) => prev - 1);
      }, 1000);
    } else if (secondsLeft === 0) {
      setIsRunning(false);
    }
    return () => clearInterval(interval);
  }, [isRunning, secondsLeft]);

  const startPreset = (duration) => {
    setSelectedDuration(duration);
    setSecondsLeft(duration);
    setIsRunning(true);
  };

  const reset = () => {
    setIsRunning(false);
    setSecondsLeft(selectedDuration);
  };

  const formatTime = (totalSeconds) => {
    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-lg flex flex-col sm:flex-row items-center justify-between gap-4">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
          <Timer className="w-5 h-5" />
        </div>
        <div>
          <div className="text-xs uppercase tracking-wider font-semibold text-slate-400">Rest Timer</div>
          <div className="text-2xl font-black text-white font-mono tracking-tight">
            {formatTime(secondsLeft)}
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2 flex-wrap">
        {[60, 90, 120, 180].map((dur) => (
          <button
            key={dur}
            onClick={() => startPreset(dur)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
              selectedDuration === dur && isRunning
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700/60'
            }`}
          >
            {dur}s
          </button>
        ))}
      </div>

      <div className="flex items-center gap-2">
        <button
          onClick={() => setIsRunning(!isRunning)}
          className={`p-2 rounded-xl text-white transition-colors ${
            isRunning ? 'bg-amber-600 hover:bg-amber-500' : 'bg-brand-600 hover:bg-brand-500'
          }`}
        >
          {isRunning ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
        </button>
        <button
          onClick={reset}
          className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors border border-slate-700"
        >
          <RotateCcw className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
