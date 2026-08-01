# CampusGuide Frontend Organization & Quality Standards

This document serves as the authoritative specification for the CampusGuide React frontend repository organization, folder conventions, naming standards, module ownership boundaries, and quality requirements.

---

## 1. Repository Organization

The frontend codebase is located under `frontend/src/` and is organized into decoupled layers:

```
frontend/src/
├── app/                  # Application Shell & Presentation Layer
│   ├── components/       # Domain-grouped UI components and reusable design primitives
│   │   ├── academic/     # Academic domain UI components
│   │   ├── atlas/        # Atlas AI workflow orchestrator UI components
│   │   ├── calendar/     # Scheduling & calendar UI components
│   │   ├── communities/  # Student communities UI components
│   │   ├── councils/     # Student councils UI components
│   │   ├── dashboard/    # Aggregator widget components
│   │   ├── notices/      # Announcement & notice board UI components
│   │   ├── notifications/ # Notification lifecycle UI components
│   │   ├── planner/      # Productivity & task tracking UI components
│   │   ├── resources/    # Document management UI components
│   │   └── ui/           # Base primitives (shadcn / Radix / Tailwind primitives)
│   ├── pages/            # Top-level route pages
│   ├── App.tsx           # Application entrypoint wrapping AppProviders and RouterProvider
│   └── routes.tsx        # React Router configuration & route guard definitions
├── core/                 # Infrastructure & Core Services Layer
│   ├── api/              # HTTP client, interceptors, error handlers
│   ├── auth/             # Session management, JWT storage, AuthContext
│   ├── config/           # Environment configuration abstraction
│   ├── errors/           # Error hierarchy & global ErrorBoundary
│   ├── loading/          # Global loading indicators & overlays
│   ├── providers/        # Root provider hierarchy composition
│   ├── routing/          # ProtectedRoute & PublicRoute route guards
│   ├── storage/          # Storage abstraction (LocalStorage/SessionStorage)
│   ├── toast/            # Toast notification wrapper (Sonner)
│   └── utils/            # Shared utilities (logger, helpers)
├── hooks/                # Data Fetching & React Query Custom Hooks
│   ├── academic/         # Course catalog, timetable, degree progress hooks
│   ├── atlas/            # Spatial search, route calculation, stream hooks
│   ├── auth/             # Login, register, logout, profile hooks
│   ├── calendar/         # Calendar entry CRUD & range hooks
│   ├── campus/           # Facility, building, & event hooks
│   ├── common/           # Generic optimistic mutation & utility hooks
│   ├── community/        # Community & feed hooks
│   ├── council/          # Council & leadership hooks
│   ├── notices/          # Notice board & pin/read hooks
│   ├── notifications/    # Notification lifecycle & stats hooks
│   ├── planner/          # Task & study goal hooks
│   └── resources/        # Document repository & upload/bookmark hooks
├── models/               # Strongly Typed Frontend UI Data Models
│   ├── academic.model.ts
│   ├── atlas.model.ts
│   ├── auth.model.ts
│   ├── calendar.model.ts
│   ├── campus.model.ts
│   ├── community.model.ts
│   ├── council.model.ts
│   ├── notice.model.ts
│   ├── notification.model.ts
│   ├── planner.model.ts
│   └── resource.model.ts
├── sdk/                  # Decoupled Domain SDK Layer
│   ├── academic/         # Academic DTOs, mappers, AcademicSdk
│   ├── atlas/            # Atlas DTOs, mappers, AtlasSdk
│   ├── auth/             # Auth DTOs, mappers, AuthSdk
│   ├── calendar/         # Calendar DTOs, mappers, CalendarSdk
│   ├── campus/           # Campus DTOs, mappers, CampusSdk
│   ├── common/           # BaseSdk, SdkError, common response types
│   ├── community/        # Community DTOs, mappers, CommunitySdk
│   ├── council/          # Council DTOs, mappers, CouncilSdk
│   ├── notices/          # Notice DTOs, mappers, NoticeSdk
│   ├── notifications/    # Notification DTOs, mappers, NotificationSdk
│   ├── planner/          # Planner DTOs, mappers, PlannerSdk
│   ├── resources/        # Resource DTOs, mappers, ResourceSdk
│   ├── atlasClientInstance.ts # Production AtlasClient singleton
│   ├── queryKeys.ts      # Centralized React Query key registry
│   └── index.ts          # Unified SDK export entrypoint
├── styles/               # Global CSS & Tailwind design tokens
└── main.tsx              # Vite application DOM root mount
```

