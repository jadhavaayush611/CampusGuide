import React, { ReactNode } from 'react';
import { ErrorBoundary } from '../errors/ErrorBoundary';
import { ToastProvider } from '../toast/ToastProvider';
import { LoadingProvider } from '../loading/LoadingProvider';
import { QueryClientProvider } from '../query/QueryClientProvider';
import { AuthProvider } from '../auth/AuthProvider';
import { OfflineBanner } from '../../app/components/OfflineBanner';

export interface AppProvidersProps {
  children: ReactNode;
}

/**
 * Root Application Provider Hierarchy.
 * Composes core infrastructure providers in strict dependency order:
 * 1. ErrorBoundary (Catches unhandled rendering errors)
 * 2. ToastProvider (Notifications host)
 * 3. LoadingProvider (Global loading overlay state)
 * 4. QueryClientProvider (Server state management & caching)
 * 5. AuthProvider (Session restoration & token management)
 */
export const AppProviders: React.FC<AppProvidersProps> = ({ children }) => {
  return (
    <ErrorBoundary>
      <ToastProvider>
        <LoadingProvider>
          <QueryClientProvider>
            <AuthProvider>
              {children}
              <OfflineBanner />
            </AuthProvider>
          </QueryClientProvider>
        </LoadingProvider>
      </ToastProvider>
    </ErrorBoundary>
  );
};
