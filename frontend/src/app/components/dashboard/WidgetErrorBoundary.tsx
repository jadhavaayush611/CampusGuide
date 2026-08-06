import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import { ErrorHandler } from '../../../core/errors/ErrorHandler';

interface Props {
  title?: string;
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class WidgetErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Widget error caught by boundary:', error, errorInfo);
  }

  private handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };

  public render() {
    if (this.state.hasError) {
      return (
        <section
          className="bg-red-50/70 border border-red-200 rounded-2xl p-6 shadow-sm flex flex-col items-center text-center justify-center min-h-[180px]"
          role="alert"
          aria-live="assertive"
        >
          <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center mb-3">
            <AlertTriangle className="w-5 h-5 text-red-600" />
          </div>
          <h4 className="text-base font-semibold text-gray-900 mb-1">
            {this.props.title || 'Widget Unavailable'}
          </h4>
          <p className="text-xs text-gray-600 mb-4 max-w-sm">
            {ErrorHandler.getUserMessage(this.state.error) || 'An error occurred while loading this section.'}
          </p>
          <button
            onClick={this.handleRetry}
            type="button"
            className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-red-200 text-red-700 hover:bg-red-50 text-xs font-medium rounded-lg transition-colors shadow-sm"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Try Again</span>
          </button>
        </section>
      );
    }

    return this.props.children;
  }
}
