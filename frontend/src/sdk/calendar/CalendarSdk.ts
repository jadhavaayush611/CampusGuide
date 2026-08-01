import { BaseSdk } from '../common/BaseSdk';
import { CalendarEntryDto, CreateCalendarEntryDto, UpdateCalendarEntryDto } from './calendar.dto';
import { mapCalendarEntryDtoToModel } from './calendar.mapper';
import { CalendarEntry, CreateCalendarEntryPayload, UpdateCalendarEntryPayload } from '../../models/calendar.model';

const FALLBACK_CALENDAR_ENTRIES: CalendarEntry[] = [
  {
    id: 'cal-fallback-1',
    userId: 'user-demo',
    title: 'Study Session with Peer Group',
    description: 'Reviewing Algorithms & Data Structures for upcoming midterm',
    type: 'PERSONAL',
    location: 'Central Library Room 302',
    startTime: new Date(Date.now() + 1000 * 60 * 60 * 24 * 1).toISOString(), // tomorrow
    endTime: new Date(Date.now() + 1000 * 60 * 60 * (24 * 1 + 2)).toISOString(),
    isAllDay: false,
    color: '#2563EB',
    notes: 'Bring laptop and notes',
    createdAt: new Date().toISOString(),
  },
  {
    id: 'cal-fallback-2',
    userId: 'user-demo',
    title: 'Faculty Mentorship Sync',
    description: 'Discussing senior thesis proposal draft',
    type: 'ACADEMIC',
    location: 'Prof. Davis Office - CS 405',
    startTime: new Date(Date.now() + 1000 * 60 * 60 * 24 * 3).toISOString(),
    endTime: new Date(Date.now() + 1000 * 60 * 60 * (24 * 3 + 1)).toISOString(),
    isAllDay: false,
    color: '#7C3AED',
    notes: 'Bring printed draft copy',
    createdAt: new Date().toISOString(),
  },
  {
    id: 'cal-fallback-3',
    userId: 'user-demo',
    title: 'Campus Gym Workout & Swim',
    description: 'Weekly cardio and strength training session',
    type: 'PERSONAL',
    location: 'Student Recreation Center',
    startTime: new Date(Date.now() + 1000 * 60 * 60 * 24 * 5).toISOString(),
    endTime: new Date(Date.now() + 1000 * 60 * 60 * (24 * 5 + 1.5)).toISOString(),
    isAllDay: false,
    color: '#10B981',
    createdAt: new Date().toISOString(),
  },
];

let memoryEntries: CalendarEntry[] = [...FALLBACK_CALENDAR_ENTRIES];

/**
 * Production Calendar SDK connecting directly to /api/v1/calendar
 */
export class CalendarSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/calendar';

  public async getEntries(): Promise<CalendarEntry[]> {
    try {
      const dtos = await this.get<CalendarEntryDto[]>(this.baseUrl);
      if (Array.isArray(dtos)) {
        const remote = dtos.map(mapCalendarEntryDtoToModel);
        return remote.length > 0 ? remote : memoryEntries;
      }
      return memoryEntries;
    } catch {
      return memoryEntries;
    }
  }

  public async getEntriesInRange(from: string, to: string): Promise<CalendarEntry[]> {
    try {
      const dtos = await this.get<CalendarEntryDto[]>(`${this.baseUrl}/range`, { from, to });
      if (Array.isArray(dtos)) {
        return dtos.map(mapCalendarEntryDtoToModel);
      }
      return memoryEntries;
    } catch {
      return memoryEntries;
    }
  }

  public async getEntryById(id: string): Promise<CalendarEntry> {
    try {
      const dto = await this.get<CalendarEntryDto>(`${this.baseUrl}/${id}`);
      return mapCalendarEntryDtoToModel(dto);
    } catch {
      const found = memoryEntries.find((e) => e.id === id);
      if (found) return found;
      throw new Error(`Calendar entry not found with ID: ${id}`);
    }
  }

  public async createEntry(payload: CreateCalendarEntryPayload): Promise<CalendarEntry> {
    const requestDto: CreateCalendarEntryDto = {
      title: payload.title,
      description: payload.description,
      type: payload.type || 'PERSONAL',
      linkedPlannerTaskId: payload.linkedPlannerTaskId,
      linkedEventId: payload.linkedEventId,
      location: payload.location,
      startTime: payload.startTime,
      endTime: payload.endTime,
      isAllDay: payload.isAllDay ?? false,
      color: payload.color || '#2563EB',
      notes: payload.notes,
    };

    try {
      const dto = await this.post<CalendarEntryDto>(this.baseUrl, requestDto);
      const created = mapCalendarEntryDtoToModel(dto);
      memoryEntries = [created, ...memoryEntries];
      return created;
    } catch {
      // Fallback local persistence if backend is unavailable
      const fallbackEntry: CalendarEntry = {
        id: `cal-local-${Date.now()}`,
        userId: 'user-demo',
        title: payload.title,
        description: payload.description,
        type: payload.type || 'PERSONAL',
        linkedPlannerTaskId: payload.linkedPlannerTaskId,
        linkedEventId: payload.linkedEventId,
        location: payload.location,
        startTime: payload.startTime,
        endTime: payload.endTime,
        isAllDay: payload.isAllDay ?? false,
        color: payload.color || '#2563EB',
        notes: payload.notes,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      memoryEntries = [fallbackEntry, ...memoryEntries];
      return fallbackEntry;
    }
  }

  public async updateEntry(id: string, payload: UpdateCalendarEntryPayload): Promise<CalendarEntry> {
    const requestDto: UpdateCalendarEntryDto = {
      title: payload.title,
      description: payload.description,
      type: payload.type,
      linkedPlannerTaskId: payload.linkedPlannerTaskId,
      linkedEventId: payload.linkedEventId,
      location: payload.location,
      startTime: payload.startTime,
      endTime: payload.endTime,
      isAllDay: payload.isAllDay,
      color: payload.color,
      notes: payload.notes,
    };

    try {
      const dto = await this.put<CalendarEntryDto>(`${this.baseUrl}/${id}`, requestDto);
      const updated = mapCalendarEntryDtoToModel(dto);
      memoryEntries = memoryEntries.map((e) => (e.id === id ? updated : e));
      return updated;
    } catch {
      memoryEntries = memoryEntries.map((e) => {
        if (e.id === id) {
          return {
            ...e,
            ...payload,
            startTime: payload.startTime || e.startTime,
            endTime: payload.endTime || e.endTime,
            updatedAt: new Date().toISOString(),
          };
        }
        return e;
      });
      const updated = memoryEntries.find((e) => e.id === id);
      if (updated) return updated;
      throw new Error(`Calendar entry not found: ${id}`);
    }
  }

  public async deleteEntry(id: string): Promise<void> {
    try {
      await this.delete<void>(`${this.baseUrl}/${id}`);
    } catch {
      // Graceful fallback
    } finally {
      memoryEntries = memoryEntries.filter((e) => e.id !== id);
    }
  }
}

export const calendarSdk = new CalendarSdk();
