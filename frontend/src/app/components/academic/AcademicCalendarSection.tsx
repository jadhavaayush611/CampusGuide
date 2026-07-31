import React, { useState } from 'react';
import { Calendar as CalendarIcon, Filter, AlertTriangle, Sparkles, BookOpen, Clock } from 'lucide-react';
import { AcademicCalendarItem } from '../../../models/planner.model';

interface AcademicCalendarSectionProps {
  calendarItems: AcademicCalendarItem[];
  isLoading: boolean;
}

export const AcademicCalendarSection: React.FC<AcademicCalendarSectionProps> = ({
  calendarItems,
  isLoading,
}) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');

  const filteredItems = calendarItems.filter(
    (item) => selectedCategory === 'ALL' || item.category === selectedCategory
  );

  const getCategoryBadge = (category: AcademicCalendarItem['category']) => {
    switch (category) {
      case 'EXAM':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-red-50 text-red-700 border border-red-200 flex items-center gap-1">
            <AlertTriangle className="w-3 h-3" /> Examination
          </span>
        );
      case 'REGISTRATION':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-blue-50 text-blue-700 border border-blue-200 flex items-center gap-1">
            <BookOpen className="w-3 h-3" /> Registration
          </span>
        );
      case 'MILESTONE':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-purple-50 text-purple-700 border border-purple-200 flex items-center gap-1">
            <Sparkles className="w-3 h-3" /> Milestone
          </span>
        );
      case 'HOLIDAY':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1">
            <Clock className="w-3 h-3" /> Holiday
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-50 text-amber-700 border border-amber-200">
            Deadline
          </span>
        );
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <CalendarIcon className="w-5 h-5 text-amber-600" />
            Academic Calendar & Key Dates
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Exams, course registration windows, semester milestones, and university holidays.
          </p>
        </div>

        {/* Category Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto">
          {['ALL', 'EXAM', 'REGISTRATION', 'MILESTONE', 'HOLIDAY'].map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1 rounded-xl text-xs font-semibold whitespace-nowrap transition-colors ${
                selectedCategory === cat
                  ? 'bg-amber-600 text-white shadow-xs'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {cat === 'ALL' ? 'All Events' : cat}
            </button>
          ))}
        </div>
      </div>

      {/* Loading Skeleton */}
      {isLoading && (
        <div className="space-y-3 animate-pulse">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-20 bg-gray-100 rounded-xl border border-gray-200"></div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredItems.length === 0 && (
        <div className="py-10 text-center bg-gray-50 rounded-xl border border-dashed border-gray-300">
          <CalendarIcon className="w-8 h-8 text-gray-300 mx-auto mb-2" />
          <h3 className="text-xs font-semibold text-gray-700">No events found for this category</h3>
        </div>
      )}

      {/* Events List */}
      {!isLoading && filteredItems.length > 0 && (
        <div className="space-y-3">
          {filteredItems.map((item) => {
            const dateObj = new Date(item.date);
            const monthStr = dateObj.toLocaleString('default', { month: 'short' });
            const dayNum = dateObj.getDate();

            return (
              <div
                key={item.id}
                className="bg-white border border-gray-200 rounded-xl p-4 flex items-center justify-between gap-4 hover:border-amber-300 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-xl bg-amber-50 border border-amber-200/70 text-amber-900 flex flex-col items-center justify-center flex-shrink-0">
                    <span className="text-[10px] font-extrabold uppercase tracking-wider text-amber-700">
                      {monthStr}
                    </span>
                    <span className="text-lg font-black leading-tight">{dayNum}</span>
                  </div>

                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="text-sm font-bold text-gray-900">{item.title}</h3>
                      {getCategoryBadge(item.category)}
                    </div>
                    <p className="text-xs text-gray-600 line-clamp-1">{item.description}</p>
                  </div>
                </div>

                <div className="text-right flex-shrink-0">
                  <span className="text-xs font-semibold text-gray-500 bg-gray-100 px-2.5 py-1 rounded-lg">
                    {item.term || 'Fall 2026'}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
