import React from 'react';
import { AggregatedCalendarEvent } from '../../../models/calendar.model';
import {
  X,
  Clock,
  MapPin,
  Calendar as CalendarIcon,
  Tag,
  Layers,
  Users,
  Paperclip,
  Edit2,
  Trash2,
  ExternalLink,
  AlertCircle,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';
import { useNavigate } from 'react-router';

interface EventDetailsModalProps {
  isOpen: boolean;
  event: AggregatedCalendarEvent | null;
  onClose: () => void;
  onEditPersonalEvent?: (event: AggregatedCalendarEvent) => void;
  onDeletePersonalEvent?: (id: string) => void;
}

export const EventDetailsModal: React.FC<EventDetailsModalProps> = ({
  isOpen,
  event,
  onClose,
  onEditPersonalEvent,
  onDeletePersonalEvent,
}) => {
  const navigate = useNavigate();

  if (!isOpen || !event) return null;

  const isPersonal = event.sourceModule === 'personal';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div
        className="fixed inset-0"
        onClick={onClose}
      />

      <div className="relative w-full max-w-xl bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden z-10 space-y-6 p-6 sm:p-8">
        {/* Top Bar / Header */}
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-2 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <span
                className="px-3 py-1 rounded-full text-xs font-extrabold uppercase tracking-wider text-white"
                style={{ backgroundColor: event.color }}
              >
                {event.sourceModule}
              </span>
              <span className="px-2.5 py-1 rounded-lg bg-gray-100 text-gray-700 text-xs font-bold">
                {event.category}
              </span>
              {event.hasConflict && (
                <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-lg border border-red-200">
                  <AlertCircle className="w-3.5 h-3.5 text-red-600" /> Overlap Conflict
                </span>
              )}
            </div>

            <h2 className="text-2xl font-bold text-gray-900 tracking-tight">
              {event.title}
            </h2>
          </div>

          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 text-gray-400 hover:text-gray-700 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Details */}
        <div className="space-y-4 text-sm text-gray-700">
          {/* Description */}
          {event.description && (
            <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200/60 leading-relaxed text-gray-800">
              {event.description}
            </div>
          )}

          {/* Time & All-Day */}
          <div className="flex items-start gap-3">
            <Clock className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
            <div>
              <span className="font-bold text-gray-900 block">Date & Time</span>
              <span className="text-gray-600">
                {event.startTime.toLocaleDateString('en-US', {
                  weekday: 'short',
                  month: 'short',
                  day: 'numeric',
                  year: 'numeric',
                })}
              </span>
              <div className="text-xs text-gray-500 font-medium mt-0.5">
                {event.isAllDay ? (
                  <span className="inline-block px-2 py-0.5 bg-blue-50 text-blue-700 font-bold rounded">
                    All-Day Event
                  </span>
                ) : (
                  `${event.startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${event.endTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
                )}
              </div>
            </div>
          </div>

          {/* Location */}
          {event.location && (
            <div className="flex items-start gap-3">
              <MapPin className="w-4 h-4 text-purple-600 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold text-gray-900 block">Location</span>
                <span className="text-gray-600">{event.location}</span>
              </div>
            </div>
          )}

          {/* Recurrence (if present) */}
          {event.recurrence && (
            <div className="flex items-start gap-3">
              <RefreshCw className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold text-gray-900 block">Recurrence</span>
                <span className="text-gray-600">{event.recurrence}</span>
              </div>
            </div>
          )}

          {/* Participants */}
          {event.participants && event.participants.length > 0 && (
            <div className="flex items-start gap-3">
              <Users className="w-4 h-4 text-indigo-600 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold text-gray-900 block">Participants</span>
                <div className="flex flex-wrap gap-2 mt-1">
                  {event.participants.map((p, idx) => (
                    <span
                      key={idx}
                      className="px-2.5 py-1 bg-indigo-50 text-indigo-700 text-xs font-semibold rounded-lg"
                    >
                      {p.name}
                    </span>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Attachments */}
          {event.attachments && event.attachments.length > 0 && (
            <div className="flex items-start gap-3">
              <Paperclip className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold text-gray-900 block">Attachments</span>
                <div className="space-y-1.5 mt-1">
                  {event.attachments.map((att, idx) => (
                    <a
                      key={idx}
                      href={att.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-2 text-xs font-semibold text-blue-600 hover:underline bg-blue-50 px-3 py-1.5 rounded-xl"
                    >
                      <span>{att.name}</span>
                      <ExternalLink className="w-3 h-3" />
                    </a>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="pt-4 border-t border-gray-100 flex items-center justify-between gap-3">
          {event.linkUrl ? (
            <button
              onClick={() => {
                onClose();
                navigate(event.linkUrl!);
              }}
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-xl font-bold text-xs transition-colors"
            >
              <span>Open in Originating Module</span>
              <ExternalLink className="w-4 h-4" />
            </button>
          ) : (
            <div />
          )}

          <div className="flex items-center gap-2">
            {isPersonal && (
              <>
                <button
                  onClick={() => {
                    onClose();
                    onDeletePersonalEvent?.(event.originalId);
                  }}
                  className="p-2.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-xl transition-colors"
                  title="Delete Personal Event"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => {
                    onClose();
                    onEditPersonalEvent?.(event);
                  }}
                  className="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-bold text-xs transition-colors shadow-sm"
                >
                  <Edit2 className="w-4 h-4" />
                  <span>Edit Event</span>
                </button>
              </>
            )}

            {!isPersonal && (
              <button
                onClick={onClose}
                className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-xl font-bold text-xs transition-colors"
              >
                Close
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
