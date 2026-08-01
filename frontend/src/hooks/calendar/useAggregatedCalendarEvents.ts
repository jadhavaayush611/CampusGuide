import { useMemo } from 'react';
import { useCalendarEntries } from './useCalendarEntries';
import { useTasks } from '../planner/useTasks';
import { useStudyGoals } from '../planner/useStudyGoals';
import { useAcademicCalendar } from '../planner/useAcademicCalendar';
import { useQuery } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { useScheduledNotifications } from '../notifications/useScheduledNotifications';
import {
  AggregatedCalendarEvent,
  CalendarFilterState,
  CalendarEventCategory,
  CalendarEventSourceModule,
} from '../../models/calendar.model';

export function useAggregatedCalendarEvents(filterState?: Partial<CalendarFilterState>) {
  const { data: personalEntries = [], isLoading: loadingPersonal, error: errorPersonal } = useCalendarEntries();
  const { data: taskResponse, isLoading: loadingTasks, error: errorTasks } = useTasks({ pageSize: 100 });
  const { data: studyGoals = [], isLoading: loadingGoals, error: errorGoals } = useStudyGoals();
  const { data: academicItems = [], isLoading: loadingAcademic, error: errorAcademic } = useAcademicCalendar();

  const { data: campusEvents = [], isLoading: loadingCampus, error: errorCampus } = useQuery({
    queryKey: queryKeys.campus.upcomingEvents(),
    queryFn: () => campusSdk.getUpcomingEvents(),
    staleTime: 5 * 60 * 1000,
  });

  const { data: scheduledReminders = [], isLoading: loadingReminders, error: errorReminders } = useScheduledNotifications();

  const isLoading = loadingPersonal || loadingTasks || loadingGoals || loadingAcademic || loadingCampus || loadingReminders;
  const isError = Boolean(errorPersonal || errorTasks || errorGoals || errorAcademic || errorCampus || errorReminders);

  const aggregatedEvents = useMemo(() => {
    const events: AggregatedCalendarEvent[] = [];

    // 1. Personal Calendar Entries
    personalEntries.forEach((entry) => {
      const s = new Date(entry.startTime);
      const e = new Date(entry.endTime);

      let category: CalendarEventCategory = 'PERSONAL';
      if (entry.type === 'ACADEMIC') category = 'ACADEMIC';
      else if (entry.type === 'TASK') category = 'TASK';
      else if (entry.type === 'EVENT') category = 'COMMUNITY';

      events.push({
        id: `personal-${entry.id}`,
        originalId: entry.id,
        title: entry.title,
        description: entry.description,
        sourceModule: 'personal',
        category,
        location: entry.location,
        startTime: isNaN(s.getTime()) ? new Date() : s,
        endTime: isNaN(e.getTime()) ? new Date(s.getTime() + 60 * 60 * 1000) : e,
        isAllDay: Boolean(entry.isAllDay),
        color: entry.color || '#2563EB',
        linkUrl: `/calendar?eventId=${entry.id}`,
        rawEntity: entry,
      });
    });

    // 2. Planner Tasks & Deadlines
    const tasks = taskResponse?.tasks || [];
    tasks.forEach((task) => {
      if (!task.dueDate || task.isArchived) return;

      const datePart = task.dueDate.split('T')[0];
      const s = task.dueDate.includes('T') ? new Date(task.dueDate) : new Date(`${datePart}T09:00:00`);
      const e = new Date(s.getTime() + 60 * 60 * 1000);

      let cat: CalendarEventCategory = 'TASK';
      if (task.category === 'ASSIGNMENT' || task.category === 'PROJECT') cat = 'TASK';
      else if (task.category === 'EXAMINATION') cat = 'EXAM';
      else if (task.category === 'ACADEMIC') cat = 'ACADEMIC';
      else if (task.category === 'REMINDER') cat = 'REMINDER';

      const color =
        task.priority === 'URGENT'
          ? '#EF4444'
          : task.priority === 'HIGH'
          ? '#F97316'
          : task.category === 'EXAMINATION'
          ? '#8B5CF6'
          : '#F59E0B';

      events.push({
        id: `planner-${task.id}`,
        originalId: task.id,
        title: task.title,
        description: task.description,
        sourceModule: 'planner',
        category: cat,
        startTime: isNaN(s.getTime()) ? new Date() : s,
        endTime: isNaN(e.getTime()) ? new Date() : e,
        isAllDay: !task.dueDate.includes('T'),
        color,
        isCompleted: task.isCompleted,
        priority: task.priority,
        linkUrl: '/planner',
        attachments: task.attachments?.map((a) => ({ name: a.name, url: a.url, size: a.size })),
        rawEntity: task,
      });
    });

    // 3. Study Goals
    studyGoals.forEach((goal) => {
      if (!goal.deadline) return;

      const datePart = goal.deadline.split('T')[0];
      const s = goal.deadline.includes('T') ? new Date(goal.deadline) : new Date(`${datePart}T10:00:00`);
      const e = new Date(s.getTime() + 90 * 60 * 1000);

      events.push({
        id: `goal-${goal.id}`,
        originalId: goal.id,
        title: `Goal: ${goal.title}`,
        description: goal.description
          ? `${goal.description} (${goal.completedHours}/${goal.targetHours} hrs logged)`
          : `Target: ${goal.targetHours} hrs study goal`,
        sourceModule: 'study_goals',
        category: 'GOAL',
        startTime: isNaN(s.getTime()) ? new Date() : s,
        endTime: isNaN(e.getTime()) ? new Date() : e,
        isAllDay: !goal.deadline.includes('T'),
        color: '#10B981',
        isCompleted: goal.isCompleted,
        linkUrl: '/planner',
        rawEntity: goal,
      });
    });

    // 4. Academic Calendar Items & Semester Milestones
    academicItems.forEach((item) => {
      const s = new Date(item.date);
      const e = item.endDate ? new Date(item.endDate) : new Date(s.getTime() + 24 * 60 * 60 * 1000);

      const isMilestone = item.category === 'MILESTONE';
      const sourceModule: CalendarEventSourceModule = isMilestone ? 'milestone' : 'academic';
      const category: CalendarEventCategory = isMilestone
        ? 'MILESTONE'
        : item.category === 'EXAM'
        ? 'EXAM'
        : 'ACADEMIC';

      const color = isMilestone ? '#EC4899' : item.category === 'EXAM' ? '#DC2626' : '#8B5CF6';

      events.push({
        id: `academic-${item.id}`,
        originalId: item.id,
        title: item.title,
        description: item.description || `Term: ${item.term || 'Current'}`,
        sourceModule,
        category,
        startTime: isNaN(s.getTime()) ? new Date() : s,
        endTime: isNaN(e.getTime()) ? new Date() : e,
        isAllDay: true,
        color,
        linkUrl: '/academic',
        rawEntity: item,
      });
    });

    // 5. Council & Community Events
    campusEvents.forEach((ev) => {
      const s = new Date(ev.startTime);
      const e = new Date(ev.endTime);
      const isCouncil = Boolean(ev.councilId);

      events.push({
        id: `campus-${ev.id}`,
        originalId: ev.id,
        title: ev.title,
        description: ev.description,
        sourceModule: isCouncil ? 'council' : 'community',
        category: isCouncil ? 'COUNCIL' : 'COMMUNITY',
        location: ev.location,
        startTime: isNaN(s.getTime()) ? new Date() : s,
        endTime: isNaN(e.getTime()) ? new Date(s.getTime() + 2 * 60 * 60 * 1000) : e,
        isAllDay: false,
        color: isCouncil ? '#3B82F6' : '#6366F1',
        linkUrl: isCouncil ? `/councils/${ev.councilId}` : '/councils',
        participants: ev.attendeeCount ? [{ id: 'count', name: `${ev.attendeeCount} Attendees` }] : undefined,
        rawEntity: ev,
      });
    });

    // 6. Reminder Schedule
    scheduledReminders.forEach((rem) => {
      const s = new Date(rem.scheduledTime);
      const e = new Date(s.getTime() + 30 * 60 * 1000);

      events.push({
        id: `reminder-${rem.id}`,
        originalId: rem.id,
        title: rem.title,
        description: rem.message,
        sourceModule: 'reminder',
        category: 'REMINDER',
        startTime: isNaN(s.getTime()) ? new Date() : s,
        endTime: isNaN(e.getTime()) ? new Date() : e,
        isAllDay: false,
        color: '#F43F5E',
        linkUrl: '/profile',
        rawEntity: rem,
      });
    });

    // Conflict detection across non-all-day events
    const timedEvents = events.filter((e) => !e.isAllDay);
    timedEvents.forEach((evA) => {
      const overlaps = timedEvents.filter(
        (evB) => evA.id !== evB.id && evA.startTime < evB.endTime && evA.endTime > evB.startTime
      );

      if (overlaps.length > 0) {
        evA.hasConflict = true;
        evA.overlappingEventIds = overlaps.map((o) => o.id);
      }
    });

    return events;
  }, [personalEntries, taskResponse, studyGoals, academicItems, campusEvents, scheduledReminders]);

  // Apply filters
  const filteredEvents = useMemo(() => {
    if (!filterState) return aggregatedEvents;

    return aggregatedEvents.filter((ev) => {
      // 1. Search Query
      if (filterState.search) {
        const q = filterState.search.toLowerCase();
        const titleMatch = ev.title.toLowerCase().includes(q);
        const descMatch = ev.description?.toLowerCase().includes(q);
        const locMatch = ev.location?.toLowerCase().includes(q);
        if (!titleMatch && !descMatch && !locMatch) return false;
      }

      // 2. Source Module
      if (
        filterState.selectedModules &&
        filterState.selectedModules.length > 0 &&
        !filterState.selectedModules.includes(ev.sourceModule)
      ) {
        return false;
      }

      // 3. Category
      if (
        filterState.selectedCategories &&
        filterState.selectedCategories.length > 0 &&
        !filterState.selectedCategories.includes(ev.category)
      ) {
        return false;
      }

      // 4. Completed status
      if (!filterState.showCompleted && ev.isCompleted) {
        return false;
      }

      // 5. Shared only
      if (filterState.showSharedOnly && ev.sourceModule === 'personal') {
        return false;
      }

      return true;
    });
  }, [aggregatedEvents, filterState]);

  return {
    events: filteredEvents,
    allEvents: aggregatedEvents,
    isLoading,
    isError,
  };
}
