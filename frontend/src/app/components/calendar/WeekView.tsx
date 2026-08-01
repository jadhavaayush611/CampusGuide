import React, { useState, memo, useCallback, useMemo } from 'react';
import { AggregatedCalendarEvent, UpdateCalendarEntryPayload } from '../../../models/calendar.model';
import { Clock, AlertCircle, MapPin } from 'lucide-react';

interface WeekViewProps {
  currentDate: Date;
  events: AggregatedCalendarEvent[];
  onSelectEvent: (event: AggregatedCalendarEvent) => void;
  onOpenCreateEventForTime: (date: Date, hour: number) => void;
  onUpdatePersonalEvent?: (id: string, payload: UpdateCalendarEntryPayload) => void;
}

export const WeekView: React.FC<WeekViewProps> = memo(function WeekView({
  currentDate,
  events,
  onSelectEvent,
  onOpenCreateEventForTime,
  onUpdatePersonalEvent,
}) {
  // Compute start of week (Monday)
  const weekDays = useMemo(() => {
    const startOfWeek = new Date(currentDate);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1);
    startOfWeek.setDate(diff);

    const days: Date[] = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(startOfWeek);
      d.setDate(startOfWeek.getDate() + i);
      days.push(d);
    }
    return days;
  }, [currentDate]);

  const hours = useMemo(() => Array.from({ length: 24 }, (_, i) => i), []);
  const todayStr = useMemo(() => new Date().toISOString().split('T')[0], []);

  // Drag and Drop state for rescheduling personal events
  const [draggedEventId, setDraggedEventId] = useState<string | null>(null);

  const handleDragStart = useCallback((e: React.DragEvent, event: AggregatedCalendarEvent) => {
    if (event.sourceModule !== 'personal') return;
    setDraggedEventId(event.originalId);
    e.dataTransfer.setData('text/plain', event.originalId);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent, targetDate: Date, targetHour: number) => {
      e.preventDefault();
      if (!draggedEventId || !onUpdatePersonalEvent) return;

      const originalEv = events.find((ev) => ev.originalId === draggedEventId && ev.sourceModule === 'personal');
      if (!originalEv) return;

      const durationMs = originalEv.endTime.getTime() - originalEv.startTime.getTime();

      const newStart = new Date(targetDate);
      newStart.setHours(targetHour, 0, 0, 0);

      const newEnd = new Date(newStart.getTime() + durationMs);

      onUpdatePersonalEvent(draggedEventId, {
        startTime: newStart.toISOString(),
        endTime: newEnd.toISOString(),
      });

      setDraggedEventId(null);
    },
    [draggedEventId, onUpdatePersonalEvent, events]
  );

  return (
    <div className="bg-white rounded-3xl border border-gray-200/80 shadow-xs overflow-hidden flex flex-col h-[750px]">
      {/* Week Header */}
      <div className="grid grid-cols-[60px_1fr] border-b border-gray-200 bg-gray-50/80">
        <div className="p-3 border-r border-gray-200 text-center text-xs font-bold text-gray-400">
          GMT
        </div>
        <div className="grid grid-cols-7 divide-x divide-gray-200">
          {weekDays.map((d) => {
            const dStr = d.toISOString().split('T')[0];
            const isToday = dStr === todayStr;
            const dayName = d.toLocaleDateString('en-US', { weekday: 'short' });

            return (
              <div key={dStr} className="p-3 text-center">
                <span className="text-xs font-bold text-gray-500 uppercase">{dayName}</span>
                <div className="mt-1 flex items-center justify-center">
                  <span
                    className={`w-7 h-7 flex items-center justify-center rounded-full text-xs font-bold ${
                      isToday ? 'bg-blue-600 text-white shadow-sm' : 'text-gray-900'
                    }`}
                  >
                    {d.getDate()}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* All-Day Events Banner */}
      {events.some((e) => e.isAllDay) && (
        <div className="grid grid-cols-[60px_1fr] border-b border-gray-200 bg-gray-50/40 text-xs">
          <div className="p-2 border-r border-gray-200 font-bold text-gray-500 text-[10px] uppercase flex items-center justify-center">
            All Day
          </div>
          <div className="grid grid-cols-7 divide-x divide-gray-200 p-1.5 gap-1">
            {weekDays.map((d) => {
              const dStr = d.toISOString().split('T')[0];
              const dayAllDayEvents = events.filter((e) => {
                if (!e.isAllDay) return false;
                const sStr = e.startTime.toISOString().split('T')[0];
                const eStr = e.endTime.toISOString().split('T')[0];
                return dStr >= sStr && dStr <= eStr;
              });

              return (
                <div key={dStr} className="space-y-1">
                  {dayAllDayEvents.map((ev) => (
                    <div
                      key={ev.id}
                      onClick={() => onSelectEvent(ev)}
                      className="px-2 py-1 rounded-md text-[11px] font-bold text-white truncate cursor-pointer shadow-2xs transition-all hover:opacity-90"
                      style={{ backgroundColor: ev.color }}
                      title={ev.title}
                    >
                      {ev.title}
                    </div>
                  ))}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Hourly Grid Scrollable Container */}
      <div className="flex-1 overflow-y-auto grid grid-cols-[60px_1fr] relative divide-x divide-gray-200">
        {/* Time Axis Column */}
        <div className="divide-y divide-gray-100 bg-gray-50/50">
          {hours.map((h) => (
            <div key={h} className="h-16 pr-2 text-right text-[11px] font-semibold text-gray-400 pt-1">
              {h === 0 ? '12 AM' : h < 12 ? `${h} AM` : h === 12 ? '12 PM' : `${h - 12} PM`}
            </div>
          ))}
        </div>

        {/* 7 Days Columns */}
        <div className="grid grid-cols-7 divide-x divide-gray-200 relative bg-white">
          {weekDays.map((d) => {
            const dStr = d.toISOString().split('T')[0];

            // Timed events for this day
            const dayEvents = events.filter((ev) => {
              if (ev.isAllDay) return false;
              const sStr = ev.startTime.toISOString().split('T')[0];
              return sStr === dStr;
            });

            return (
              <div
                key={dStr}
                className="relative h-[1536px] bg-grid-pattern" // 24 hours * 64px = 1536px
                onDragOver={(e) => e.preventDefault()}
              >
                {/* Hour Rows Lines */}
                {hours.map((h) => (
                  <div
                    key={h}
                    onClick={() => onOpenCreateEventForTime(d, h)}
                    onDrop={(e) => handleDrop(e, d, h)}
                    className="h-16 border-b border-gray-100/80 hover:bg-blue-50/20 cursor-pointer transition-colors group"
                  />
                ))}

                {/* Event Overlay Cards */}
                {dayEvents.map((ev) => {
                  const startHour = ev.startTime.getHours() + ev.startTime.getMinutes() / 60;
                  const endHour = ev.endTime.getHours() + ev.endTime.getMinutes() / 60;
                  const durationHours = Math.max(0.5, endHour - startHour);

                  const topPx = startHour * 64;
                  const heightPx = Math.max(32, durationHours * 64);

                  const isPersonal = ev.sourceModule === 'personal';

                  return (
                    <div
                      key={ev.id}
                      draggable={isPersonal}
                      onDragStart={(e) => handleDragStart(e, ev)}
                      onClick={(e) => {
                        e.stopPropagation();
                        onSelectEvent(ev);
                      }}
                      className={`absolute left-1 right-1 rounded-xl p-2 text-xs font-semibold overflow-hidden cursor-pointer shadow-sm transition-all hover:z-20 hover:shadow-md ${
                        isPersonal ? 'cursor-grab active:cursor-grabbing' : ''
                      }`}
                      style={{
                        top: `${topPx}px`,
                        height: `${heightPx}px`,
                        backgroundColor: `${ev.color}18`,
                        borderLeft: `4px solid ${ev.color}`,
                        color: '#1E293B',
                      }}
                      title={`${ev.title} (${ev.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })})`}
                    >
                      <div className="flex items-start justify-between gap-1">
                        <span className="font-bold text-gray-900 truncate leading-tight">
                          {ev.title}
                        </span>
                        {ev.hasConflict && <AlertCircle className="w-3.5 h-3.5 text-red-600 shrink-0" />}
                      </div>

                      <div className="flex items-center gap-1 text-[10px] text-gray-600 mt-0.5">
                        <Clock className="w-3 h-3 text-gray-500" />
                        <span>
                          {ev.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -{' '}
                          {ev.endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>

                      {ev.location && (
                        <div className="flex items-center gap-1 text-[10px] text-gray-500 mt-0.5 truncate">
                          <MapPin className="w-3 h-3" />
                          <span>{ev.location}</span>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
});
