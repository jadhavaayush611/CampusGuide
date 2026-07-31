import { TokenStorage, TokenPair } from './TokenStorage';
import { isTokenExpired, getTokenExpirationTime } from '../utils/jwt';
import { logger } from '../utils/logger';

export type TokenChangeListener = (tokens: TokenPair | null) => void;

export class TokenManager {
  private tokenStorage: TokenStorage;
  private listeners: Set<TokenChangeListener> = new Set();

  constructor(tokenStorage?: TokenStorage) {
    this.tokenStorage = tokenStorage ?? new TokenStorage();
  }

  public subscribe(listener: TokenChangeListener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notifyListeners(tokens: TokenPair | null): void {
    this.listeners.forEach((listener) => {
      try {
        listener(tokens);
      } catch (err) {
        logger.error('Error in token change listener:', err);
      }
    });
  }

  public getAccessToken(): string | null {
    return this.tokenStorage.getAccessToken();
  }

  public getRefreshToken(): string | null {
    return this.tokenStorage.getRefreshToken();
  }

  public setTokens(tokens: { accessToken: string; refreshToken?: string; expiresAt?: number }): void {
    let expiresAt = tokens.expiresAt;
    if (!expiresAt && tokens.accessToken) {
      const expMs = getTokenExpirationTime(tokens.accessToken);
      if (expMs) expiresAt = expMs;
    }

    const tokenPair: TokenPair = {
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      expiresAt,
    };

    this.tokenStorage.saveTokens(tokenPair);
    this.notifyListeners(tokenPair);
  }

  public clearTokens(): void {
    this.tokenStorage.clearTokens();
    this.notifyListeners(null);
  }

  public isAccessTokenExpired(bufferSeconds = 10): boolean {
    const token = this.getAccessToken();
    if (!token) return true;

    // Check stored expiresAt timestamp first if available
    const storedExp = this.tokenStorage.getExpiresAt();
    if (storedExp) {
      return storedExp - bufferSeconds * 1000 <= Date.now();
    }

    return isTokenExpired(token, bufferSeconds);
  }

  public hasValidAccessToken(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;
    return !this.isAccessTokenExpired();
  }
}

/** Centralized TokenManager Singleton */
export const tokenManager = new TokenManager();
