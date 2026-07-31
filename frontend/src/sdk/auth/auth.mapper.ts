import { UserDto, AuthResponseDto } from './auth.dto';
import { User, AuthTokens, AuthSession } from '../../models/auth.model';

export function mapUserDtoToModel(dto: UserDto): User {
  return {
    id: dto.id,
    email: dto.email,
    name: dto.name,
    role: dto.role,
    department: dto.department ?? undefined,
    avatarUrl: dto.avatarUrl ?? undefined,
    studentId: dto.studentId ?? undefined,
    phone: dto.phone ?? undefined,
    bio: dto.bio ?? undefined,
    createdAt: dto.createdAt,
  };
}

export function mapAuthResponseToSession(dto: AuthResponseDto): AuthSession {
  const tokens: AuthTokens = {
    accessToken: dto.accessToken,
    refreshToken: dto.refreshToken,
    expiresIn: dto.expiresIn,
    tokenType: dto.tokenType ?? 'Bearer',
  };

  const user: User = dto.user
    ? mapUserDtoToModel(dto.user)
    : {
        id: '',
        email: '',
        name: '',
        role: 'USER',
      };

  return {
    user,
    tokens,
    isAuthenticated: true,
  };
}
