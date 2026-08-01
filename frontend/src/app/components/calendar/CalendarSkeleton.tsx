import React from 'react';

export const CalendarSkeleton: React.FC = () => {
  return (
    <div className="w-full space-y-6 animate-pulse p-4">
      <div className="h-14 bg-gray-200 rounded-2xl w-full" />
      <div className="grid grid-cols-7 gap-3">
        {Array.from({ length: 35 }).map((_, i) => (
          <div key={i} className="h-28 bg-gray-100 border border-gray-200 rounded-2xl p-2 space-y-2">
            <div className="h-4 w-6 bg-gray-200 rounded" />
            {i % 3 === 0 && <div className="h-5 bg-blue-100 rounded-lg w-full" />}
            {i % 5 === 0 && <div className="h-5 bg-purple-100 rounded-lg w-3/4" />}
          </div>
        ))}
      </div>
    </div>
  );
};
