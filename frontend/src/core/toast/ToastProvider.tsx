import React, { ReactNode } from 'react';
import { Toaster } from 'sonner';

export interface ToastProviderProps {
  children: ReactNode;
}

export const ToastProvider: React.FC<ToastProviderProps> = ({ children }) => {
  return (
    <>
      {children}
      <Toaster position="top-right" richColors closeButton duration={4000} />
    </>
  );
};
