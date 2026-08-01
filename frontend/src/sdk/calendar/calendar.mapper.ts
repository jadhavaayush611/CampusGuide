import { CalendarEntryDto } from './calendar.dto';
import { CalendarEntry, AggregatedCalendarEvent } from '../../models/calendar.model';

export function mapCalendarEntryDtoToModel(dto: CalendarEntryDto): CalendarEntry {
  return {
    id: dto.id,
    userId: dto.userId,
    title: dto.title,
    description: dto.description,
    type: dto.type,
    linkedPlannerTaskId: dto.linkedPlannerTaskId,
    linkedEventId: dto.linkedEventId,
    location: dto.location,
    startTime: dto.startTime,
    endTime: dto.endTime,
    isAllDay: dto.isAllDay,
    color: dto.color,
    notes: dto.notes,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

export function mapCalendarEntryToAggregatedEvent(entry: CalendarEntry): AggregatedCalendarEvent {
  const startDate = new Date(entry.startTime);
  const endDate = new Date(entry.endTime);

  let category: AggregatedCalendarEvent['category'] = 'PERSONAL';
  if (entry.type === 'ACADEMIC') category = 'ACADEMIC';
  else if (entry.type === 'TASK') category = 'TASK';
  else if (entry.type === 'EVENT') category = 'COMMUNITY';

  return {
    id: `personal-${entry.id}`,
    originalId: entry.id,
    title: entry.title,
    description: entry.description,
    sourceModule: 'personal',
    category,
    location: entry.location,
    startTime: isNaN(startDate.getTime()) ? new Date() : startDate,
    endTime: isNaN(endDate.getTime()) ? new Date() : endDate,
    isAllDay: entry.isAllDay,
    color: entry.color || '#2563EB',
    rawEntity: entry,
  };
}
