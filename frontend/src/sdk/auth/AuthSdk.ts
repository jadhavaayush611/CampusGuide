import { BaseSdk } from '../common/BaseSdk';
import {
  LoginRequestDto,
  RegisterRequestDto,
  AuthResponseDto,
  UserDto,
  UpdateProfileDto,
  ChangePasswordDto,
} from './auth.dto';
import { mapUserDtoToModel, mapAuthResponseToSession } from './auth.mapper';
import { User, AuthSession, LoginCredentials, RegisterPayload, PasswordChangePayload } from '../../models/auth.model';

/**
 * Production Authentication SDK module encapsulating all auth endpoints.
 */
export class AuthSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/auth';
  private readonly usersUrl = '/api/v1/users';

  /**
   * Authenticate user with credentials
   */
  public async login(credentials: LoginCredentials): Promise<AuthSession> {
    const payload = {
      email: credentials.email,
      emailOrUsername: credentials.email,
      password: credentials.password,
    };
    const dto = await this.post<AuthResponseDto>(`${this.baseUrl}/login`, payload, { skipAuth: true });
    return mapAuthResponseToSession(dto);
  }

  /**
   * Register a new user account
   */
  public async register(payload: RegisterPayload): Promise<AuthSession> {
    const reqDto: RegisterRequestDto = {
      name: payload.name,
      email: payload.email,
      password: payload.password,
      role: payload.role,
      studentId: payload.studentId,
      department: payload.department,
    };
    const dto = await this.post<AuthResponseDto>(`${this.baseUrl}/register`, reqDto, { skipAuth: true });
    return mapAuthResponseToSession(dto);
  }

  /**
   * Retrieve current authenticated user profile
   */
  public async getCurrentUser(): Promise<User> {
    const dto = await this.get<UserDto>(`${this.baseUrl}/me`);
    return mapUserDtoToModel(dto);
  }

  /**
   * Refresh current access token using refresh token
   */
  public async refreshToken(refreshToken: string): Promise<AuthSession> {
    const dto = await this.post<AuthResponseDto>(`${this.baseUrl}/refresh`, { refreshToken }, { skipAuth: true });
    return mapAuthResponseToSession(dto);
  }

  /**
   * Update user profile information
   */
  public async updateProfile(userId: string, payload: UpdateProfileDto): Promise<User> {
    const dto = await this.put<UserDto>(`${this.usersUrl}/${userId}`, payload);
    return mapUserDtoToModel(dto);
  }

  /**
   * Change user password
   */
  public async changePassword(payload: PasswordChangePayload): Promise<void> {
    await this.post<void>(`${this.baseUrl}/change-password`, payload);
  }

  /**
   * Logout user session
   */
  public async logout(): Promise<void> {
    try {
      await this.post<void>(`${this.baseUrl}/logout`);
    } catch {
      // Ignore network errors during logout teardown
    }
  }
}

export const authSdk = new AuthSdk();
