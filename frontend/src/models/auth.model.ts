/**
 * Frontend UI Domain Models for Authentication & Users
 */

export interface User {
  id: string;
  email: string;
  name: string;
  role: string;
  department?: string;
  avatarUrl?: string;
  studentId?: string;
  phone?: string;
  bio?: string;
  createdAt?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
  expiresIn?: number;
  tokenType?: string;
}

export interface AuthSession {
  user: User;
  tokens: AuthTokens;
  isAuthenticated: boolean;
}

export interface UserProfile extends User {
  enrolledCoursesCount?: number;
  completedCredits?: number;
  joinedCommunitiesCount?: number;
}

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
  role?: string;
  studentId?: string;
  department?: string;
}

export interface PasswordChangePayload {
  currentPassword: string;
  newPassword: string;
}
