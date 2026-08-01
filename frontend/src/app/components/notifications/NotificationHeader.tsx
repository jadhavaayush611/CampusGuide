import React from 'react';
import {
  NotificationPriority,
  NotificationDeliveryStatus,
} from '../../../models/notification.model';
import { Search, Filter, ArrowUpDown, CheckCheck, RefreshCw } from 'lucide-react';

interface NotificationHeaderProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  selectedPriority: NotificationPriority | 'ALL';
  onPriorityChange: (priority: NotificationPriority | 'ALL') => void;
  selectedDeliveryStatus: NotificationDeliveryStatus | 'ALL';
  onDeliveryStatusChange: (status: NotificationDeliveryStatus | 'ALL') => void;
  sortBy: 'createdAt' | 'priority' | 'title';
  onSortByChange: (sortBy: 'createdAt' | 'priority' | 'title') => void;
  sortOrder: 'asc' | 'desc';
  onToggleSortOrder: () => void;
  onMarkAllAsRead: () => void;
  isMarkingAllRead?: boolean;
}

export const NotificationHeader: React.FC<NotificationHeaderProps> = ({
  searchQuery,
  onSearchChange,
  selectedPriority,
  onPriorityChange,
  selectedDeliveryStatus,
  onDeliveryStatusChange,
  sortBy,
  onSortByChange,
  sortOrder,
  onToggleSortOrder,
  onMarkAllAsRead,
  isMarkingAllRead,
}) => {
  return (
    <div className="bg-white rounded-2xl p-5 border border-gray-200 shadow-sm space-y-4">
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notification Center</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Discover, track, and manage all your platform alerts, reminders, and system updates.
          </p>
        </div>

        <button
          onClick={onMarkAllAsRead}
          disabled={isMarkingAllRead}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-xl text-xs font-semibold transition-all disabled:opacity-50"
        >
          <CheckCheck className="w-4 h-4 text-blue-600" />
          <span>{isMarkingAllRead ? 'Marking...' : 'Mark All as Read'}</span>
        </button>
      </div>

      {/* Filter and Search Controls */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 pt-2 border-t border-gray-100">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by title, message, or module..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
          />
        </div>

        {/* Priority Filter */}
        <div className="flex items-center gap-2">
          <Filter className="w-3.5 h-3.5 text-gray-400 hidden sm:inline" />
          <select
            value={selectedPriority}
            onChange={(e) => onPriorityChange(e.target.value as any)}
            className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          >
            <option value="ALL">All Priorities</option>
            <option value="URGENT">Urgent Priority</option>
            <option value="HIGH">High Priority</option>
            <option value="NORMAL">Normal Priority</option>
            <option value="LOW">Low Priority</option>
          </select>
        </div>

        {/* Delivery Status Filter */}
        <select
          value={selectedDeliveryStatus}
          onChange={(e) => onDeliveryStatusChange(e.target.value as any)}
          className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
        >
          <option value="ALL">All Delivery Statuses</option>
          <option value="DELIVERED">Delivered</option>
          <option value="SCHEDULED">Scheduled</option>
          <option value="FAILED">Failed</option>
        </select>

        {/* Sorting */}
        <div className="flex items-center gap-1.5">
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value as any)}
            className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          >
            <option value="createdAt">Sort by Date</option>
            <option value="priority">Sort by Priority</option>
            <option value="title">Sort by Title</option>
          </select>
          <button
            onClick={onToggleSortOrder}
            title={`Sort ${sortOrder === 'asc' ? 'Descending' : 'Ascending'}`}
            className="p-2 bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-xl text-gray-600 transition-all"
          >
            <ArrowUpDown className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
};
