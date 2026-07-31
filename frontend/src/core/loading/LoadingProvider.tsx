import React, { useState, useCallback, ReactNode } from 'react';
import { LoadingContext, LoadingContextValue } from './LoadingContext';
import { LoadingOverlay } from './LoadingOverlay';

export interface LoadingProviderProps {
  children: ReactNode;
}

export const LoadingProvider: React.FC<LoadingProviderProps> = ({ children }) => {
  const [loadingKeys, setLoadingKeys] = useState<Set<string>>(new Set());

  const startLoading = useCallback((key = 'global') => {
    setLoadingKeys((prev) => {
      const next = new Set(prev);
      next.add(key);
      return next;
    });
  }, []);

  const stopLoading = useCallback((key = 'global') => {
    setLoadingKeys((prev) => {
      const next = new Set(prev);
      next.delete(key);
      return next;
    });
  }, []);

  const isLoading = useCallback(
    (key = 'global') => {
      return loadingKeys.has(key);
    },
    [loadingKeys]
  );

  const clearAllLoading = useCallback(() => {
    setLoadingKeys(new Set());
  }, []);

  const isGlobalLoading = loadingKeys.has('global');

  const value: LoadingContextValue = {
    isGlobalLoading,
    startLoading,
    stopLoading,
    isLoading,
    clearAllLoading,
  };

  return (
    <LoadingContext.Provider value={value}>
      {children}
      {isGlobalLoading && <LoadingOverlay isGlobal message="Loading..." />}
    </LoadingContext.Provider>
  );
};
