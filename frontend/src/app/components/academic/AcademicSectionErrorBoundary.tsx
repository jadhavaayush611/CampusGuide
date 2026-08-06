import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { ErrorHandler } from '../../../core/errors/ErrorHandler';

interface Props {
  title?: string;
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class AcademicSectionErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('[AcademicSectionErrorBoundary]', error, errorInfo);
  }

  private handleRetry = () => {
    this.setState({ hasError: false, error: undefined });
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="bg-destructive/5 dark:bg-destructive/10 border border-destructive/20 rounded-2xl p-6 text-center shadow-xs my-4">
          <div className="w-12 h-12 rounded-full bg-destructive/10 text-destructive flex items-center justify-center mx-auto mb-3">
            <AlertCircle className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-foreground mb-1">
            Failed to load {this.props.title || 'academic section'}
          </h3>
          <p className="text-xs text-muted-foreground mb-4 max-w-md mx-auto leading-relaxed">
            {ErrorHandler.getUserMessage(this.state.error) || 'An unexpected error occurred while fetching section data.'}
          </p>
          <button
            onClick={this.handleRetry}
            className="inline-flex items-center gap-2 px-4 py-2 bg-destructive hover:bg-destructive/90 text-white font-semibold text-xs rounded-lg shadow-xs transition-all active:scale-[0.98] cursor-pointer"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Retry Loading</span>
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
