import React from 'react';
import {
  Filter,
  CheckSquare,
  Square,
  Layers,
  AlertTriangle,
  User,
  CalendarCheck,
  Target,
  GraduationCap,
  Shield,
  Bell,
  Sparkles,
  CheckCircle2,
} from 'lucide-react';
import {
  CalendarEventSourceModule,
  CalendarEventCategory,
  CalendarFilterState,
} from '../../../models/calendar.model';

interface CalendarSidebarFilterProps {
  filterState: CalendarFilterState;
  onFilterChange: (updated: Partial<CalendarFilterState>) => void;
  onResetFilters: () => void;
  conflictEventsCount: number;
  totalEventsCount: number;
}

const MODULE_OPTIONS: { id: CalendarEventSourceModule; label: string; icon: React.FC<{ className?: string }>; color: string }[] = [
  { id: 'personal', label: 'Personal Events', icon: User, color: '#2563EB' },
  { id: 'planner', label: 'Planner Tasks & Deadlines', icon: CalendarCheck, color: '#F59E0B' },
  { id: 'study_goals', label: 'Study Goals', icon: Target, color: '#10B981' },
  { id: 'academic', label: 'Academic Calendar', icon: GraduationCap, color: '#8B5CF6' },
  { id: 'council', label: 'Council Events', icon: Shield, color: '#3B82F6' },
  { id: 'reminder', label: 'Reminder Schedule', icon: Bell, color: '#F43F5E' },
  { id: 'milestone', label: 'Semester Milestones', icon: Sparkles, color: '#EC4899' },
];

export const CalendarSidebarFilter: React.FC<CalendarSidebarFilterProps> = ({
  filterState,
  onFilterChange,
  onResetFilters,
  conflictEventsCount,
  totalEventsCount,
}) => {
  const toggleModule = (module: CalendarEventSourceModule) => {
    const current = filterState.selectedModules;
    const exists = current.includes(module);
    const updated = exists ? current.filter((m) => m !== module) : [...current, module];
    onFilterChange({ selectedModules: updated });
  };

  const selectAllModules = () => {
    onFilterChange({ selectedModules: [] });
  };

  return (
    <div className="w-full lg:w-72 bg-white rounded-3xl border border-gray-200/80 p-5 space-y-6 shadow-xs">
      {/* Header & Reset */}
      <div className="flex items-center justify-between pb-3 border-b border-gray-100">
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-blue-600" />
          <h3 className="font-bold text-gray-900 text-sm">Filter Calendar</h3>
        </div>
        <button
          onClick={onResetFilters}
          className="text-xs text-blue-600 hover:text-blue-700 font-semibold hover:underline"
        >
          Reset All
        </button>
      </div>

      {/* Conflict Status Alert Widget */}
      {conflictEventsCount > 0 ? (
        <div className="p-3.5 bg-red-50 border border-red-200 rounded-2xl space-y-1">
          <div className="flex items-center gap-2 text-red-800 text-xs font-bold">
            <AlertTriangle className="w-4 h-4 text-red-600 shrink-0" />
            <span>{conflictEventsCount} Overlapping Event{conflictEventsCount > 1 ? 's' : ''}</span>
          </div>
          <p className="text-[11px] text-red-600 leading-tight">
            Schedule conflicts detected in your calendar. Check Week or Day view to resolve time overlaps.
          </p>
        </div>
      ) : (
        <div className="p-3.5 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center gap-2.5 text-emerald-800 text-xs font-semibold">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>No schedule conflicts detected</span>
        </div>
      )}

      {/* Source Modules Section */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold uppercase tracking-wider text-gray-500">
            Source Modules
          </span>
          <button
            onClick={selectAllModules}
            className="text-[11px] font-semibold text-gray-500 hover:text-gray-900"
          >
            {filterState.selectedModules.length === 0 ? 'All Selected' : 'Select All'}
          </button>
        </div>

        <div className="space-y-1.5">
          {MODULE_OPTIONS.map((item) => {
            const isSelected =
              filterState.selectedModules.length === 0 || filterState.selectedModules.includes(item.id);
            const Icon = item.icon;

            return (
              <button
                key={item.id}
                onClick={() => toggleModule(item.id)}
                className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs transition-all ${
                  isSelected
                    ? 'bg-gray-50 text-gray-900 font-semibold border border-gray-200'
                    : 'text-gray-500 hover:bg-gray-50/60'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <span
                    className="w-2.5 h-2.5 rounded-full shrink-0"
                    style={{ backgroundColor: item.color }}
                  />
                  <Icon className="w-3.5 h-3.5 text-gray-500" />
                  <span>{item.label}</span>
                </div>
                {isSelected ? (
                  <CheckSquare className="w-4 h-4 text-blue-600 shrink-0" />
                ) : (
                  <Square className="w-4 h-4 text-gray-300 shrink-0" />
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Completed & Options */}
      <div className="space-y-3 pt-3 border-t border-gray-100">
        <span className="text-xs font-bold uppercase tracking-wider text-gray-500">
          Display Options
        </span>

        <label className="flex items-center justify-between p-2.5 bg-gray-50 rounded-xl border border-gray-200/60 cursor-pointer text-xs font-semibold text-gray-700">
          <span>Show Completed Tasks</span>
          <input
            type="checkbox"
            checked={filterState.showCompleted}
            onChange={(e) => onFilterChange({ showCompleted: e.target.checked })}
            className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
          />
        </label>
      </div>

      {/* Stats footer */}
      <div className="pt-3 border-t border-gray-100 text-center text-xs text-gray-400 font-medium">
        Showing {totalEventsCount} aggregated events
      </div>
    </div>
  );
};
