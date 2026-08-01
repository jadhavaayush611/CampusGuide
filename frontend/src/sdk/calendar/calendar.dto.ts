/**
 * Backend Data Transfer Objects (DTOs) for Calendar API
 */

export interface CalendarEntryDto {
  id: string;
  userId: string;
  title: string;
  description?: string;
  type: 'ACADEMIC' | 'EVENT' | 'TASK' | 'PERSONAL' | 'OTHER';
  linkedPlannerTaskId?: string;
  linkedEventId?: string;
  location?: string;
  startTime: string; // ISO 8601 LocalDateTime
  endTime: string;   // ISO 8601 LocalDateTime
  isAllDay: boolean;
  color?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCalendarEntryDto {
  title: string;
  description?: string;
  type: 'ACADEMIC' | 'EVENT' | 'TASK' | 'PERSONAL' | 'OTHER';
  linkedPlannerTaskId?: string;
  linkedEventId?: string;
  location?: string;
  startTime: string;
  endTime: string;
  isAllDay?: boolean;
  color?: string;
  notes?: string;
}

export interface UpdateCalendarEntryDto {
  title?: string;
  description?: string;
  type?: 'ACADEMIC' | 'EVENT' | 'TASK' | 'PERSONAL' | 'OTHER';
  linkedPlannerTaskId?: string;
  linkedEventId?: string;
  location?: string;
  startTime?: string;
  endTime?: string;
  isAllDay?: boolean;
  color?: string;
  notes?: string;
}
