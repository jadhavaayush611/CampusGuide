import React from 'react';
import { AggregatedCalendarEvent } from '../../../models/calendar.model';
import { ConflictIndicator } from './ConflictIndicator';
import { Clock, CheckCircle2, AlertCircle } from 'lucide-react';

interface MonthViewProps {
  currentDate: Date;
  events: AggregatedCalendarEvent[];
  onSelectEvent: (event: AggregatedCalendarEvent) => void;
  onSelectDate: (date: Date) => void;
  onOpenCreateEventForDate: (date: Date) => void;
}

export const MonthView: React.FC<MonthViewProps> = ({
  currentDate,
  events,
  onSelectEvent,
  onSelectDate,
  onOpenCreateEventForDate,
}) => {
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  // Compute start of calendar grid (Monday start)
  const firstDayOfMonth = new Date(year, month, 1);
  const lastDayOfMonth = new Date(year, month + 1, 0);

  let startDayOfWeek = firstDayOfMonth.getDay(); // 0 is Sun, 1 is Mon...
  // Convert to Monday start: Mon=0, Tue=1, ..., Sun=6
  let offset = startDayOfWeek === 0 ? 6 : startDayOfWeek - 1;

  const startDate = new Date(year, month, 1 - offset);

  // Generate 35 or 42 grid cells (5 or 6 weeks)
  const daysGrid: Date[] = [];
  const curr = new Date(startDate);
  for (let i = 0; i < 42; i++) {
    daysGrid.push(new Date(curr));
    curr.setDate(curr.getDate() + 1);
  }

  // Trim to 35 days if 6th week belongs entirely to next month
  if (daysGrid[35].getMonth() !== month && daysGrid[35].getDate() > 7) {
    daysGrid.splice(35, 7);
  }

  const todayStr = new Date().toISOString().split('T')[0];

  const weekDayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  return (
    <div className="bg-white rounded-3xl border border-gray-200/80 shadow-xs overflow-hidden">
      {/* Weekday Header */}
      <div className="grid grid-cols-7 border-b border-gray-200 bg-gray-50/80 text-center text-xs font-bold text-gray-600 uppercase tracking-wider py-3">
        {weekDayLabels.map((day) => (
          <div key={day}>{day}</div>
        ))}
      </div>

      {/* Month Days Grid */}
      <div className="grid grid-cols-7 auto-rows-fr divide-x divide-y divide-gray-100 bg-gray-100">
        {daysGrid.map((date, idx) => {
          const dateStr = date.toISOString().split('T')[0];
          const isCurrentMonth = date.getMonth() === month;
          const isToday = dateStr === todayStr;

          // Filter events occurring on this date
          const dayEvents = events.filter((ev) => {
            const evStartStr = ev.startTime.toISOString().split('T')[0];
            const evEndStr = ev.endTime.toISOString().split('T')[0];
            return dateStr >= evStartStr && dateStr <= evEndStr;
          });

          const hasConflict = dayEvents.some((e) => e.hasConflict);
          const maxVisible = 3;
          const visibleEvents = dayEvents.slice(0, maxVisible);
          const overflowCount = dayEvents.length - maxVisible;

          return (
            <div
              key={idx}
              onClick={() => onSelectDate(date)}
              onDoubleClick={() => onOpenCreateEventForDate(date)}
              className={`min-h-[120px] p-2 bg-white flex flex-col justify-between transition-colors hover:bg-blue-50/20 group cursor-pointer ${
                !isCurrentMonth ? 'bg-gray-50/50 text-gray-400' : 'text-gray-900'
              }`}
            >
              {/* Day Header row */}
              <div className="flex items-center justify-between">
                <span
                  className={`w-7 h-7 flex items-center justify-center rounded-full text-xs font-bold ${
                    isToday
                      ? 'bg-blue-600 text-white shadow-sm'
                      : isCurrentMonth
                      ? 'text-gray-900 group-hover:bg-gray-100'
                      : 'text-gray-400'
                  }`}
                >
                  {date.getDate()}
                </span>

                {hasConflict && <ConflictIndicator compact />}
              </div>

              {/* Day Events Stack */}
              <div className="space-y-1 my-1 flex-1 overflow-hidden">
                {visibleEvents.map((ev) => (
                  <div
                    key={ev.id}
                    onClick={(e) => {
                      e.stopPropagation();
                      onSelectEvent(ev);
                    }}
                    className={`px-2 py-1 rounded-lg text-[11px] font-semibold truncate transition-all cursor-pointer shadow-2xs hover:scale-[1.02] flex items-center justify-between ${
                      ev.isCompleted ? 'opacity-50 line-through' : ''
                    }`}
                    style={{
                      backgroundColor: `${ev.color}15`,
                      color: ev.color,
                      borderLeft: `3px solid ${ev.color}`,
                    }}
                    title={`${ev.title} (${ev.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })})`}
                  >
                    <span className="truncate">{ev.title}</span>
                    {ev.hasConflict && (
                      <AlertCircle className="w-3 h-3 text-red-500 shrink-0 ml-1" />
                    )}
                  </div>
                ))}

                {overflowCount > 0 && (
                  <div className="text-[10px] font-extrabold text-gray-500 pl-1">
                    +{overflowCount} more...
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
