/**
 * Core Custom Error Hierarchy for CampusGuide
 */

export class AppError extends Error {
  readonly code: string;
  readonly timestamp: string;
  readonly details?: unknown;

  constructor(message: string, code = 'APP_ERROR', details?: unknown) {
    super(message);
    this.name = this.constructor.name;
    this.code = code;
    this.timestamp = new Date().toISOString();
    this.details = details;

    // Restore prototype chain for ES5/ES6 compatibility
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export class ApiError extends AppError {
  readonly statusCode: number;
  readonly responseData?: unknown;
  readonly correlationId?: string;

  constructor(
    message: string,
    statusCode: number,
    responseData?: unknown,
    correlationId?: string,
    code = 'API_ERROR'
  ) {
    super(message, code, responseData);
    this.statusCode = statusCode;
    this.responseData = responseData;
    this.correlationId = correlationId;
  }
}

export class AuthError extends AppError {
  constructor(message = 'Authentication required', code = 'AUTH_ERROR', details?: unknown) {
    super(message, code, details);
  }
}

export class ValidationError extends AppError {
  readonly errors?: Record<string, string[]>;

  constructor(message = 'Validation failed', errors?: Record<string, string[]>, details?: unknown) {
    super(message, 'VALIDATION_ERROR', details);
    this.errors = errors;
  }
}

export class NetworkError extends ApiError {
  constructor(message = 'Network connection failure', details?: unknown) {
    super(message, 0, details, undefined, 'NETWORK_ERROR');
  }
}

export class TimeoutError extends ApiError {
  constructor(message = 'Request timed out', details?: unknown) {
    super(message, 504, details, undefined, 'TIMEOUT_ERROR');
  }
}
