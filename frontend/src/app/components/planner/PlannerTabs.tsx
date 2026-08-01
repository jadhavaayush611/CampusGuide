import React from 'react';
import { ClipboardList, Target, CalendarDays, GraduationCap, Archive } from 'lucide-react';

export type PlannerTab = 'TASKS' | 'GOALS' | 'DEADLINES' | 'ACADEMIC' | 'ARCHIVED';

interface PlannerTabsProps {
  activeTab: PlannerTab;
  onTabChange: (tab: PlannerTab) => void;
  tasksCount?: number;
  goalsCount?: number;
  overdueCount?: number;
  archivedCount?: number;
}

export const PlannerTabs: React.FC<PlannerTabsProps> = ({
  activeTab,
  onTabChange,
  tasksCount = 0,
  goalsCount = 0,
  overdueCount = 0,
  archivedCount = 0,
}) => {
  const tabs: { id: PlannerTab; label: string; icon: React.FC<{ className?: string }>; badge?: number; badgeColor?: string }[] = [
    { id: 'TASKS', label: 'Tasks & Productivity', icon: ClipboardList, badge: tasksCount, badgeColor: 'bg-blue-100 text-blue-800' },
    { id: 'GOALS', label: 'Study Goals', icon: Target, badge: goalsCount, badgeColor: 'bg-emerald-100 text-emerald-800' },
    { id: 'DEADLINES', label: 'Deadlines & Milestones', icon: CalendarDays, badge: overdueCount > 0 ? overdueCount : undefined, badgeColor: 'bg-red-100 text-red-700 font-bold' },
    { id: 'ACADEMIC', label: 'Academic & Degree', icon: GraduationCap },
    { id: 'ARCHIVED', label: 'Archived Tasks', icon: Archive, badge: archivedCount, badgeColor: 'bg-gray-100 text-gray-700' },
  ];

  return (
    <div className="border-b border-gray-200 overflow-x-auto scrollbar-none">
      <nav className="flex space-x-2 sm:space-x-4 min-w-max pb-1">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex items-center gap-2.5 px-4 py-3 text-sm font-semibold rounded-t-2xl border-b-2 transition-all ${
                isActive
                  ? 'border-[#2563EB] text-[#2563EB] bg-blue-50/50'
                  : 'border-transparent text-gray-500 hover:text-gray-900 hover:bg-gray-50'
              }`}
            >
              <Icon className={`w-4 h-4 ${isActive ? 'text-[#2563EB]' : 'text-gray-400'}`} />
              <span>{tab.label}</span>
              {tab.badge !== undefined && tab.badge > 0 && (
                <span className={`px-2 py-0.5 rounded-full text-xs ${tab.badgeColor || 'bg-gray-100 text-gray-700'}`}>
                  {tab.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </div>
  );
};
