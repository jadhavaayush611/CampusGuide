import React from 'react';
import { BellOff, SearchX, CheckCircle } from 'lucide-react';

interface NotificationEmptyStateProps {
  hasFilters: boolean;
  status: 'ALL' | 'UNREAD' | 'READ' | 'ARCHIVED';
  onResetFilters?: () => void;
}

export const NotificationEmptyState: React.FC<NotificationEmptyStateProps> = ({
  hasFilters,
  status,
  onResetFilters,
}) => {
  return (
    <div className="bg-white rounded-3xl p-12 border border-gray-200 shadow-sm text-center max-w-md mx-auto my-8 space-y-4">
      <div className="w-16 h-16 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center mx-auto mb-2">
        {hasFilters ? (
          <SearchX className="w-8 h-8 text-blue-600" />
        ) : status === 'UNREAD' ? (
          <CheckCircle className="w-8 h-8 text-emerald-600" />
        ) : (
          <BellOff className="w-8 h-8 text-blue-600" />
        )}
      </div>

      <div>
        <h3 className="text-lg font-bold text-gray-900">
          {hasFilters
            ? 'No matching notifications found'
            : status === 'UNREAD'
            ? "You're all caught up!"
            : status === 'ARCHIVED'
            ? 'No archived notifications'
            : 'No notifications yet'}
        </h3>
        <p className="text-xs text-gray-500 mt-1 leading-relaxed">
          {hasFilters
            ? 'Try adjusting your search query, priority filters, or category tabs to discover notifications.'
            : status === 'UNREAD'
            ? 'There are no unread notifications waiting for your review.'
            : status === 'ARCHIVED'
            ? 'Notifications you archive will appear here for historical reference.'
            : 'Important platform alerts, reminders, and module updates will appear here.'}
        </p>
      </div>

      {hasFilters && onResetFilters && (
        <button
          onClick={onResetFilters}
          className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white hover:bg-blue-700 rounded-xl text-xs font-bold transition-all shadow-sm"
        >
          Reset Filters
        </button>
      )}
    </div>
  );
};
