import React from 'react';
import { AggregatedCalendarEvent } from '../../../models/calendar.model';
import { ConflictIndicator } from './ConflictIndicator';
import { Clock, MapPin, AlertCircle, Calendar as CalendarIcon, User, FileText } from 'lucide-react';

interface DayViewProps {
  currentDate: Date;
  events: AggregatedCalendarEvent[];
  onSelectEvent: (event: AggregatedCalendarEvent) => void;
  onOpenCreateEventForTime: (date: Date, hour: number) => void;
}

export const DayView: React.FC<DayViewProps> = ({
  currentDate,
  events,
  onSelectEvent,
  onOpenCreateEventForTime,
}) => {
  const dStr = currentDate.toISOString().split('T')[0];
  const hours = Array.from({ length: 24 }, (_, i) => i);

  // Filter events for this day
  const dayEvents = events.filter((ev) => {
    const sStr = ev.startTime.toISOString().split('T')[0];
    const eStr = ev.endTime.toISOString().split('T')[0];
    return dStr >= sStr && dStr <= eStr;
  });

  const allDayEvents = dayEvents.filter((e) => e.isAllDay);
  const timedEvents = dayEvents.filter((e) => !e.isAllDay);
  const conflictEvents = dayEvents.filter((e) => e.hasConflict);

  return (
    <div className="bg-white rounded-3xl border border-gray-200/80 shadow-xs overflow-hidden flex flex-col h-[750px]">
      {/* Day View Header */}
      <div className="p-4 bg-gray-50 border-b border-gray-200 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-blue-600 text-white flex items-center justify-center font-bold text-lg shadow-sm">
            {currentDate.getDate()}
          </div>
          <div>
            <h3 className="font-bold text-gray-900 text-base">
              {currentDate.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
            </h3>
            <p className="text-xs text-gray-500">{dayEvents.length} events scheduled</p>
          </div>
        </div>

        {conflictEvents.length > 0 && (
          <ConflictIndicator conflictCount={conflictEvents.length} />
        )}
      </div>

      {/* All-Day Events Banner */}
      {allDayEvents.length > 0 && (
        <div className="p-3 bg-indigo-50/50 border-b border-gray-200 space-y-1.5">
          <span className="text-[10px] font-bold text-indigo-700 uppercase tracking-wider">
            All-Day Events & Milestones
          </span>
          <div className="flex flex-wrap gap-2">
            {allDayEvents.map((ev) => (
              <div
                key={ev.id}
                onClick={() => onSelectEvent(ev)}
                className="px-3 py-1.5 bg-white border border-gray-200 rounded-xl text-xs font-bold text-gray-900 cursor-pointer shadow-2xs hover:border-blue-500 transition-all flex items-center gap-2"
              >
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: ev.color }} />
                <span>{ev.title}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Hourly Grid Scroll Container */}
      <div className="flex-1 overflow-y-auto grid grid-cols-[80px_1fr] relative divide-x divide-gray-200">
        {/* Time Axis Column */}
        <div className="divide-y divide-gray-100 bg-gray-50/50">
          {hours.map((h) => (
            <div key={h} className="h-20 pr-3 text-right text-xs font-semibold text-gray-400 pt-2">
              {h === 0 ? '12:00 AM' : h < 12 ? `${h}:00 AM` : h === 12 ? '12:00 PM' : `${h - 12}:00 PM`}
            </div>
          ))}
        </div>

        {/* Day Time Slot Grid */}
        <div className="relative h-[1920px] bg-white">
          {hours.map((h) => (
            <div
              key={h}
              onClick={() => onOpenCreateEventForTime(currentDate, h)}
              className="h-20 border-b border-gray-100 hover:bg-blue-50/20 cursor-pointer transition-colors"
            />
          ))}

          {/* Timed Events Cards Overlay */}
          {timedEvents.map((ev) => {
            const startHour = ev.startTime.getHours() + ev.startTime.getMinutes() / 60;
            const endHour = ev.endTime.getHours() + ev.endTime.getMinutes() / 60;
            const durationHours = Math.max(0.5, endHour - startHour);

            const topPx = startHour * 80;
            const heightPx = Math.max(45, durationHours * 80);

            return (
              <div
                key={ev.id}
                onClick={(e) => {
                  e.stopPropagation();
                  onSelectEvent(ev);
                }}
                className="absolute left-4 right-4 rounded-2xl p-3.5 border shadow-sm cursor-pointer transition-all hover:shadow-md hover:z-20 flex flex-col justify-between"
                style={{
                  top: `${topPx}px`,
                  height: `${heightPx}px`,
                  backgroundColor: `${ev.color}12`,
                  borderColor: `${ev.color}40`,
                  borderLeft: `6px solid ${ev.color}`,
                }}
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <h4 className="font-bold text-gray-900 text-sm">{ev.title}</h4>
                    {ev.hasConflict && (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-red-100 text-red-700 text-[10px] font-bold rounded-full">
                        <AlertCircle className="w-3 h-3 text-red-600" /> Overlap Conflict
                      </span>
                    )}
                  </div>
                  {ev.description && (
                    <p className="text-xs text-gray-600 mt-1 line-clamp-2">{ev.description}</p>
                  )}
                </div>

                <div className="flex flex-wrap items-center gap-4 text-xs font-semibold text-gray-600 mt-2">
                  <div className="flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5 text-gray-500" />
                    <span>
                      {ev.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} –{' '}
                      {ev.endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>

                  {ev.location && (
                    <div className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-gray-500" />
                      <span>{ev.location}</span>
                    </div>
                  )}

                  <div
                    className="px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider text-white ml-auto"
                    style={{ backgroundColor: ev.color }}
                  >
                    {ev.sourceModule}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
