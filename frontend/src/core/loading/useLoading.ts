import { useContext } from 'react';
import { LoadingContext, LoadingContextValue } from './LoadingContext';

export function useLoading(): LoadingContextValue {
  const context = useContext(LoadingContext);
  if (!context) {
    throw new Error('useLoading must be used within a LoadingProvider');
  }
  return context;
}
