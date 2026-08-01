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
  resources: {
    all: ['resources'] as const,
    list: (params?: Record<string, any>) => [...queryKeys.resources.all, 'list', params] as const,
    featured: () => [...queryKeys.resources.all, 'featured'] as const,
    recent: () => [...queryKeys.resources.all, 'recent'] as const,
    popular: () => [...queryKeys.resources.all, 'popular'] as const,
    bookmarked: () => [...queryKeys.resources.all, 'bookmarked'] as const,
    detail: (id: string) => [...queryKeys.resources.all, 'detail', id] as const,
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
    academicCalendar: (term?: string) => [...queryKeys.planner.all, 'academicCalendar', { term }] as const,
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
  communities: {
    all: ['communities'] as const,
    list: (params?: Record<string, any>) => [...queryKeys.communities.all, 'list', params] as const,
    featured: () => [...queryKeys.communities.all, 'featured'] as const,
    trending: () => [...queryKeys.communities.all, 'trending'] as const,
    recentlyActive: () => [...queryKeys.communities.all, 'recentlyActive'] as const,
    joined: () => [...queryKeys.communities.all, 'joined'] as const,
    detail: (id: string) => [...queryKeys.communities.all, 'detail', id] as const,
    feed: (id: string, filter?: string) => [...queryKeys.communities.detail(id), 'feed', { filter }] as const,
    members: (id: string, params?: Record<string, any>) => [...queryKeys.communities.detail(id), 'members', params] as const,
  },
  councils: {
    all: ['councils'] as const,
    list: (params?: Record<string, any>) => [...queryKeys.councils.all, 'list', params] as const,
    featured: () => [...queryKeys.councils.all, 'featured'] as const,
    recentlyActive: () => [...queryKeys.councils.all, 'recentlyActive'] as const,
    joined: () => [...queryKeys.councils.all, 'joined'] as const,
    detail: (id: string) => [...queryKeys.councils.all, 'detail', id] as const,
    leadership: (id: string) => [...queryKeys.councils.detail(id), 'leadership'] as const,
    members: (id: string, params?: Record<string, any>) => [...queryKeys.councils.detail(id), 'members', params] as const,
    events: (id: string) => [...queryKeys.councils.detail(id), 'events'] as const,
    notices: (id: string, filter?: string) => [...queryKeys.councils.detail(id), 'notices', { filter }] as const,
    resources: (id: string, category?: string) => [...queryKeys.councils.detail(id), 'resources', { category }] as const,
  },
  notices: {
    all: ['notices'] as const,
    list: (params?: Record<string, any>) => [...queryKeys.notices.all, 'list', params] as const,
    pinned: () => [...queryKeys.notices.all, 'pinned'] as const,
    recent: () => [...queryKeys.notices.all, 'recent'] as const,
    important: () => [...queryKeys.notices.all, 'important'] as const,
    archived: () => [...queryKeys.notices.all, 'archived'] as const,
    detail: (id: string) => [...queryKeys.notices.all, 'detail', id] as const,
    detailBySlug: (slug: string) => [...queryKeys.notices.all, 'slug', slug] as const,
    readStatus: () => [...queryKeys.notices.all, 'readStatus'] as const,
    unreadCount: () => [...queryKeys.notices.all, 'unreadCount'] as const,
  },
};


