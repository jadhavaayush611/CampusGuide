import React, { ReactNode } from 'react';
import { QueryClientProvider as TanStackQueryClientProvider } from '@tanstack/react-query';
import { queryClient } from './queryClient';

export interface QueryClientProviderProps {
  children: ReactNode;
}

/**
 * Provider wrapping the application in TanStack React Query context.
 */
export const QueryClientProvider: React.FC<QueryClientProviderProps> = ({ children }) => {
  return (
    <TanStackQueryClientProvider client={queryClient}>
      {children}
    </TanStackQueryClientProvider>
  );
};
