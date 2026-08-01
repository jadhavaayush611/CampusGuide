import React from 'react';
import { Skeleton } from '../ui/skeleton';
import { ClipboardList, Plus } from 'lucide-react';

export const TaskSkeleton: React.FC<{ count?: number; viewMode?: 'grid' | 'list' }> = ({
  count = 6,
  viewMode = 'grid',
}) => {
  if (viewMode === 'list') {
    return (
      <div className="space-y-3">
        {Array.from({ length: count }).map((_, i) => (
          <div key={i} className="p-4 bg-white rounded-xl border border-gray-100 flex items-center gap-4">
            <Skeleton className="w-5 h-5 rounded" />
            <div className="flex-1 space-y-2">
              <Skeleton className="h-5 w-2/3" />
              <Skeleton className="h-4 w-1/3" />
            </div>
            <Skeleton className="h-6 w-20 rounded-full" />
            <Skeleton className="h-6 w-16 rounded-full" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="p-5 bg-white rounded-2xl border border-gray-100 space-y-4 shadow-sm">
          <div className="flex items-center justify-between">
            <Skeleton className="h-5 w-24 rounded-full" />
            <Skeleton className="h-5 w-16 rounded-full" />
          </div>
          <Skeleton className="h-6 w-3/4" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-2/3" />
          <div className="space-y-1">
            <div className="flex justify-between">
              <Skeleton className="h-3 w-16" />
              <Skeleton className="h-3 w-8" />
            </div>
            <Skeleton className="h-2 w-full rounded-full" />
          </div>
          <div className="pt-2 flex justify-between items-center border-t border-gray-100">
            <Skeleton className="h-4 w-28" />
            <Skeleton className="h-8 w-8 rounded-full" />
          </div>
        </div>
      ))}
    </div>
  );
};

export const TaskEmptyState: React.FC<{
  title?: string;
  description?: string;
  onAction?: () => void;
  actionLabel?: string;
}> = ({
  title = 'No tasks found',
  description = 'No tasks match your current filter criteria or search query. Create a new task to get started.',
  onAction,
  actionLabel = 'Create New Task',
}) => {
  return (
    <div className="py-16 px-4 bg-white border border-dashed border-gray-200 rounded-3xl text-center space-y-4">
      <div className="w-16 h-16 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center mx-auto shadow-sm">
        <ClipboardList className="w-8 h-8" />
      </div>
      <div className="space-y-1 max-w-md mx-auto">
        <h3 className="text-lg font-bold text-gray-900">{title}</h3>
        <p className="text-sm text-gray-500">{description}</p>
      </div>
      {onAction && (
        <button
          onClick={onAction}
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-[#2563EB] hover:bg-blue-700 text-white rounded-xl font-semibold text-sm transition-all shadow-md hover:shadow-blue-200"
        >
          <Plus className="w-4 h-4" />
          {actionLabel}
        </button>
      )}
    </div>
  );
};
