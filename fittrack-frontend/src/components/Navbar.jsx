import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  UtensilsCrossed, 
  Dumbbell, 
  Footprints, 
  Sparkles, 
  BarChart3, 
  LogOut,
  Flame
} from 'lucide-react';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Nutrition & Food', path: '/nutrition', icon: UtensilsCrossed },
    { name: 'Gym Workouts', path: '/workouts', icon: Dumbbell },
    { name: 'Running', path: '/running', icon: Footprints },
    { name: 'AI Coach', path: '/ai-coach', icon: Sparkles },
    { name: 'Analytics', path: '/analytics', icon: BarChart3 },
  ];

  return (
    <header className="sticky top-0 z-50 bg-slate-900/90 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand Logo */}
        <NavLink to="/" className="flex items-center gap-2.5 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-600 to-emerald-400 flex items-center justify-center text-white shadow-lg shadow-brand-500/20 group-hover:scale-105 transition-transform">
            <Flame className="w-6 h-6 fill-white" />
          </div>
          <div>
            <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
              FitTrack
            </span>
            <span className="hidden sm:inline-block ml-2 px-1.5 py-0.5 text-[10px] font-semibold bg-brand-500/10 text-brand-400 border border-brand-500/20 rounded">
              AI ENGINE
            </span>
          </div>
        </NavLink>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center gap-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-slate-800 text-brand-400 shadow-sm border border-slate-700/60'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                {item.name}
              </NavLink>
            );
          })}
        </nav>

        {/* User Badge & Logout */}
        <div className="flex items-center gap-3">
          {user ? (
            <div className="flex items-center gap-3">
              <div className="hidden sm:block text-right">
                <div className="text-xs font-semibold text-slate-200">{user.fullName}</div>
                <div className="text-[11px] text-slate-400">
                  Deficit: <span className="text-brand-400 font-medium">500 kcal</span>
                </div>
              </div>
              <button
                onClick={handleLogout}
                title="Log out"
                className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 border border-transparent hover:border-rose-500/20 transition-colors"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <NavLink
              to="/login"
              className="text-sm font-medium px-4 py-2 rounded-lg bg-brand-600 hover:bg-brand-500 text-white transition-colors"
            >
              Sign In
            </NavLink>
          )}
        </div>
      </div>

      {/* Mobile Navigation Bar */}
      <div className="md:hidden border-t border-slate-800/80 bg-slate-900/95 flex overflow-x-auto py-2 px-3 gap-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex flex-col items-center gap-1 min-w-[70px] py-1.5 px-2 rounded-lg text-[11px] font-medium whitespace-nowrap ${
                  isActive
                    ? 'text-brand-400 bg-slate-800'
                    : 'text-slate-400 hover:text-slate-200'
                }`
              }
            >
              <Icon className="w-4 h-4" />
              {item.name.split(' ')[0]}
            </NavLink>
          );
        })}
      </div>
    </header>
  );
}
