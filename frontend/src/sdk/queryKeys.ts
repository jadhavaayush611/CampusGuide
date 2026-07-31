/**
 * Centralized Type-Safe Query Key Hierarchy for TanStack React Query.
 *
 * Query Key Structure Rules:
 * 1. Root scope key: ['domain']
 * 2. Scope collection: ['domain', 'entity-plural']
 * 3. Specific item: ['domain', 'entity-plural', id]
 * 4. Item sub-resource: ['domain', 'entity-plural', id, 'sub-resource']
 * 5. Filtered collection: ['domain', 'entity-plural', { filter }]
 */
export const queryKeys = {
  auth: {
    all: ['auth'] as const,
    user: () => [...queryKeys.auth.all, 'user'] as const,
    session: () => [...queryKeys.auth.all, 'session'] as const,
  },
  campus: {
    all: ['campus'] as const,
    buildings: () => [...queryKeys.campus.all, 'buildings'] as const,
    building: (id: string) => [...queryKeys.campus.buildings(), id] as const,
    locations: (buildingId?: string) => [...queryKeys.campus.all, 'locations', { buildingId }] as const,
    floorPlans: (buildingId: string) => [...queryKeys.campus.building(buildingId), 'floor-plans'] as const,
    events: () => [...queryKeys.campus.all, 'events'] as const,
    upcomingEvents: () => [...queryKeys.campus.events(), 'upcoming'] as const,
    event: (id: string) => [...queryKeys.campus.events(), id] as const,
    eventStatus: (eventId: string) => [...queryKeys.campus.event(eventId), 'status'] as const,
    councils: () => [...queryKeys.campus.all, 'councils'] as const,
    council: (id: string) => [...queryKeys.campus.councils(), id] as const,
    resources: () => [...queryKeys.campus.all, 'resources'] as const,
    searchResources: (query: string) => [...queryKeys.campus.resources(), 'search', query] as const,
  },
  planner: {
    all: ['planner'] as const,
    schedules: () => [...queryKeys.planner.all, 'schedules'] as const,
    schedule: (id: string) => [...queryKeys.planner.schedules(), id] as const,
    courses: (department?: string) => [...queryKeys.planner.all, 'courses', { department }] as const,
    course: (id: string) => [...queryKeys.planner.all, 'courses', id] as const,
    timetable: (scheduleId?: string) => [...queryKeys.planner.all, 'timetable', { scheduleId }] as const,
    studyGoals: () => [...queryKeys.planner.all, 'studyGoals'] as const,
    degreePlan: () => [...queryKeys.planner.all, 'degreePlan'] as const,
  },
  atlas: {
    all: ['atlas'] as const,
    search: (query: string, category?: string) => [...queryKeys.atlas.all, 'search', { query, category }] as const,
    route: (originLat: number, originLng: number, destLat: number, destLng: number, isAccessible?: boolean) =>
      [...queryKeys.atlas.all, 'route', { originLat, originLng, destLat, destLng, isAccessible }] as const,
    landmarks: (category?: string) => [...queryKeys.atlas.all, 'landmarks', { category }] as const,
    landmark: (id: string) => [...queryKeys.atlas.all, 'landmarks', id] as const,
    mapLayers: () => [...queryKeys.atlas.all, 'mapLayers'] as const,
  },
  notifications: {
    all: ['notifications'] as const,
    list: () => [...queryKeys.notifications.all, 'list'] as const,
    unreadCount: () => [...queryKeys.notifications.all, 'unread-count'] as const,
  },
};
