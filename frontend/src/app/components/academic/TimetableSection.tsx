import React, { useState } from 'react';
import { Clock, MapPin, User, Calendar as CalendarIcon, Grid, List, Sparkles } from 'lucide-react';
import { TimetableSlot } from '../../../models/planner.model';

interface TimetableSectionProps {
  timetable: TimetableSlot[];
  isLoading: boolean;
}

const WEEKDAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'] as const;

export const TimetableSection: React.FC<TimetableSectionProps> = ({
  timetable,
  isLoading,
}) => {
  // Determine current day of week
  const daysMap: Record<number, string> = {
    1: 'MONDAY',
    2: 'TUESDAY',
    3: 'WEDNESDAY',
    4: 'THURSDAY',
    5: 'FRIDAY',
    6: 'SATURDAY',
    0: 'SUNDAY',
  };
  const currentDayStr = daysMap[new Date().getDay()] || 'MONDAY';

  const [selectedDay, setSelectedDay] = useState<string>('ALL');
  const [viewMode, setViewMode] = useState<'GRID' | 'LIST'>('GRID');

  // Filter slots by selected day if not ALL
  const filteredSlots = timetable.filter(
    (slot) => selectedDay === 'ALL' || slot.dayOfWeek === selectedDay
  );

  const todaySlots = timetable.filter((slot) => slot.dayOfWeek === currentDayStr);

  const getTypeBadge = (type?: string) => {
    switch (type) {
      case 'LAB':
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-800">LAB</span>;
      case 'TUTORIAL':
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800">TUTORIAL</span>;
      case 'SEMINAR':
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-100 text-indigo-800">SEMINAR</span>;
      default:
        return <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-100 text-blue-800">LECTURE</span>;
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <Clock className="w-5 h-5 text-emerald-600" />
              Class Timetable & Schedule
            </h2>
            <span className="px-2 py-0.5 bg-emerald-50 text-emerald-700 text-xs font-semibold rounded-md border border-emerald-200">
              Today: {todaySlots.length} Classes
            </span>
          </div>
          <p className="text-xs text-gray-500 mt-0.5">
            Weekly class schedule, lecture halls, laboratory locations & instructor details.
          </p>
        </div>

        {/* View mode toggle */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <div className="bg-gray-100 p-1 rounded-xl flex items-center gap-1">
            <button
              onClick={() => setViewMode('GRID')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors ${
                viewMode === 'GRID' ? 'bg-white text-gray-900 shadow-xs' : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <Grid className="w-3.5 h-3.5" />
              <span>Weekly Grid</span>
            </button>
            <button
              onClick={() => setViewMode('LIST')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors ${
                viewMode === 'LIST' ? 'bg-white text-gray-900 shadow-xs' : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <List className="w-3.5 h-3.5" />
              <span>List View</span>
            </button>
          </div>
        </div>
      </div>

      {/* Today's Classes Alert Banner */}
      {todaySlots.length > 0 && (
        <div className="bg-gradient-to-r from-emerald-50 to-teal-50 border border-emerald-200 rounded-xl p-4 mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-emerald-950">Today's Schedule ({currentDayStr})</h4>
              <p className="text-xs text-emerald-700">
                You have {todaySlots.length} lecture/lab sessions scheduled today.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 overflow-x-auto">
            {todaySlots.map((slot) => (
              <div key={slot.id} className="bg-white px-3 py-1.5 rounded-lg border border-emerald-200 text-xs flex-shrink-0">
                <span className="font-bold text-gray-900">{slot.courseCode}</span>
                <span className="text-emerald-700 ml-1 font-medium">({slot.startTime})</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Day Selector Pills */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-2 mb-6 border-b border-gray-100">
        <button
          onClick={() => setSelectedDay('ALL')}
          className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-colors ${
            selectedDay === 'ALL'
              ? 'bg-blue-600 text-white shadow-xs'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          All Week
        </button>
        {WEEKDAYS.map((day) => {
          const isToday = day === currentDayStr;
          return (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-colors flex items-center gap-1 ${
                selectedDay === day
                  ? 'bg-blue-600 text-white shadow-xs'
                  : isToday
                  ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-200 font-bold'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <span>{day.slice(0, 3)}</span>
              {isToday && <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>}
            </button>
          );
        })}
      </div>

      {/* Loading Skeleton */}
      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-32 bg-gray-100 rounded-xl border border-gray-200"></div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredSlots.length === 0 && (
        <div className="py-12 text-center bg-gray-50 rounded-xl border border-dashed border-gray-300">
          <CalendarIcon className="w-10 h-10 text-gray-300 mx-auto mb-2" />
          <h3 className="text-sm font-semibold text-gray-800">No classes scheduled</h3>
          <p className="text-xs text-gray-500 mt-1">There are no slots registered for the selected day.</p>
        </div>
      )}

      {/* GRID VIEW */}
      {!isLoading && viewMode === 'GRID' && filteredSlots.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredSlots.map((slot) => (
            <div
              key={slot.id}
              className="bg-white border border-gray-200 hover:border-emerald-300 hover:shadow-md rounded-xl p-5 transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="px-2 py-0.5 bg-gray-100 text-gray-800 text-xs font-bold rounded">
                    {slot.dayOfWeek}
                  </span>
                  {getTypeBadge(slot.type)}
                </div>

                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xs font-bold text-blue-700">{slot.courseCode}</span>
                  <span className="text-xs font-bold text-gray-900 line-clamp-1">{slot.courseTitle}</span>
                </div>

                <div className="flex items-center gap-2 text-xs font-semibold text-gray-700 mt-2 bg-emerald-50/80 text-emerald-900 px-2.5 py-1 rounded-lg w-fit">
                  <Clock className="w-3.5 h-3.5 text-emerald-600" />
                  <span>{slot.startTime} - {slot.endTime}</span>
                </div>

                <div className="space-y-1 mt-3 pt-3 border-t border-gray-100 text-xs text-gray-600">
                  <div className="flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-gray-400" />
                    <span>
                      Room {slot.room} • {slot.buildingName || slot.buildingCode || 'Main Campus'}
                    </span>
                  </div>
                  {slot.instructor && (
                    <div className="flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5 text-gray-400" />
                      <span>{slot.instructor}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* LIST VIEW */}
      {!isLoading && viewMode === 'LIST' && filteredSlots.length > 0 && (
        <div className="space-y-3">
          {filteredSlots.map((slot) => (
            <div
              key={slot.id}
              className="bg-white border border-gray-200 rounded-xl p-4 flex flex-col md:flex-row md:items-center justify-between gap-4 hover:border-emerald-300 transition-colors"
            >
              <div className="flex items-start gap-4">
                <div className="w-24 text-center py-2 bg-emerald-50 rounded-xl border border-emerald-100 flex-shrink-0">
                  <span className="text-[10px] font-bold uppercase text-emerald-700 block">{slot.dayOfWeek}</span>
                  <span className="text-xs font-bold text-emerald-950 mt-0.5 block">{slot.startTime}</span>
                  <span className="text-[10px] text-emerald-600 block">{slot.endTime}</span>
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded">
                      {slot.courseCode}
                    </span>
                    <h3 className="text-sm font-bold text-gray-900">{slot.courseTitle}</h3>
                    {getTypeBadge(slot.type)}
                  </div>
                  <div className="flex flex-wrap items-center gap-4 text-xs text-gray-600 mt-2">
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-gray-400" />
                      Room {slot.room} ({slot.buildingName || slot.buildingCode || 'Campus'})
                    </span>
                    {slot.instructor && (
                      <span className="flex items-center gap-1">
                        <User className="w-3.5 h-3.5 text-gray-400" />
                        {slot.instructor}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
