import React, { useState, useEffect } from 'react';
import {
  X,
  Plus,
  Edit2,
  Calendar as CalendarIcon,
  Clock,
  MapPin,
  Tag,
  Palette,
  FileText,
  Save,
} from 'lucide-react';
import {
  AggregatedCalendarEvent,
  CreateCalendarEntryPayload,
  UpdateCalendarEntryPayload,
} from '../../../models/calendar.model';

interface EventFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  eventToEdit?: AggregatedCalendarEvent | null;
  presetDate?: Date | null;
  presetHour?: number | null;
  onSubmitCreate: (payload: CreateCalendarEntryPayload) => void;
  onSubmitUpdate: (id: string, payload: UpdateCalendarEntryPayload) => void;
  isSubmitting?: boolean;
}

const COLOR_PRESETS = [
  '#2563EB', // Blue
  '#7C3AED', // Purple
  '#10B981', // Emerald
  '#F59E0B', // Amber
  '#EF4444', // Red
  '#EC4899', // Pink
  '#06B6D4', // Cyan
];

export const EventFormModal: React.FC<EventFormModalProps> = ({
  isOpen,
  onClose,
  eventToEdit,
  presetDate,
  presetHour,
  onSubmitCreate,
  onSubmitUpdate,
  isSubmitting = false,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<'PERSONAL' | 'ACADEMIC' | 'TASK' | 'EVENT' | 'OTHER'>('PERSONAL');
  const [location, setLocation] = useState('');
  const [startDateStr, setStartDateStr] = useState('');
  const [startTimeStr, setStartTimeStr] = useState('09:00');
  const [endDateStr, setEndDateStr] = useState('');
  const [endTimeStr, setEndTimeStr] = useState('10:00');
  const [isAllDay, setIsAllDay] = useState(false);
  const [color, setColor] = useState('#2563EB');
  const [notes, setNotes] = useState('');

  useEffect(() => {
    if (eventToEdit) {
      setTitle(eventToEdit.title);
      setDescription(eventToEdit.description || '');
      setLocation(eventToEdit.location || '');
      setIsAllDay(eventToEdit.isAllDay);
      setColor(eventToEdit.color || '#2563EB');

      const s = eventToEdit.startTime;
      const e = eventToEdit.endTime;
      setStartDateStr(s.toISOString().split('T')[0]);
      setStartTimeStr(s.toTimeString().slice(0, 5));
      setEndDateStr(e.toISOString().split('T')[0]);
      setEndTimeStr(e.toTimeString().slice(0, 5));
    } else {
      const targetDate = presetDate || new Date();
      const dateIso = targetDate.toISOString().split('T')[0];
      setStartDateStr(dateIso);
      setEndDateStr(dateIso);

      if (presetHour !== null && presetHour !== undefined) {
        const startH = presetHour.toString().padStart(2, '0');
        const endH = (presetHour + 1).toString().padStart(2, '0');
        setStartTimeStr(`${startH}:00`);
        setEndTimeStr(`${endH}:00`);
      } else {
        setStartTimeStr('09:00');
        setEndTimeStr('10:00');
      }

      setTitle('');
      setDescription('');
      setLocation('');
      setIsAllDay(false);
      setColor('#2563EB');
      setNotes('');
      setType('PERSONAL');
    }
  }, [eventToEdit, presetDate, presetHour, isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    const startDateTimeIso = isAllDay
      ? `${startDateStr}T00:00:00`
      : `${startDateStr}T${startTimeStr}:00`;

    const endDateTimeIso = isAllDay
      ? `${endDateStr}T23:59:59`
      : `${endDateStr}T${endTimeStr}:00`;

    if (eventToEdit) {
      onSubmitUpdate(eventToEdit.originalId, {
        title: title.trim(),
        description: description.trim(),
        type,
        location: location.trim(),
        startTime: startDateTimeIso,
        endTime: endDateTimeIso,
        isAllDay,
        color,
        notes: notes.trim(),
      });
    } else {
      onSubmitCreate({
        title: title.trim(),
        description: description.trim(),
        type,
        location: location.trim(),
        startTime: startDateTimeIso,
        endTime: endDateTimeIso,
        isAllDay,
        color,
        notes: notes.trim(),
      });
    }

    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-200" role="dialog" aria-modal="true" aria-labelledby="event-modal-title">
      <div className="fixed inset-0" onClick={onClose} />

      <div className="relative w-full max-w-xl bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden z-10 p-6 sm:p-8 space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-100 pb-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              {eventToEdit ? <Edit2 className="w-5 h-5" /> : <Plus className="w-5 h-5" />}
            </div>
            <div>
              <h3 id="event-modal-title" className="font-bold text-gray-900 text-lg">
                {eventToEdit ? 'Edit Personal Event' : 'Create Personal Event'}
              </h3>
              <p className="text-xs text-gray-500">
                {eventToEdit ? 'Modify event timing and details' : 'Add a new custom event to your schedule'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 text-gray-400 hover:text-gray-700 rounded-full transition-colors"
            aria-label="Close dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Title */}
          <div>
            <label htmlFor="event-title" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
              Event Title <span className="text-red-500">*</span>
            </label>
            <input
              id="event-title"
              type="text"
              required
              placeholder="e.g. Midterm Group Prep, Gym Session..."
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium text-gray-900 focus:ring-2 focus:ring-blue-500 focus:outline-none transition-all"
            />
          </div>

          {/* Type & Color Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="event-type" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
                Category / Type
              </label>
              <select
                id="event-type"
                value={type}
                onChange={(e) => setType(e.target.value as any)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-semibold text-gray-900 focus:ring-2 focus:ring-blue-500 focus:outline-none transition-all"
              >
                <option value="PERSONAL">Personal</option>
                <option value="ACADEMIC">Academic</option>
                <option value="TASK">Task / Project</option>
                <option value="EVENT">Community / Social</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div>
              <span className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
                Color Badge
              </span>
              <div className="flex items-center gap-2 pt-1">
                {COLOR_PRESETS.map((c) => (
                  <button
                    key={c}
                    type="button"
                    onClick={() => setColor(c)}
                    className={`w-6 h-6 rounded-full transition-transform ${
                      color === c ? 'ring-2 ring-offset-2 ring-blue-600 scale-110' : 'hover:scale-105'
                    }`}
                    style={{ backgroundColor: c }}
                    aria-label={`Color preset ${c}`}
                    aria-pressed={color === c}
                  />
                ))}
              </div>
            </div>
          </div>

          {/* All Day Switch */}
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-2xl border border-gray-200/60">
            <label htmlFor="event-all-day" className="text-xs font-bold text-gray-800 cursor-pointer flex-1">
              All-Day Event
            </label>
            <input
              id="event-all-day"
              type="checkbox"
              checked={isAllDay}
              onChange={(e) => setIsAllDay(e.target.checked)}
              className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
            />
          </div>

          {/* Start Date & Time */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="event-start-date" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
                Start Date
              </label>
              <input
                id="event-start-date"
                type="date"
                required
                value={startDateStr}
                onChange={(e) => setStartDateStr(e.target.value)}
                className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold text-gray-900 focus:ring-2 focus:ring-blue-500"
              />
            </div>
            {!isAllDay && (
              <div>
                <label htmlFor="event-start-time" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
                  Start Time
                </label>
                <input
                  id="event-start-time"
                  type="time"
                  required
                  value={startTimeStr}
                  onChange={(e) => setStartTimeStr(e.target.value)}
                  className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold text-gray-900 focus:ring-2 focus:ring-blue-500"
                />
              </div>
            )}
          </div>

          {/* End Date & Time */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="event-end-date" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
                End Date
              </label>
              <input
                id="event-end-date"
                type="date"
                required
                value={endDateStr}
                onChange={(e) => setEndDateStr(e.target.value)}
                className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold text-gray-900 focus:ring-2 focus:ring-blue-500"
              />
            </div>
            {!isAllDay && (
              <div>
                <label htmlFor="event-end-time" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
                  End Time
                </label>
                <input
                  id="event-end-time"
                  type="time"
                  required
                  value={endTimeStr}
                  onChange={(e) => setEndTimeStr(e.target.value)}
                  className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold text-gray-900 focus:ring-2 focus:ring-blue-500"
                />
              </div>
            )}
          </div>

          {/* Location */}
          <div>
            <label htmlFor="event-location" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
              Location
            </label>
            <input
              id="event-location"
              type="text"
              placeholder="Building, room number, or online meeting link..."
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-900 focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Description */}
          <div>
            <label htmlFor="event-description" className="block text-xs font-bold text-gray-700 uppercase tracking-wider mb-1">
              Description / Notes
            </label>
            <textarea
              id="event-description"
              rows={3}
              placeholder="Add event agenda or details..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs font-medium text-gray-900 focus:ring-2 focus:ring-blue-500 resize-none"
            />
          </div>

          {/* Submit Action */}
          <div className="pt-4 border-t border-gray-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-bold transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || !title.trim()}
              aria-busy={isSubmitting}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold transition-all shadow-md disabled:opacity-50"
            >
              <Save className="w-4 h-4" />
              <span>{isSubmitting ? 'Saving...' : eventToEdit ? 'Update Event' : 'Create Event'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
