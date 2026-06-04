"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { api, ApiError } from "@/utils/api";
import { WishlistResponseDTO } from "@/dto/WishlistResponseDTO";
import { CartItem } from "@/dto/CartItem";
import { Product } from "@/dto/Product";

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  role: string;
}

interface AuthContextType {
  user: UserProfile | null;
  token: string | null;
  loading: boolean;
  theme: "light" | "dark";
  toggleTheme: () => void;
  login: (email: string, password: String) => Promise<void>;
  register: (name: string, email: string, password: String) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    if (typeof window !== 'undefined') {
      return (localStorage.getItem('theme') as 'light' | 'dark') || 'light';
    }
    return 'light';
  });

  // Initialize and load user if token is present
  useEffect(() => {
    async function loadUser() {
      const storedToken = localStorage.getItem("token");
      if (storedToken) {
        setToken(storedToken);
        try {
          // Fetch current profile from backend /users/me
          const profile = await api.get<UserProfile>("/users/me");
          setUser(profile);
        } catch (error) {
          console.error("Failed to restore session, logging out:", error);
          // Token is likely invalid or expired, clear it
          localStorage.removeItem("token");
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    }

    loadUser();
  }, []);

  // Login handler
  const login = async (email: string, password: String) => {
    try {
      const response = await api.post<{ message: string; data: string }>("/users/login", {
        email,
        password,
      });

      const jwtToken = response.data;
      if (jwtToken) {
        localStorage.setItem("token", jwtToken);
        setToken(jwtToken);
        
        // Fetch profile
        const profile = await api.get<UserProfile>("/users/me");
        setUser(profile);
      } else {
        throw new Error("No token returned from server");
      }
    } catch (error) {
      throw error;
    }
  };

  // Register handler
  const register = async (name: string, email: string, password: String) => {
    try {
      await api.post<any>("/users/register", {
        name,
        email,
        password,
        role: "CUSTOMER", // default role
      });
      // Optionally auto-login after successful registration
      await login(email, password);
    } catch (error) {
      throw error;
    }
  };

  // Logout handler
  const logout = async () => {
    try {
      // Best-effort call to backend logout to blacklist the token
      await api.post("/users/logout");
    } catch (error) {
      console.warn("Backend logout blacklist call failed:", error);
    } finally {
      // Always clear local session even if server-side blacklist fails
      localStorage.removeItem("token");
      setToken(null);
      setUser(null);
    }
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    if (typeof window !== 'undefined') {
      localStorage.setItem('theme', newTheme);
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, theme, toggleTheme, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
