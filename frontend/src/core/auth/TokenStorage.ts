import { IStorage, LocalStorageAdapter } from '../storage';

export interface TokenPair {
  accessToken: string;
  refreshToken?: string;
  expiresAt?: number; // timestamp in ms
}

export class TokenStorage {
  private storage: IStorage;
  private readonly ACCESS_TOKEN_KEY = 'campusguide_access_token';
  private readonly REFRESH_TOKEN_KEY = 'campusguide_refresh_token';
  private readonly EXPIRES_AT_KEY = 'campusguide_token_expires_at';

  constructor(storage?: IStorage) {
    this.storage = storage ?? new LocalStorageAdapter();
  }

  getAccessToken(): string | null {
    return this.storage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return this.storage.getItem(this.REFRESH_TOKEN_KEY);
  }

  getExpiresAt(): number | null {
    const raw = this.storage.getItem(this.EXPIRES_AT_KEY);
    return raw ? parseInt(raw, 10) : null;
  }

  saveTokens(tokens: TokenPair): void {
    if (tokens.accessToken) {
      this.storage.setItem(this.ACCESS_TOKEN_KEY, tokens.accessToken);
    }
    if (tokens.refreshToken) {
      this.storage.setItem(this.REFRESH_TOKEN_KEY, tokens.refreshToken);
    }
    if (tokens.expiresAt) {
      this.storage.setItem(this.EXPIRES_AT_KEY, String(tokens.expiresAt));
    }
  }

  clearTokens(): void {
    this.storage.removeItem(this.ACCESS_TOKEN_KEY);
    this.storage.removeItem(this.REFRESH_TOKEN_KEY);
    this.storage.removeItem(this.EXPIRES_AT_KEY);
  }

  hasTokens(): boolean {
    return Boolean(this.getAccessToken());
  }
}