---

## 2. Folder Conventions

1. **Domain Grouping**: Components, hooks, and SDK modules must be grouped by domain directory (`academic`, `atlas`, `calendar`, `community`, `council`, `dashboard`, `notices`, `notifications`, `planner`, `resources`).
2. **Page Layer Scoping**: Route pages reside exclusively in `src/app/pages/`. Pages do not contain raw API calls or state logic; they delegate to domain hooks and render domain components.
3. **Core Infrastructure**: All cross-cutting concerns (authentication, HTTP client, environment variables, storage, toast notifications) reside strictly inside `src/core/`.
4. **Base UI Primitives**: Reusable, unstyled/design-system primitive components reside in `src/app/components/ui/`.

---

## 3. Naming Conventions

| Artifact Category | Format Pattern | Example |
| :--- | :--- | :--- |
| **Pages** | PascalCase ending with `Page` or descriptive domain noun | `AtlasPage.tsx`, `Dashboard.tsx`, `PlannerPage.tsx` |
| **Components** | PascalCase noun describing UI role | `Header.tsx`, `CalendarHeader.tsx`, `NoticeCard.tsx` |
| **Custom Hooks** | camelCase starting with `use` | `useNotifications.ts`, `useAtlasConversations.ts` |
| **Domain SDKs** | PascalCase class ending with `Sdk` | `AtlasSdk.ts`, `PlannerSdk.ts`, `CalendarSdk.ts` |
| **DTO Files** | `domain.dto.ts` | `planner.dto.ts`, `notice.dto.ts` |
| **Mapper Files** | `domain.mapper.ts` | `planner.mapper.ts`, `notice.mapper.ts` |
| **Model Files** | `domain.model.ts` | `planner.model.ts`, `notice.model.ts` |
| **Query Keys** | camelCase object namespace inside `queryKeys.ts` | `queryKeys.planner.tasks()` |

---

## 4. Module Ownership Boundaries

To prevent architectural drift, the following ownership invariants are strictly enforced:

1. **Dashboard (`/`)**:
   - **Role**: Top-level orchestrator and aggregator.
   - **Constraint**: Holds zero business logic or CRUD mutations; delegates widget rendering and deep-links to owning modules.
2. **Atlas (`/atlas`)**:
   - **Role**: Campus workflow orchestrator.
   - **Constraint**: Manages multi-step AI reasoning and deep-linking via `CampusResultCard`. Does NOT duplicate or override owning domain business logic.
3. **Calendar (`/calendar`)**:
   - **Exclusive Owner**: All scheduling, time slots, month/week/day agenda views, and time conflict detection.
4. **Resources (`/resources`)**:
   - **Exclusive Owner**: Document repository browsing, search, upload, download, and bookmarking.
5. **Planner (`/planner`)**:
   - **Exclusive Owner**: Personal tasks, priorities, progress tracking, archiving, and study goal management.
6. **Academic (`/academic`)**:
   - **Exclusive Owner**: Course catalog, student weekly timetable schedules, degree audit progress, and GPA tracking.
7. **Notifications (`/notifications`)**:
   - **Exclusive Owner**: Notification lifecycle, read/unread state toggles, delivery status, and notification stats.
8. **Communities vs. Councils**:
   - **Communities (`/communities`)**: Interest-driven student clubs, discussion feeds, and social interaction.
   - **Councils (`/councils`)**: Formal student government, departmental councils, policy notices, and leadership governance.

---

## 5. Repository Quality Standards

1. **Zero Temporary Markers**: Codebase must contain zero `TODO`, `FIXME`, `HACK`, `TEMP`, or `XXX` comments.
2. **Zero Unreferenced / Orphaned Files**: All pages must be registered in `routes.tsx` or linked in navigation. Unused components, prototype files, or obsolete folders are strictly removed.
3. **Strict Type Safety**: TypeScript must pass clean execution (`npm run typecheck`) with zero type errors.
4. **SDK-Only API Communication**: Components and pages must communicate exclusively via typed domain SDKs (`src/sdk/`) and React Query hooks (`src/hooks/`). Direct `fetch` or `axios` calls outside `src/core/api` or `src/sdk/` are prohibited.
5. **UI & UX State Consistency**:
   - Every data-fetching component must handle Loading Skeletons (`*Skeleton.tsx`), Empty States (`*EmptyState.tsx`), and Error Boundaries (`*ErrorBoundary.tsx`).
   - Mutations must provide optimistic updates or Toast notifications on success/failure.
