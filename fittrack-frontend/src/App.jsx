import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import ChatbotWidget from './components/ChatbotWidget';
import DashboardPage from './pages/DashboardPage';
import NutritionPage from './pages/NutritionPage';
import WorkoutPage from './pages/WorkoutPage';
import RunningPage from './pages/RunningPage';
import AiCoachPage from './pages/AiCoachPage';
import AnalyticsPage from './pages/AnalyticsPage';
import LoginPage from './pages/LoginPage';

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-950">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-brand-500"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function MainLayout() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans relative">
      <Navbar />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="/nutrition" element={<ProtectedRoute><NutritionPage /></ProtectedRoute>} />
          <Route path="/workouts" element={<ProtectedRoute><WorkoutPage /></ProtectedRoute>} />
          <Route path="/running" element={<ProtectedRoute><RunningPage /></ProtectedRoute>} />
          <Route path="/ai-coach" element={<ProtectedRoute><AiCoachPage /></ProtectedRoute>} />
          <Route path="/analytics" element={<ProtectedRoute><AnalyticsPage /></ProtectedRoute>} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      {/* Floating Global AI Chatbot for Authenticated Users */}
      {user && <ChatbotWidget />}

      <footer className="border-t border-slate-900 bg-slate-950/80 py-6 text-center text-xs text-slate-500">
        <p>FitTrack • Precision Calorie Deficit Engine &amp; AI Fitness Suite</p>
      </footer>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainLayout />
    </AuthProvider>
  );
}
