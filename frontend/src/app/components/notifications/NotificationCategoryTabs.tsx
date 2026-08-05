import React from 'react';
import {
  NotificationCategory,
} from '../../../models/notification.model';
import {
  GraduationCap,
  CalendarCheck,
  Calendar,
  Users,
  Shield,
  BookOpen,
  ClipboardList,
  Compass,
  KeyRound,
  Server,
  Layers,
} from 'lucide-react';

interface NotificationCategoryTabsProps {
  selectedCategory: NotificationCategory | 'ALL';
  onSelectCategory: (category: NotificationCategory | 'ALL') => void;
  categoryCounts?: Record<string, number>;
}

const CATEGORIES: { id: NotificationCategory | 'ALL'; label: string; icon: React.FC<{ className?: string }> }[] = [
  { id: 'ALL', label: 'All Categories', icon: Layers },
  { id: 'Academic', label: 'Academic', icon: GraduationCap },
  { id: 'Planner', label: 'Planner', icon: CalendarCheck },
  { id: 'Calendar', label: 'Calendar', icon: Calendar },
  { id: 'Communities', label: 'Communities', icon: Users },
  { id: 'Councils', label: 'Councils', icon: Shield },
  { id: 'Resources', label: 'Resources', icon: BookOpen },
  { id: 'Notices', label: 'Notices', icon: ClipboardList },
  { id: 'Atlas', label: 'Atlas AI', icon: Compass },
  { id: 'Authentication', label: 'Authentication', icon: KeyRound },
  { id: 'System', label: 'System', icon: Server },
];

export const NotificationCategoryTabs: React.FC<NotificationCategoryTabsProps> = ({
  selectedCategory,
  onSelectCategory,
  categoryCounts,
}) => {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLButtonElement>, index: number) => {
    let nextIndex = index;
    if (e.key === 'ArrowRight') {
      nextIndex = (index + 1) % CATEGORIES.length;
    } else if (e.key === 'ArrowLeft') {
      nextIndex = (index - 1 + CATEGORIES.length) % CATEGORIES.length;
    } else if (e.key === 'Home') {
      nextIndex = 0;
    } else if (e.key === 'End') {
      nextIndex = CATEGORIES.length - 1;
    } else {
      return;
    }
    e.preventDefault();
    const parent = e.currentTarget.parentElement;
    if (parent) {
      const children = Array.from(parent.children) as HTMLButtonElement[];
      children[nextIndex]?.focus();
      onSelectCategory(CATEGORIES[nextIndex].id);
    }
  };

  return (
    <div
      role="tablist"
      aria-label="Notification Categories"
      className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none"
    >
      {CATEGORIES.map((cat, idx) => {
        const Icon = cat.icon;
        const isSelected = selectedCategory === cat.id;
        const count = categoryCounts ? categoryCounts[cat.id] ?? 0 : undefined;

        return (
          <button
            key={cat.id}
            role="tab"
            aria-selected={isSelected}
            tabIndex={isSelected ? 0 : -1}
            id={`tab-${cat.id}`}
            onClick={() => onSelectCategory(cat.id)}
            onKeyDown={(e) => handleKeyDown(e, idx)}
            className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition-all duration-150 border focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1 ${
              isSelected
                ? 'bg-blue-600 text-white border-blue-600 shadow-sm shadow-blue-200'
                : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50 hover:border-gray-300'
            }`}
          >
            <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-white' : 'text-gray-500'}`} aria-hidden="true" />
            <span>{cat.label}</span>
            {count !== undefined && count > 0 && (
              <span
                className={`px-1.5 py-0.5 rounded-full text-[10px] font-bold ${
                  isSelected ? 'bg-white/20 text-white' : 'bg-gray-100 text-gray-600'
                }`}
              >
                {count}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
};
