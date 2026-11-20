import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/client';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('fittrack_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('fittrack_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.get('/auth/me')
        .then((res) => {
          setUser(res.data);
          localStorage.setItem('fittrack_user', JSON.stringify(res.data));
        })
        .catch(() => {
          logout();
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('fittrack_token', jwtToken);
    localStorage.setItem('fittrack_user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return res.data;
  };

  const register = async (formData) => {
    const res = await api.post('/auth/register', formData);
    const { token: jwtToken, ...userData } = res.data;
    localStorage.setItem('fittrack_token', jwtToken);
    localStorage.setItem('fittrack_user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return res.data;
  };

  const logout = () => {
    localStorage.removeItem('fittrack_token');
    localStorage.removeItem('fittrack_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
