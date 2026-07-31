import { AppError, ApiError } from '../../core/errors/AppError';

/**
 * Domain SDK Error Class wrapping low-level network/API exceptions.
 */
export class SdkError extends AppError {
  readonly statusCode: number;
  readonly correlationId?: string;

  constructor(
    message: string,
    statusCode = 500,
    code = 'SDK_ERROR',
    details?: unknown,
    correlationId?: string
  ) {
    super(message, code, details);
    this.statusCode = statusCode;
    this.correlationId = correlationId;
  }

  /**
   * Factory method converting an ApiError into a strongly typed SdkError.
   */
  public static fromApiError(error: unknown, defaultMessage = 'SDK Operation Failed'): SdkError {
    if (error instanceof ApiError) {
      return new SdkError(
        error.message || defaultMessage,
        error.statusCode,
        error.code || 'API_ERROR',
        error.responseData,
        error.correlationId
      );
    }
    if (error instanceof AppError) {
      return new SdkError(error.message, 500, error.code, error.details);
    }
    if (error instanceof Error) {
      return new SdkError(error.message || defaultMessage, 500, 'UNEXPECTED_ERROR');
    }
    return new SdkError(defaultMessage, 500, 'UNKNOWN_ERROR');
  }
}
