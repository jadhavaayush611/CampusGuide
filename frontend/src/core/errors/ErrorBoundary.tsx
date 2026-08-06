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
        <div
          className="min-h-[200px] p-8 m-4 rounded-xl bg-destructive/5 dark:bg-destructive/10 border border-destructive/20 text-destructive flex flex-col items-center justify-center text-center font-sans"
          role="alert"
          aria-live="assertive"
        >
          <h2 className="text-lg font-bold mb-2">
            Something went wrong
          </h2>
          <p className="text-sm mb-5 text-destructive/80 max-w-md leading-relaxed">
            {ErrorHandler.getUserMessage(this.state.error)}
          </p>
          <button
            onClick={this.handleReset}
            type="button"
            className="px-4 py-2 bg-destructive text-white hover:bg-destructive/90 rounded-lg text-sm font-semibold transition-all active:scale-[0.98] cursor-pointer"
          >
            Try Again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
