import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import { ErrorHandler } from '../../../core/errors/ErrorHandler';

interface Props {
  children: ReactNode;
  fallbackTitle?: string;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class PlannerErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('PlannerErrorBoundary caught an error:', error, errorInfo);
  }

  private handleReset = () => {
    this.setState({ hasError: false, error: null });
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="p-6 bg-destructive/5 dark:bg-destructive/10 border border-destructive/20 rounded-2xl text-center space-y-4 my-4 shadow-xs">
          <div className="w-12 h-12 bg-destructive/10 text-destructive rounded-full flex items-center justify-center mx-auto">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-foreground">
            {this.props.fallbackTitle || 'Failed to load Planner Section'}
          </h3>
          <p className="text-xs text-muted-foreground max-w-md mx-auto leading-relaxed">
            {ErrorHandler.getUserMessage(this.state.error) || 'An unexpected rendering error occurred while loading this section.'}
          </p>
          <button
            onClick={this.handleReset}
            className="inline-flex items-center gap-2 px-4 py-2 bg-destructive hover:bg-destructive/90 text-white rounded-lg text-xs font-semibold transition-all active:scale-[0.98] cursor-pointer shadow-xs"
          >
            <RefreshCw className="w-4 h-4" />
            Retry Section
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
