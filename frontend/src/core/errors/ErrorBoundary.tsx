import React, { Component, ErrorInfo, ReactNode } from 'react';
import { ErrorHandler } from './ErrorHandler';
import { AppError } from './AppError';

export interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode | ((error: AppError, reset: () => void) => ReactNode);
  onError?: (error: AppError, errorInfo: ErrorInfo) => void;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: AppError | null;
}

/**
 * React Error Boundary for capturing component tree rendering errors.
 */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  public override state: ErrorBoundaryState = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: unknown): ErrorBoundaryState {
    const normalized = ErrorHandler.normalize(error, 'Rendering failure');
    return {
      hasError: true,
      error: normalized,
    };
  }

  public override componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    const normalized = ErrorHandler.handle(error, 'React.ErrorBoundary');
    if (this.props.onError) {
      this.props.onError(normalized, errorInfo);
    }
  }

  private handleReset = (): void => {
    this.setState({ hasError: false, error: null });
  };

  public override render(): ReactNode {
    if (this.state.hasError && this.state.error) {
      if (typeof this.props.fallback === 'function') {
        return this.props.fallback(this.state.error, this.handleReset);
      }

      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default modern fallback UI
      return (
        <div style={{
          minHeight: '200px',
          padding: '2rem',
          margin: '1rem',
          borderRadius: '0.75rem',
          backgroundColor: '#fef2f2',
          border: '1px solid #fecaca',
          color: '#991b1b',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          textAlign: 'center',
          fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '0.5rem' }}>
            Something went wrong
          </h2>
          <p style={{ fontSize: '0.875rem', marginBottom: '1.25rem', color: '#7f1d1d' }}>
            {ErrorHandler.getUserMessage(this.state.error)}
          </p>
          <button
            onClick={this.handleReset}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#dc2626',
              color: '#ffffff',
              border: 'none',
              borderRadius: '0.375rem',
              fontWeight: 500,
              cursor: 'pointer',
              fontSize: '0.875rem'
            }}
          >
            Try Again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
