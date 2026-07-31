import React from 'react';

export interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  color?: string;
  className?: string;
  label?: string;
}

const sizeMap = {
  sm: '16px',
  md: '24px',
  lg: '36px',
  xl: '48px',
};

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'md',
  color = '#2563eb',
  className = '',
  label,
}) => {
  const pixelSize = sizeMap[size];

  return (
    <div
      aria-label={label || 'Loading...'}
      role="status"
      style={{
        display: 'inline-flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '0.5rem',
      }}
      className={className}
    >
      <svg
        style={{
          width: pixelSize,
          height: pixelSize,
          animation: 'spin 0.8s linear infinite',
        }}
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          strokeWidth="3"
          style={{ opacity: 0.25, color }}
        />
        <path
          fill="currentColor"
          style={{ color }}
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
      {label && (
        <span style={{ fontSize: '0.875rem', color: '#4b5563', fontWeight: 500 }}>
          {label}
        </span>
      )}
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};
