import React from 'react';
import {
  ChevronLeft,
  ChevronRight,
  Calendar as CalendarIcon,
  Plus,
  Search,
  Grid,
  Columns,
  Clock,
  List,
  Sparkles,
} from 'lucide-react';
import { CalendarViewMode } from '../../../models/calendar.model';

interface CalendarHeaderProps {
  currentDate: Date;
  viewMode: CalendarViewMode;
  searchQuery: string;
  onViewModeChange: (view: CalendarViewMode) => void;
  onNavigatePrev: () => void;
  onNavigateNext: () => void;
  onToday: () => void;
  onDateSelect: (date: Date) => void;
  onSearchChange: (query: string) => void;
  onOpenCreateModal: () => void;
  totalEventsCount?: number;
  conflictEventsCount?: number;
}

export const CalendarHeader: React.FC<CalendarHeaderProps> = ({
  currentDate,
  viewMode,
  searchQuery,
  onViewModeChange,
  onNavigatePrev,
  onNavigateNext,
  onToday,
  onDateSelect,
  onSearchChange,
  onOpenCreateModal,
  totalEventsCount = 0,
  conflictEventsCount = 0,
}) => {
  // Format heading based on view mode
  const formattedTitle = React.useMemo(() => {
    if (viewMode === 'month') {
      return currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    }
    if (viewMode === 'day') {
      return currentDate.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    }
    if (viewMode === 'week') {
      const startOfWeek = new Date(currentDate);
      const day = startOfWeek.getDay();
      const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // Monday start
      startOfWeek.setDate(diff);

      const endOfWeek = new Date(startOfWeek);
      endOfWeek.setDate(startOfWeek.getDate() + 6);

      const startStr = startOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      const endStr = endOfWeek.toLocaleDateString('en-US', {
        month: startOfWeek.getMonth() === endOfWeek.getMonth() ? 'numeric' : 'short',
        day: 'numeric',
        year: 'numeric',
      });
      return `${startStr} – ${endStr}`;
    }
    return currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }, [currentDate, viewMode]);

  return (
    <div className="space-y-6">
      {/* Top Banner Header */}
      <div className="relative overflow-hidden bg-gradient-to-r from-slate-950 via-slate-900 to-indigo-950 text-white rounded-3xl p-6 sm:p-8 shadow-xl border border-white/10">
        <div className="absolute right-0 top-0 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-semibold text-indigo-200 border border-white/10">
              <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
              Unified Campus Schedule & Time Management
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
              Campus Calendar
            </h1>
            <p className="text-indigo-100/80 text-sm max-w-2xl leading-relaxed">
              Aggregated view of academic deadlines, study goals, personal events, council meetings, and reminder schedules.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              onClick={onOpenCreateModal}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-xl font-semibold text-sm transition-all shadow-lg hover:shadow-blue-500/30"
            >
              <Plus className="w-4 h-4" />
              Add Personal Event
            </button>
          </div>
        </div>
      </div>

      {/* Control & Navigation Bar */}
      <div className="bg-white p-4 sm:p-5 rounded-2xl border border-gray-200/80 shadow-xs space-y-4 lg:space-y-0 lg:flex lg:items-center lg:justify-between gap-4">
        {/* Navigation & Date Display */}
        <div className="flex items-center gap-3">
          <div className="flex items-center bg-gray-100 p-1 rounded-xl border border-gray-200">
            <button
              onClick={onNavigatePrev}
              className="p-2 hover:bg-white text-gray-700 rounded-lg transition-colors shadow-2xs"
              title="Previous"
              aria-label="Previous"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={onToday}
              className="px-3 py-1.5 hover:bg-white text-gray-800 text-xs font-bold rounded-lg transition-colors shadow-2xs"
              title="Jump to Today"
            >
              Today
            </button>
            <button
              onClick={onNavigateNext}
              className="p-2 hover:bg-white text-gray-700 rounded-lg transition-colors shadow-2xs"
              title="Next"
              aria-label="Next"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <div className="relative flex items-center gap-2">
            <h2 className="text-xl font-bold text-gray-900 tracking-tight whitespace-nowrap">
              {formattedTitle}
            </h2>
            <input
              type="date"
              className="opacity-0 absolute inset-0 w-full h-full cursor-pointer z-10"
              value={currentDate.toISOString().split('T')[0]}
              onChange={(e) => {
                if (e.target.value) {
                  onDateSelect(new Date(e.target.value));
                }
              }}
              title="Jump to specific date"
            />
            <span className="p-1.5 hover:bg-gray-100 text-gray-500 rounded-lg cursor-pointer transition-colors" title="Pick Date">
              <CalendarIcon className="w-4 h-4" />
            </span>
          </div>
        </div>

        {/* Search & View Switcher */}
        <div className="flex flex-wrap items-center gap-3">
          {/* Search Box */}
          <div className="relative min-w-[200px] flex-1 sm:flex-initial">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search calendar events..."
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              className="w-full pl-9 pr-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
            />
          </div>

          {/* View Mode Buttons */}
          <div className="flex items-center bg-gray-100 p-1 rounded-xl border border-gray-200 text-xs font-semibold text-gray-600">
            <button
              onClick={() => onViewModeChange('month')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all ${
                viewMode === 'month'
                  ? 'bg-white text-blue-600 shadow-xs font-bold'
                  : 'hover:text-gray-900'
              }`}
            >
              <Grid className="w-3.5 h-3.5" />
              <span>Month</span>
            </button>
            <button
              onClick={() => onViewModeChange('week')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all ${
                viewMode === 'week'
                  ? 'bg-white text-blue-600 shadow-xs font-bold'
                  : 'hover:text-gray-900'
              }`}
            >
              <Columns className="w-3.5 h-3.5" />
              <span>Week</span>
            </button>
            <button
              onClick={() => onViewModeChange('day')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all ${
                viewMode === 'day'
                  ? 'bg-white text-blue-600 shadow-xs font-bold'
                  : 'hover:text-gray-900'
              }`}
            >
              <Clock className="w-3.5 h-3.5" />
              <span>Day</span>
            </button>
            <button
              onClick={() => onViewModeChange('agenda')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all ${
                viewMode === 'agenda'
                  ? 'bg-white text-blue-600 shadow-xs font-bold'
                  : 'hover:text-gray-900'
              }`}
            >
              <List className="w-3.5 h-3.5" />
              <span>Agenda</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
