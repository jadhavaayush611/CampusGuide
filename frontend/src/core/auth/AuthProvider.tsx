import React, { useState, useEffect, useCallback, ReactNode } from 'react';
import { AuthContext, AuthState, User, AuthContextValue } from './AuthContext';
import { tokenManager, TokenManager } from './TokenManager';
import { apiClient } from '../api/ApiClient';
import { HTTP_HEADER } from '../api/ApiConstants';
import { parseJwtPayload } from '../utils/jwt';
import { logger } from '../utils/logger';
import { authSdk } from '../../sdk/auth/AuthSdk';

import { queryClient } from '../query/queryClient';

export interface AuthProviderProps {
  children: ReactNode;
  tokenMgr?: TokenManager;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children, tokenMgr = tokenManager }) => {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: false,
    isLoading: true,
    user: null,
    error: null,
  });

  const logout = useCallback(async (): Promise<void> => {
    logger.info('[AuthProvider] Logging out user');
    tokenMgr.clearTokens();
    queryClient.clear();
    setState({
      isAuthenticated: false,
      isLoading: false,
      user: null,
      error: null,
    });
  }, [tokenMgr]);

  const restoreSession = useCallback(async (): Promise<void> => {
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    try {
      if (tokenMgr.hasValidAccessToken()) {
        try {
          const user = await authSdk.getCurrentUser();
          setState({
            isAuthenticated: true,
            isLoading: false,
            user,
            error: null,
          });
          logger.info('[AuthProvider] Session restored via getCurrentUser():', user.id);
          return;
        } catch (fetchErr) {
          logger.warn('[AuthProvider] Could not fetch current user from backend, checking JWT token fallback:', fetchErr);
          const token = tokenMgr.getAccessToken();
          if (token) {
            const payload = parseJwtPayload(token);
            const fallbackUser: User = {
              id: (payload?.sub as string) || (payload?.userId as string) || 'user-session',
              email: (payload?.email as string) || 'user@campusguide.edu',
              name: (payload?.name as string) || (payload?.email ? (payload.email as string).split('@')[0] : 'Campus User'),
              role: (payload?.role as string) || (Array.isArray(payload?.roles) ? payload?.roles[0] : 'STUDENT'),
            };

            setState({
              isAuthenticated: true,
              isLoading: false,
              user: fallbackUser,
              error: null,
            });
            return;
          }
        }
      }

      // Check if refresh token is available for silent refresh on startup
      const refreshToken = tokenMgr.getRefreshToken();
      if (refreshToken) {
        try {
          logger.info('[AuthProvider] Access token expired. Attempting startup token refresh...');
          await tokenMgr.refreshTokens();
          const user = await authSdk.getCurrentUser();
          setState({
            isAuthenticated: true,
            isLoading: false,
            user,
            error: null,
          });
          logger.info('[AuthProvider] Session restored via silent token refresh');
          return;
        } catch (refreshErr) {
          logger.warn('[AuthProvider] Startup silent token refresh failed:', refreshErr);
        }
      }

      // If token expired, missing, or refresh failed
      tokenMgr.clearTokens();
      setState({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
      });
    } catch (err: any) {
      logger.error('[AuthProvider] Error restoring session:', err);
      tokenMgr.clearTokens();
      setState({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: err?.message || 'Failed to restore session',
      });
    }
  }, [tokenMgr]);

  const login = useCallback(
    (tokens: { accessToken: string; refreshToken?: string }, user: User): void => {
      tokenMgr.setTokens(tokens);
      setState({
        isAuthenticated: true,
        isLoading: false,
        user,
        error: null,
      });
      logger.info('[AuthProvider] User authenticated successfully:', user.id || user.email);
    },
    [tokenMgr]
  );

  const setUser = useCallback((user: User | null): void => {
    setState((prev) => ({ ...prev, user }));
  }, []);

  // Setup API Client Interceptors for Authentication
  useEffect(() => {
    // Request Interceptor: Attach JWT Bearer Token
    const unbindRequest = apiClient.addRequestInterceptor((reqConfig) => {
      if (reqConfig.skipAuth) return reqConfig;

      const token = tokenMgr.getAccessToken();
      if (token) {
        const headers = {
          ...reqConfig.headers,
          [HTTP_HEADER.AUTHORIZATION]: `Bearer ${token}`,
        };
        return { ...reqConfig, headers };
      }
      return reqConfig;
    });

    // Error Interceptor: Handle 401 Unauthorized (Expired Tokens)
    const unbindError = apiClient.addErrorInterceptor(async (apiError) => {
      if (apiError.statusCode === 401) {
        logger.warn('[AuthProvider] Received 401 Unauthorized.');
        const refreshToken = tokenMgr.getRefreshToken();
        if (refreshToken) {
          try {
            logger.info('[AuthProvider] Attempting silent token refresh on 401...');
            await tokenMgr.refreshTokens();
            return;
          } catch (refreshErr) {
            logger.error('[AuthProvider] Silent token refresh failed on 401. Performing forced logout:', refreshErr);
          }
        }
        await logout();
      }
    });

    return () => {
      unbindRequest();
      unbindError();
    };
  }, [logout, tokenMgr]);

  // Restore Session on Mount
  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  const contextValue: AuthContextValue = {
    ...state,
    login,
    logout,
    restoreSession,
    setUser,
  };

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
};
