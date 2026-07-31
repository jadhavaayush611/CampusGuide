import { useState } from 'react';
import { Calendar, Clock, MapPin, Users, Download, ExternalLink, CheckCircle2 } from 'lucide-react';
import { CampusEvent } from '../../../models/campus.model';
import { toast } from '../../../core/toast/useToast';

interface CouncilEventsProps {
  events: CampusEvent[];
}

export function CouncilEvents({ events }: CouncilEventsProps) {
  const [registeredEventIds, setRegisteredEventIds] = useState<Record<string, boolean>>({});

  const handleToggleRegistration = (eventId: string, currentRegistered?: boolean) => {
    const isNowRegistered = !Boolean(registeredEventIds[eventId] ?? currentRegistered);
    setRegisteredEventIds((prev) => ({ ...prev, [eventId]: isNowRegistered }));
    if (isNowRegistered) {
      toast.success('Successfully registered for council event!');
    } else {
      toast.success('Event registration cancelled.');
    }
  };

  const handleDownloadIcs = (event: CampusEvent) => {
    const icsContent = `BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//CampusGuide//Council Events//EN
BEGIN:VEVENT
SUMMARY:${event.title}
DESCRIPTION:${event.description}
LOCATION:${event.location}
DTSTART:${new Date(event.startTime).toISOString().replace(/-|:|\.\d\d\d/g, '')}
DTEND:${new Date(event.endTime).toISOString().replace(/-|:|\.\d\d\d/g, '')}
END:VEVENT
END:VCALENDAR`;

    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${event.title.replace(/[^a-z0-9]/gi, '_').toLowerCase()}.ics`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    toast.success('Calendar file downloaded!');
  };

  if (!events || events.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-lg mx-auto">
        <div className="w-16 h-16 bg-blue-50 text-[#2563EB] rounded-full flex items-center justify-center mx-auto mb-4 text-2xl">
          📅
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-1">No upcoming council events</h3>
        <p className="text-sm text-gray-600">Check back soon for workshops, hackathons, and town hall announcements.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-bold text-gray-900">Upcoming Council Events ({events.length})</h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {events.map((event) => {
          const isRegistered = Boolean(registeredEventIds[event.id] ?? event.isRegistered);
          const startDate = new Date(event.startTime);
          const formattedDate = startDate.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            year: 'numeric',
          });
          const formattedTime = startDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });

          return (
            <div
              key={event.id}
              className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden flex flex-col justify-between hover:shadow-md transition-shadow"
            >
              <div>
                <div className="h-44 relative bg-gradient-to-r from-blue-500 to-indigo-600 overflow-hidden">
                  {event.imageUrl && (
                    <img src={event.imageUrl} alt={event.title} className="w-full h-full object-cover" />
                  )}
                  <div className="absolute top-3 right-3 bg-white/90 backdrop-blur-md px-3 py-1 rounded-lg text-xs font-semibold text-gray-900 shadow-xs flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-[#2563EB]" />
                    {formattedDate}
                  </div>
                </div>

                <div className="p-6">
                  <h4 className="text-xl font-bold text-gray-900 mb-2 leading-snug">{event.title}</h4>
                  <p className="text-sm text-gray-600 mb-4 line-clamp-2 leading-relaxed">{event.description}</p>

                  <div className="space-y-2 text-sm text-gray-600 mb-6 bg-gray-50 p-3 rounded-lg border border-gray-100">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4 text-gray-400" />
                      <span>{formattedTime}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-gray-400" />
                      <span>{event.location}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4 text-gray-400" />
                      <span>{event.attendeeCount} participants registered</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="px-6 pb-6 pt-2 flex items-center gap-3">
                <button
                  onClick={() => handleToggleRegistration(event.id, event.isRegistered)}
                  className={`flex-1 py-2.5 rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
                    isRegistered
                      ? 'bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100'
                      : 'bg-[#2563EB] text-white hover:bg-blue-600'
                  }`}
                >
                  {isRegistered ? (
                    <>
                      <CheckCircle2 className="w-4 h-4" />
                      Registered
                    </>
                  ) : (
                    'Register for Event'
                  )}
                </button>

                <button
                  onClick={() => handleDownloadIcs(event)}
                  className="px-3 py-2.5 border border-gray-200 text-gray-700 hover:bg-gray-50 rounded-lg text-sm transition-colors flex items-center gap-1.5"
                  title="Add to Calendar (.ics)"
                >
                  <Download className="w-4 h-4 text-gray-500" />
                  .ICS
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
