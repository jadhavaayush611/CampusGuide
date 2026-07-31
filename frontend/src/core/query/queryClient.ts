import { QueryClient, QueryClientConfig } from '@tanstack/react-query';
import { ApiError } from '../errors/AppError';
import { logger } from '../utils/logger';

/**
 * Standard Retry Policy:
 * - Retry up to 3 times for transient server errors (5xx) or network timeouts.
 * - DO NOT retry for client errors (4xx: 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found).
 */
export const queryRetryPolicy = (failureCount: number, error: unknown): boolean => {
  if (failureCount >= 3) {
    return false;
  }

  if (error instanceof ApiError) {
    const status = error.statusCode;
    // Don't retry client errors (400-499)
    if (status >= 400 && status < 500) {
      return false;
    }
  }

  return true;
};

/**
 * Global TanStack React Query Configuration Defaults
 */
export const queryClientConfig: QueryClientConfig = {
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      retry: queryRetryPolicy,
      refetchOnWindowFocus: false,
      refetchOnReconnect: true,
      refetchOnMount: true,
    },
    mutations: {
      retry: false,
      onError: (error: unknown) => {
        logger.error('[QueryClient Mutation Error]:', error);
      },
    },
  },
};

/**
 * Global QueryClient Singleton Instance
 */
export const queryClient = new QueryClient(queryClientConfig);
