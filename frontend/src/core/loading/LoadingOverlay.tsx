import React from 'react';
import { LoadingSpinner } from './LoadingSpinner';

export interface LoadingOverlayProps {
  isGlobal?: boolean;
  message?: string;
  className?: string;
}

export const LoadingOverlay: React.FC<LoadingOverlayProps> = ({
  isGlobal = false,
  message = 'Loading...',
  className = '',
}) => {
  const containerStyle: React.CSSProperties = isGlobal
    ? {
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 9999,
        backgroundColor: 'rgba(255, 255, 255, 0.85)',
        backdropFilter: 'blur(4px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }
    : {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 50,
        backgroundColor: 'rgba(255, 255, 255, 0.75)',
        backdropFilter: 'blur(2px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      };

  return (
    <div
      style={containerStyle}
      className={className}
      role="status"
      aria-live="polite"
      aria-busy="true"
      aria-label={message}
    >
      <LoadingSpinner size="lg" label={message} />
    </div>
  );
};
