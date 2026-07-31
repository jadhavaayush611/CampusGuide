# Production Dashboard Architecture & Integration

## Overview

The CampusGuide Production Dashboard (`src/app/pages/Dashboard.tsx`) serves as the main command center for students and faculty. It integrates user identity, academic summary, planner, campus activities, spatial wayfinding quick actions, and real-time notification alerts.

All data strictly flows through established domain SDKs, typed models, and TanStack React Query server-state hooks. Direct API communication outside of SDKs is strictly prohibited.

---

## Widget Composition

The dashboard is composed of modular, self-contained widgets located in `src/app/components/dashboard/`:

```
Dashboard Page (src/app/pages/Dashboard.tsx)
│
├── UserOverviewWidget (src/app/components/dashboard/UserOverviewWidget.tsx)
│   └── Consumes useCurrentUser()
│
├── AcademicSummaryWidget (src/app/components/dashboard/AcademicSummaryWidget.tsx)
│   ├── Consumes useCourses()
│   ├── Consumes useTimetable()
│   └── Consumes useDegreePlan()
│
├── NotificationsWidget (src/app/components/dashboard/NotificationsWidget.tsx)
│   ├── Consumes useNotifications()
│   └── Consumes useUnreadNotificationCount()
│
├── PlannerWidget (src/app/components/dashboard/PlannerWidget.tsx)
│   ├── Consumes useTimetable()
│   ├── Consumes useStudyGoals()
│   ├── Consumes useDegreePlan()
│   └── Consumes useCreateStudyGoal()
│
├── CampusActivityWidget (src/app/components/dashboard/CampusActivityWidget.tsx)
│   ├── Consumes useCampusEvents(upcomingOnly=true)
│   ├── Consumes useCouncils()
│   └── Consumes useResources()
│
└── AtlasWidget (src/app/components/dashboard/AtlasWidget.tsx)
    ├── Consumes useAtlasSearch()
    ├── Consumes useRouteCalculation()
    └── Consumes useBuildings()
```

### Widget Isolation & Error Boundary Protection

Every widget on the dashboard is wrapped in an individual `WidgetErrorBoundary` (`src/app/components/dashboard/WidgetErrorBoundary.tsx`). If any single backend query or widget render throws an exception, the failure is caught locally and renders a clean, isolated error state with a retry option without affecting the rest of the dashboard layout.

---

## Data Flow Architecture

Data flow follows a unidirectional pipeline:

```
[ Backend REST APIs / Mock Fallbacks ]
               │
               ▼
[ BaseSdk / Domain SDKs (AuthSdk, PlannerSdk, CampusSdk, AtlasSdk, NotificationSdk) ]
               │
               ▼
[ Domain Mappers & Type-Safe Models (auth.model, planner.model, campus.model, atlas.model, notification.model) ]
               │
               ▼
[ TanStack React Query Hooks (useCurrentUser, useCourses, useTimetable, useNotifications, etc.) ]
               │
               ▼
[ Reusable Dashboard Widgets (UserOverview, AcademicSummary, Planner, Activity, Atlas, Notifications) ]
```

---

## Loading Strategy

1. **Parallel Query Execution**: React Query hooks execute concurrently when individual widgets mount, preventing network waterfalls.
2. **Skeleton Screen UX**: Each widget implements a layout-matched loading skeleton while queries fetch data.
3. **Background Refetching**: If cached data is stale, stale-while-revalidate displays cached data instantly while refreshing in the background.

---

## Caching & Performance Strategy

1. **Cache Reuse Across Views**: Queries share domain keys defined in `src/sdk/queryKeys.ts`. Navigating between the dashboard, course view, and council pages reuses cached server state.
2. **Stale Time Configuration**:
   - `useCurrentUser`: 10 minutes (user identity rarely changes mid-session).
   - `useCourses` & `useDegreePlan`: 10 minutes.
   - `useCampusEvents` & `useCouncils`: 2 minutes.
   - `useNotifications`: 1 minute.
   - `useUnreadNotificationCount`: 30 seconds.
3. **Optimistic Updates**: Goal creation (`useCreateStudyGoal`) uses optimistic cache mutation to update the UI instantly.

---

## Query Key Hierarchy (`src/sdk/queryKeys.ts`)

Dashboard queries consume centralized query key factories:

```typescript
export const queryKeys = {
  auth: { user: () => ['auth', 'user'] },
  campus: {
    events: () => ['campus', 'events'],
    upcomingEvents: () => ['campus', 'events', 'upcoming'],
    councils: () => ['campus', 'councils'],
    resources: () => ['campus', 'resources'],
    buildings: () => ['campus', 'buildings'],
  },
  planner: {
    courses: (department?: string) => ['planner', 'courses', { department }],
    timetable: (scheduleId?: string) => ['planner', 'timetable', { scheduleId }],
    studyGoals: () => ['planner', 'studyGoals'],
    degreePlan: () => ['planner', 'degreePlan'],
  },
  atlas: {
    search: (query: string, category?: string) => ['atlas', 'search', { query, category }],
    route: (originLat: number, originLng: number, destLat: number, destLng: number) =>
      ['atlas', 'route', { originLat, originLng, destLat, destLng }],
  },
  notifications: {
    list: () => ['notifications', 'list'],
    unreadCount: () => ['notifications', 'unread-count'],
  },
};
```

---

## Verification & Build Standards

The dashboard code is verified using:
- `npm run typecheck` (`tsc --noEmit`) - 0 errors
- `npm run build` (`vite build`) - Clean bundle output
