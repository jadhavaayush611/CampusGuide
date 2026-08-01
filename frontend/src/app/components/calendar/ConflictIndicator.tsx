import React from 'react';
import { AlertCircle } from 'lucide-react';

interface ConflictIndicatorProps {
  conflictCount?: number;
  message?: string;
  compact?: boolean;
}

export const ConflictIndicator: React.FC<ConflictIndicatorProps> = ({
  conflictCount = 1,
  message,
  compact = false,
}) => {
  if (compact) {
    return (
      <span
        title="Time slot conflict detected!"
        className="inline-flex items-center gap-1 px-1.5 py-0.5 bg-red-100 text-red-700 text-[10px] font-bold rounded-md border border-red-200"
      >
        <AlertCircle className="w-3 h-3 text-red-600 shrink-0" />
        <span>Overlap</span>
      </span>
    );
  }

  return (
    <div className="flex items-center gap-2 px-3 py-1.5 bg-red-50 border border-red-200 text-red-800 rounded-xl text-xs font-medium">
      <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
      <span>
        {message || `${conflictCount} scheduling overlap${conflictCount > 1 ? 's' : ''} detected`}
      </span>
    </div>
  );
};
