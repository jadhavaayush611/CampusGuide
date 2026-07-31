import { createContext } from 'react';

export interface LoadingContextValue {
  isGlobalLoading: boolean;
  startLoading: (key?: string) => void;
  stopLoading: (key?: string) => void;
  isLoading: (key?: string) => boolean;
  clearAllLoading: () => void;
}

export const LoadingContext = createContext<LoadingContextValue | null>(null);
