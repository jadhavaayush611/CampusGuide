import { AppError, ApiError, NetworkError, TimeoutError, AuthError } from './AppError';
import { logger } from '../utils/logger';

export type ErrorListener = (error: AppError) => void;

/**
 * Centralized Error Handler Service for processing rendering and API failures.
 */
export class ErrorHandler {
  private static listeners: Set<ErrorListener> = new Set();

  /**
   * Subscribe to global handled errors (e.g. for displaying global notifications).
   */
  static subscribe(listener: ErrorListener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  /**
   * Normalizes any thrown exception or rejection into an AppError instance.
   */
  static normalize(error: unknown, fallbackMessage = 'An unexpected error occurred'): AppError {
    if (error instanceof AppError) {
      return error;
    }

    if (error instanceof Error) {
      if (error.name === 'AbortError') {
        return new TimeoutError('Request timed out before completing');
      }
      return new AppError(error.message || fallbackMessage, 'UNKNOWN_ERROR', { originalError: error });
    }

    if (typeof error === 'string') {
      return new AppError(error);
    }

    return new AppError(fallbackMessage, 'UNKNOWN_ERROR', { raw: error });
  }

  /**
   * Centralized method to handle, log, and notify listeners of an error.
   */
  static handle(error: unknown, context?: string): AppError {
    const normalized = this.normalize(error);
    const prefix = context ? `[${context}]` : '';

    logger.error(`${prefix} ${normalized.code}: ${normalized.message}`, {
      timestamp: normalized.timestamp,
      details: normalized.details,
      stack: normalized.stack,
    });

    // Broadcast to listeners
    this.listeners.forEach((listener) => {
      try {
        listener(normalized);
      } catch (err) {
        logger.error('Error listener threw an exception:', err);
      }
    });

    return normalized;
  }

  /**
   * Formats user-friendly message from an AppError or unknown error.
   */
  static getUserMessage(error: unknown): string {
    const normalized = this.normalize(error);

    if (normalized instanceof ApiError) {
      if (normalized.statusCode === 401) return 'Session expired. Please log in again.';
      if (normalized.statusCode === 403) return 'You do not have permission to perform this action.';
      if (normalized.statusCode === 404) return 'The requested resource was not found.';
      if (normalized.statusCode >= 500) return 'A server error occurred. Please try again later.';
    }

    if (normalized instanceof NetworkError) {
      return 'Unable to reach server. Please check your internet connection.';
    }

    if (normalized instanceof TimeoutError) {
      return 'The request took too long to complete. Please try again.';
    }

    if (normalized instanceof AuthError) {
      return normalized.message;
    }

    return normalized.message || 'An unexpected error occurred.';
  }
}
