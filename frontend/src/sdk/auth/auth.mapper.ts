import { UserDto, AuthResponseDto } from './auth.dto';
import { User, AuthTokens, AuthSession } from '../../models/auth.model';

export function mapUserDtoToModel(dto: UserDto & { username?: string }): User {
  return {
    id: dto.id || '',
    email: dto.email || '',
    name: dto.name || dto.username || (dto.email ? dto.email.split('@')[0] : 'Campus User'),
    role: dto.role || 'STUDENT',
    department: dto.department ?? undefined,
    avatarUrl: dto.avatarUrl ?? undefined,
    studentId: dto.studentId ?? undefined,
    phone: dto.phone ?? undefined,
    bio: dto.bio ?? undefined,
    createdAt: dto.createdAt,
  };
}

export function mapAuthResponseToSession(dto: AuthResponseDto & { token?: string; email?: string; role?: string }): AuthSession {
  const accessToken = dto.accessToken || dto.token || '';
  const tokens: AuthTokens = {
    accessToken,
    refreshToken: dto.refreshToken,
    expiresIn: dto.expiresIn,
    tokenType: dto.tokenType ?? 'Bearer',
  };

  const user: User = dto.user
    ? mapUserDtoToModel(dto.user)
    : {
        id: '',
        email: dto.email || '',
        name: dto.email ? dto.email.split('@')[0] : 'Campus User',
        role: dto.role || 'STUDENT',
      };

  return {
    user,
    tokens,
    isAuthenticated: true,
  };
}
