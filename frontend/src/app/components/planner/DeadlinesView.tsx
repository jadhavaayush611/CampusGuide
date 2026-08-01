import React from 'react';
import { useNavigate } from 'react-router';
import { Calendar, AlertCircle, CheckCircle2, Clock, CalendarDays, ExternalLink } from 'lucide-react';
import { useUpcomingDeadlines, UpcomingDeadlineItem } from '../../../hooks/planner/useUpcomingDeadlines';

export const DeadlinesView: React.FC = () => {
  const navigate = useNavigate();
  const { data, isLoading, isError } = useUpcomingDeadlines();

  const handleOpenCalendar = (title: string, date: string) => {
    navigate(`/calendar?date=${encodeURIComponent(date)}&filter=planner`);
  };

  if (isLoading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-28 bg-gray-200 rounded-3xl" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="h-64 bg-gray-200 rounded-2xl" />
          <div className="h-64 bg-gray-200 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="p-8 bg-red-50 text-red-700 rounded-3xl text-center space-y-2 border border-red-200">
        <AlertCircle className="w-8 h-8 text-red-500 mx-auto" />
        <h3 className="text-lg font-bold">Failed to load Deadlines</h3>
        <p className="text-xs">Unable to load combined deadline data. Please retry.</p>
      </div>
    );
  }

  const { upcoming, overdue, recentlyCompleted } = data;

  return (
    <div className="space-y-8">
      {/* Banner */}
      <div className="bg-gradient-to-r from-indigo-900 via-blue-900 to-slate-900 text-white p-6 sm:p-8 rounded-3xl shadow-lg flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 rounded-full text-xs font-semibold text-blue-200">
            <CalendarDays className="w-4 h-4 text-blue-400" />
            Unified Deadlines & Milestone Monitor
          </div>
          <h2 className="text-2xl font-bold">Upcoming Deadlines & Schedule Milestones</h2>
          <p className="text-xs text-blue-100/80 max-w-xl">
            Aggregate view of assignment deadlines, exam dates, registration windows, capstone milestones, and personal tasks.
          </p>
        </div>

        <button
          onClick={() => handleOpenCalendar('Campus Academic Calendar', new Date().toISOString().split('T')[0])}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-white text-gray-900 hover:bg-blue-50 rounded-xl font-bold text-xs transition-colors shadow-sm shrink-0"
        >
          <Calendar className="w-4 h-4 text-blue-600" />
          <span>Open in Calendar</span>
          <ExternalLink className="w-3.5 h-3.5 text-gray-400" />
        </button>
      </div>

      {/* Grid of Deadlines */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 1. Overdue Items */}
        <div className="space-y-4">
          <div className="flex items-center justify-between p-3 bg-red-50 rounded-2xl border border-red-200 text-red-900">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-red-600 shrink-0" />
              <h3 className="text-sm font-extrabold uppercase tracking-wider">Overdue Items</h3>
            </div>
            <span className="px-2.5 py-0.5 bg-red-600 text-white text-xs font-bold rounded-full">
              {overdue.length}
            </span>
          </div>

          {overdue.length === 0 ? (
            <div className="p-6 text-center bg-white rounded-2xl border border-gray-100 text-xs text-gray-400">
              No overdue items! Great job keeping up.
            </div>
          ) : (
            <div className="space-y-3">
              {overdue.map((item) => (
                <DeadlineCard key={item.id} item={item} onOpenCalendar={handleOpenCalendar} />
              ))}
            </div>
          )}
        </div>

        {/* 2. Upcoming Deadlines */}
        <div className="space-y-4 lg:col-span-2">
          <div className="flex items-center justify-between p-3 bg-blue-50 rounded-2xl border border-blue-200 text-blue-900">
            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-blue-600 shrink-0" />
              <h3 className="text-sm font-extrabold uppercase tracking-wider">Upcoming Deadlines</h3>
            </div>
            <span className="px-2.5 py-0.5 bg-blue-600 text-white text-xs font-bold rounded-full">
              {upcoming.length}
            </span>
          </div>

          {upcoming.length === 0 ? (
            <div className="p-6 text-center bg-white rounded-2xl border border-gray-100 text-xs text-gray-400">
              No upcoming deadlines found.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {upcoming.map((item) => (
                <DeadlineCard key={item.id} item={item} onOpenCalendar={handleOpenCalendar} />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Recently Completed Section */}
      {recentlyCompleted.length > 0 && (
        <div className="space-y-4 pt-4 border-t border-gray-200">
          <div className="flex items-center justify-between p-3 bg-emerald-50 rounded-2xl border border-emerald-200 text-emerald-900">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
              <h3 className="text-sm font-extrabold uppercase tracking-wider">Recently Completed Items</h3>
            </div>
            <span className="px-2.5 py-0.5 bg-emerald-600 text-white text-xs font-bold rounded-full">
              {recentlyCompleted.length}
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {recentlyCompleted.map((item) => (
              <DeadlineCard key={item.id} item={item} onOpenCalendar={handleOpenCalendar} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

const DeadlineCard: React.FC<{
  item: UpcomingDeadlineItem;
  onOpenCalendar: (title: string, date: string) => void;
}> = ({ item, onOpenCalendar }) => {
  const getTypeColor = () => {
    switch (item.type) {
      case 'TASK':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'CALENDAR':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'STUDY_GOAL':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  return (
    <div className={`p-4 bg-white rounded-2xl border transition-all duration-200 space-y-2 shadow-xs hover:shadow-md ${
      item.isOverdue ? 'border-red-300 bg-red-50/10' : item.isCompleted ? 'border-emerald-200 bg-emerald-50/10' : 'border-gray-100'
    }`}>
      <div className="flex items-center justify-between gap-2">
        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getTypeColor()}`}>
          {item.type} • {item.category || 'General'}
        </span>
        <button
          onClick={() => onOpenCalendar(item.title, item.dueDate)}
          className="text-xs text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-1"
          title="Open in Calendar Shortcut"
        >
          <span>Calendar</span>
          <ExternalLink className="w-3 h-3" />
        </button>
      </div>

      <h4 className={`text-sm font-bold text-gray-900 ${item.isCompleted ? 'line-through text-gray-500' : ''}`}>
        {item.title}
      </h4>

      <div className="flex items-center justify-between text-xs text-gray-500 pt-1">
        <div className="flex items-center gap-1">
          <Calendar className="w-3.5 h-3.5 text-gray-400" />
          <span className={item.isOverdue ? 'text-red-600 font-bold' : ''}>
            {item.dueDate}
          </span>
        </div>
        {item.isCompleted && (
          <span className="text-emerald-600 font-bold text-[10px]">COMPLETED</span>
        )}
      </div>
    </div>
  );
};
