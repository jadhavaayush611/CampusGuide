import { BaseSdk } from '../common/BaseSdk';
import { CalendarEntryDto, CreateCalendarEntryDto, UpdateCalendarEntryDto } from './calendar.dto';
import { mapCalendarEntryDtoToModel } from './calendar.mapper';
import { CalendarEntry, CreateCalendarEntryPayload, UpdateCalendarEntryPayload } from '../../models/calendar.model';

/**
 * Production Calendar SDK connecting directly to /api/v1/calendar
 */
export class CalendarSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/calendar';

  public async getEntries(): Promise<CalendarEntry[]> {
    const dtos = await this.get<CalendarEntryDto[]>(this.baseUrl);
    return (dtos || []).map(mapCalendarEntryDtoToModel);
  }

  public async getEntriesInRange(from: string, to: string): Promise<CalendarEntry[]> {
    const dtos = await this.get<CalendarEntryDto[]>(`${this.baseUrl}/range`, { from, to });
    return (dtos || []).map(mapCalendarEntryDtoToModel);
  }

  public async getEntryById(id: string): Promise<CalendarEntry> {
    const dto = await this.get<CalendarEntryDto>(`${this.baseUrl}/${id}`);
    return mapCalendarEntryDtoToModel(dto);
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

    const dto = await this.post<CalendarEntryDto>(this.baseUrl, requestDto);
    return mapCalendarEntryDtoToModel(dto);
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

    const dto = await this.put<CalendarEntryDto>(`${this.baseUrl}/${id}`, requestDto);
    return mapCalendarEntryDtoToModel(dto);
  }

  public async deleteEntry(id: string): Promise<void> {
    await this.delete<void>(`${this.baseUrl}/${id}`);
  }
}

export const calendarSdk = new CalendarSdk();
