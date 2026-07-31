import { createContext } from 'react';

export interface User {
  id: string;
  email: string;
  name: string;
  role?: string;
  avatarUrl?: string;
  [key: string]: unknown;
}

export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  error: string | null;
}

export interface AuthContextValue extends AuthState {
  login: (tokens: { accessToken: string; refreshToken?: string }, user: User) => void;
  logout: () => Promise<void>;
  restoreSession: () => Promise<void>;
  setUser: (user: User | null) => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
