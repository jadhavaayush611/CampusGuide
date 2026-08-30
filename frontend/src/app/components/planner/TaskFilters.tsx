import React from 'react';
import { Search, Filter, ArrowUpDown, LayoutGrid, List, RotateCcw } from 'lucide-react';
import { TaskCategory, TaskPriority, TaskStatus, TaskQueryParams } from '../../../models/planner.model';

interface TaskFiltersProps {
  filters: TaskQueryParams;
  onFilterChange: (newFilters: Partial<TaskQueryParams>) => void;
  onResetFilters: () => void;
  viewMode: 'grid' | 'list';
  onViewModeChange: (mode: 'grid' | 'list') => void;
  totalResults?: number;
}

const CATEGORIES: { value: TaskCategory | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All Categories' },
  { value: 'ACADEMIC', label: 'Academic' },
  { value: 'ASSIGNMENT', label: 'Assignment' },
  { value: 'PROJECT', label: 'Project' },
  { value: 'STUDY_GOAL', label: 'Study Goal' },
  { value: 'EXAMINATION', label: 'Examination' },
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'REMINDER', label: 'Reminder' },
  { value: 'MISCELLANEOUS', label: 'Miscellaneous' },
];

const PRIORITIES: { value: TaskPriority | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All Priorities' },
  { value: 'URGENT', label: 'Urgent' },
  { value: 'HIGH', label: 'High' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low' },
];

const STATUSES: { value: TaskStatus | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'All Statuses' },
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

const DUE_DATE_FILTERS = [
  { value: 'ALL', label: 'All Dates' },
  { value: 'TODAY', label: 'Due Today' },
  { value: 'THIS_WEEK', label: 'Due This Week' },
  { value: 'OVERDUE', label: 'Overdue' },
  { value: 'UPCOMING', label: 'Upcoming' },
];

export const TaskFilters: React.FC<TaskFiltersProps> = ({
  filters,
  onFilterChange,
  onResetFilters,
  viewMode,
  onViewModeChange,
  totalResults,
}) => {
  return (
    <div className="bg-white p-4 sm:p-5 rounded-2xl border border-gray-100 shadow-sm space-y-4">
      {/* Top Search & Primary Filter Row */}
      <div className="flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        {/* Search Bar */}
        <div className="relative flex-1 min-w-[240px]">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            value={filters.search || ''}
            onChange={(e) => onFilterChange({ search: e.target.value, page: 1 })}
            placeholder="Search tasks by title, description, or tags..."
            className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all placeholder:text-gray-400"
          />
        </div>

        {/* View Mode & Reset Controls */}
        <div className="flex items-center gap-2 self-end md:self-auto">
          {totalResults !== undefined && (
            <span className="text-xs font-medium text-gray-500 mr-2 hidden sm:inline">
              {totalResults} {totalResults === 1 ? 'task' : 'tasks'} found
            </span>
          )}

          <div className="flex items-center bg-gray-100 p-1 rounded-xl border border-gray-200">
            <button
              onClick={() => onViewModeChange('grid')}
              className={`p-1.5 rounded-lg text-xs font-semibold transition-all ${
                viewMode === 'grid' ? 'bg-white text-blue-600 shadow-xs' : 'text-gray-500 hover:text-gray-800'
              }`}
              title="Grid View"
            >
              <LayoutGrid className="w-4 h-4" />
            </button>
            <button
              onClick={() => onViewModeChange('list')}
              className={`p-1.5 rounded-lg text-xs font-semibold transition-all ${
                viewMode === 'list' ? 'bg-white text-blue-600 shadow-xs' : 'text-gray-500 hover:text-gray-800'
              }`}
              title="List View"
            >
              <List className="w-4 h-4" />
            </button>
          </div>

          <button
            onClick={onResetFilters}
            className="p-2 text-gray-500 hover:text-gray-800 hover:bg-gray-100 rounded-xl transition-colors"
            title="Reset Filters"
          >
            <RotateCcw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Filter Dropdowns Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 pt-1 border-t border-gray-100">
        {/* Category */}
        <div>
          <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
            Category
          </label>
          <select
            value={filters.category || 'ALL'}
            onChange={(e) => onFilterChange({ category: e.target.value as any, page: 1 })}
            className="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            {CATEGORIES.map((cat) => (
              <option key={cat.value} value={cat.value}>
                {cat.label}
              </option>
            ))}
          </select>
        </div>

        {/* Priority */}
        <div>
          <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
            Priority
          </label>
          <select
            value={filters.priority || 'ALL'}
            onChange={(e) => onFilterChange({ priority: e.target.value as any, page: 1 })}
            className="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            {PRIORITIES.map((p) => (
              <option key={p.value} value={p.value}>
                {p.label}
              </option>
            ))}
          </select>
        </div>

        {/* Status */}
        <div>
          <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
            Status
          </label>
          <select
            value={filters.status || 'ALL'}
            onChange={(e) => onFilterChange({ status: e.target.value as any, page: 1 })}
            className="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            {STATUSES.map((s) => (
              <option key={s.value} value={s.value}>
                {s.label}
              </option>
            ))}
          </select>
        </div>

        {/* Due Date */}
        <div>
          <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
            Due Date
          </label>
          <select
            value={filters.dueDateFilter || 'ALL'}
            onChange={(e) => onFilterChange({ dueDateFilter: e.target.value as any, page: 1 })}
            className="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            {DUE_DATE_FILTERS.map((d) => (
              <option key={d.value} value={d.value}>
                {d.label}
              </option>
            ))}
          </select>
        </div>

        {/* Sort By */}
        <div>
          <label className="block text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-1">
            Sort By
          </label>
          <select
            value={`${filters.sortBy || 'dueDate'}-${filters.sortOrder || 'asc'}`}
            onChange={(e) => {
              const [sortBy, sortOrder] = e.target.value.split('-');
              onFilterChange({ sortBy: sortBy as any, sortOrder: sortOrder as any });
            }}
            className="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            <option value="dueDate-asc">Due Date (Earliest)</option>
            <option value="dueDate-desc">Due Date (Latest)</option>
            <option value="priority-desc">Priority (Highest)</option>
            <option value="createdDate-desc">Recently Created</option>
            <option value="title-asc">Title (A-Z)</option>
          </select>
        </div>
      </div>
    </div>
  );
};
