import React from 'react';

export const NotificationSkeleton: React.FC = () => {
  return (
    <div className="space-y-3">
      {[1, 2, 3, 4, 5].map((idx) => (
        <div
          key={idx}
          className="p-5 bg-white rounded-2xl border border-gray-200 shadow-sm animate-pulse flex items-start gap-4"
        >
          <div className="w-11 h-11 bg-gray-200 rounded-2xl flex-shrink-0"></div>
          <div className="flex-1 space-y-2.5">
            <div className="flex items-center justify-between">
              <div className="h-4 bg-gray-200 rounded w-1/3"></div>
              <div className="h-4 bg-gray-200 rounded w-20"></div>
            </div>
            <div className="h-3.5 bg-gray-100 rounded w-5/6"></div>
            <div className="h-3 bg-gray-100 rounded w-1/4 pt-1"></div>
          </div>
        </div>
      ))}
    </div>
  );
};
