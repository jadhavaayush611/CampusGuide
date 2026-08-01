import React, { memo } from 'react';
import { AggregatedCalendarEvent } from '../../../models/calendar.model';
import { Clock, MapPin, ExternalLink, AlertCircle, CheckCircle2, Calendar as CalendarIcon } from 'lucide-react';
import { useNavigate } from 'react-router';

interface AgendaViewProps {
  events: AggregatedCalendarEvent[];
  onSelectEvent: (event: AggregatedCalendarEvent) => void;
}

export const AgendaView: React.FC<AgendaViewProps> = memo(function AgendaView({ events, onSelectEvent }) {
  const navigate = useNavigate();

  // Sort events chronologically
  const sortedEvents = React.useMemo(() => {
    return [...events].sort((a, b) => a.startTime.getTime() - b.startTime.getTime());
  }, [events]);

  // Group events by Date string (YYYY-MM-DD)
  const groupedEvents = React.useMemo(() => {
    const groups: { [dateStr: string]: { date: Date; items: AggregatedCalendarEvent[] } } = {};

    sortedEvents.forEach((ev) => {
      const dateStr = ev.startTime.toISOString().split('T')[0];
      if (!groups[dateStr]) {
        groups[dateStr] = { date: ev.startTime, items: [] };
      }
      groups[dateStr].items.push(ev);
    });

    return Object.values(groups);
  }, [sortedEvents]);

  if (sortedEvents.length === 0) {
    return (
      <div className="bg-white rounded-3xl border border-gray-200/80 p-12 text-center space-y-4 shadow-xs">
        <div className="w-16 h-16 bg-blue-50 text-blue-600 rounded-3xl flex items-center justify-center mx-auto">
          <CalendarIcon className="w-8 h-8" />
        </div>
        <div>
          <h3 className="font-bold text-gray-900 text-lg">No Agenda Items Found</h3>
          <p className="text-xs text-gray-500 max-w-sm mx-auto mt-1">
            No events match your current filter state or search parameters for this period.
          </p>
        </div>
      </div>
    );
  }

  const todayStr = new Date().toISOString().split('T')[0];

  return (
    <div className="space-y-6">
      {groupedEvents.map((group) => {
        const groupDateStr = group.date.toISOString().split('T')[0];
        const isToday = groupDateStr === todayStr;
        const formattedDate = group.date.toLocaleDateString('en-US', {
          weekday: 'long',
          month: 'long',
          day: 'numeric',
          year: 'numeric',
        });

        return (
          <div key={groupDateStr} className="space-y-3">
            {/* Group Date Header */}
            <div className="flex items-center gap-3">
              <span
                className={`px-3 py-1 rounded-xl text-xs font-extrabold uppercase tracking-wider ${
                  isToday ? 'bg-blue-600 text-white shadow-xs' : 'bg-gray-100 text-gray-700'
                }`}
              >
                {isToday ? 'Today' : formattedDate}
              </span>
              <div className="h-px bg-gray-200 flex-1" />
              <span className="text-xs font-semibold text-gray-400">
                {group.items.length} event{group.items.length > 1 ? 's' : ''}
              </span>
            </div>

            {/* Event Items List */}
            <div className="space-y-3">
              {group.items.map((ev) => (
                <div
                  key={ev.id}
                  onClick={() => onSelectEvent(ev)}
                  className={`bg-white p-4 sm:p-5 rounded-2xl border border-gray-200/80 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-l-4 ${
                    ev.isCompleted ? 'opacity-60 bg-gray-50/60' : ''
                  }`}
                  style={{ borderLeftColor: ev.color }}
                >
                  <div className="space-y-1.5 flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span
                        className="px-2.5 py-0.5 rounded-full text-[10px] font-extrabold uppercase tracking-wider text-white"
                        style={{ backgroundColor: ev.color }}
                      >
                        {ev.sourceModule}
                      </span>
                      <span className="px-2 py-0.5 rounded-md bg-gray-100 text-gray-600 text-[10px] font-bold">
                        {ev.category}
                      </span>
                      {ev.hasConflict && (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-red-100 text-red-700 text-[10px] font-bold rounded-md">
                          <AlertCircle className="w-3 h-3 text-red-600" /> Conflict
                        </span>
                      )}
                    </div>

                    <h4 className="font-bold text-gray-900 text-base flex items-center gap-2">
                      <span>{ev.title}</span>
                      {ev.isCompleted && <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />}
                    </h4>

                    {ev.description && (
                      <p className="text-xs text-gray-600 line-clamp-2 leading-relaxed">
                        {ev.description}
                      </p>
                    )}
                  </div>

                  {/* Right Metadata & Deep Link */}
                  <div className="flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-2 pt-2 sm:pt-0 border-t sm:border-t-0 border-gray-100 text-xs text-gray-500 font-medium shrink-0">
                    <div className="flex items-center gap-1.5 text-gray-700 font-semibold">
                      <Clock className="w-4 h-4 text-gray-400" />
                      <span>
                        {ev.isAllDay
                          ? 'All Day'
                          : `${ev.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${ev.endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`}
                      </span>
                    </div>

                    {ev.location && (
                      <div className="flex items-center gap-1 text-gray-500 text-xs">
                        <MapPin className="w-3.5 h-3.5 text-gray-400" />
                        <span>{ev.location}</span>
                      </div>
                    )}

                    {ev.linkUrl && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(ev.linkUrl!);
                        }}
                        className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-700 font-bold text-xs hover:underline mt-1"
                      >
                        <span>Open Module</span>
                        <ExternalLink className="w-3 h-3" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
});
