import React from 'react';

interface ResourceSkeletonProps {
  viewMode?: 'grid' | 'list';
  count?: number;
}

export function ResourceSkeleton({ viewMode = 'grid', count = 6 }: ResourceSkeletonProps) {
  const items = Array.from({ length: count });

  if (viewMode === 'list') {
    return (
      <div className="space-y-3">
        {items.map((_, idx) => (
          <div
            key={idx}
            className="bg-white rounded-xl border border-gray-200 p-4 animate-pulse flex items-center justify-between"
          >
            <div className="flex items-center gap-4 flex-1">
              <div className="w-10 h-10 bg-gray-200 rounded-lg flex-shrink-0" />
              <div className="space-y-2 flex-1 max-w-xl">
                <div className="h-4 bg-gray-200 rounded w-3/4" />
                <div className="h-3 bg-gray-200 rounded w-1/2" />
              </div>
            </div>
            <div className="flex items-center gap-4">
              <div className="h-4 bg-gray-200 rounded w-20 hidden md:block" />
              <div className="w-9 h-9 bg-gray-200 rounded-lg" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {items.map((_, idx) => (
        <div
          key={idx}
          className="bg-white rounded-xl border border-gray-200 p-5 animate-pulse flex flex-col justify-between h-56"
        >
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="h-6 w-24 bg-gray-200 rounded-full" />
              <div className="h-6 w-16 bg-gray-200 rounded-full" />
            </div>
            <div className="h-5 bg-gray-200 rounded w-5/6 mb-2" />
            <div className="h-4 bg-gray-200 rounded w-full mb-1" />
            <div className="h-4 bg-gray-200 rounded w-2/3" />
          </div>
          <div className="pt-4 border-t border-gray-100 flex items-center justify-between">
            <div className="h-4 bg-gray-200 rounded w-28" />
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-gray-200 rounded-lg" />
              <div className="w-8 h-8 bg-gray-200 rounded-lg" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
