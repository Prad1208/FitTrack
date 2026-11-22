import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach Bearer token and optional Gemini API Key to outgoing requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('fittrack_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const geminiKey = localStorage.getItem('fittrack_gemini_key');
  if (geminiKey && geminiKey.trim()) {
    config.headers['X-Gemini-Api-Key'] = geminiKey.trim();
  }
  return config;
}, (error) => Promise.reject(error));

// Global 401 redirect
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('fittrack_token');
      localStorage.removeItem('fittrack_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
