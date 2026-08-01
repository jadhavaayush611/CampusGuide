/**
 * UI Domain Models for the Calendar Module
 */

export type CalendarViewMode = 'month' | 'week' | 'day' | 'agenda';

export type CalendarEventSourceModule =
  | 'personal'
  | 'planner'
  | 'study_goals'
  | 'academic'
  | 'council'
  | 'community'
  | 'reminder'
  | 'milestone';

export type CalendarEventCategory =
  | 'ACADEMIC'
  | 'PERSONAL'
  | 'TASK'
  | 'EXAM'
  | 'GOAL'
  | 'COUNCIL'
  | 'COMMUNITY'
  | 'REMINDER'
  | 'MILESTONE'
  | 'OTHER';

export interface CalendarParticipant {
  id: string;
  name: string;
  role?: string;
  avatarUrl?: string;
}

export interface CalendarAttachment {
  id?: string;
  name: string;
  url: string;
  size?: string;
  type?: string;
}

export interface CalendarEntry {
  id: string;
  userId: string;
  title: string;
  description?: string;
  type: 'ACADEMIC' | 'EVENT' | 'TASK' | 'PERSONAL' | 'OTHER';
  linkedPlannerTaskId?: string;
  linkedEventId?: string;
  location?: string;
  startTime: string; // ISO String
  endTime: string;   // ISO String
  isAllDay: boolean;
  color?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCalendarEntryPayload {
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

export interface UpdateCalendarEntryPayload {
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

export interface AggregatedCalendarEvent {
  id: string;
  originalId: string;
  title: string;
  description?: string;
  sourceModule: CalendarEventSourceModule;
  category: CalendarEventCategory;
  location?: string;
  startTime: Date;
  endTime: Date;
  isAllDay: boolean;
  color: string;
  recurrence?: string;
  participants?: CalendarParticipant[];
  attachments?: CalendarAttachment[];
  isCompleted?: boolean;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: string;
  isShared?: boolean;
  linkUrl?: string;
  rawEntity?: unknown;
  hasConflict?: boolean;
  overlappingEventIds?: string[];
}

export interface CalendarFilterState {
  search: string;
  selectedModules: CalendarEventSourceModule[];
  selectedCategories: CalendarEventCategory[];
  showCompleted: boolean;
  showSharedOnly: boolean;
  viewMode: CalendarViewMode;
  currentDate: Date;
}
