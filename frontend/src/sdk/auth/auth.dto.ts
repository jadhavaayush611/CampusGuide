/**
 * Authentication Backend DTO Schemas
 */

export interface LoginRequestDto {
  email: string;
  password: string;
}

export interface RegisterRequestDto {
  name: string;
  email: string;
  password: string;
  role?: string;
  studentId?: string;
  department?: string;
}

export interface RefreshTokenRequestDto {
  refreshToken: string;
}

export interface UserDto {
  id: string;
  email: string;
  name: string;
  role: string;
  department?: string | null;
  avatarUrl?: string | null;
  studentId?: string | null;
  phone?: string | null;
  bio?: string | null;
  createdAt?: string;
}

export interface AuthResponseDto {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user?: UserDto;
}

export interface UpdateProfileDto {
  name?: string;
  phone?: string;
  bio?: string;
  avatarUrl?: string;
  department?: string;
}

export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}
