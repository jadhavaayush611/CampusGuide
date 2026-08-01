# Frontend SDK Architecture

## Overview

CampusGuide enforces a strict architectural boundary: **Feature pages and components must never communicate directly with the API client or execute direct `fetch` calls.** All backend interaction must pass through the production SDK layer located at `src/sdk/`.

The SDK layer encapsulates endpoint URLs, manages request payload construction, parses raw backend Data Transfer Objects (DTOs), transforms them into clean Frontend UI Models, and propagates strongly typed `SdkError` instances.

---

## SDK Directory Structure

```
src/sdk/
├── common/
│   ├── BaseSdk.ts        # Abstract base class providing HTTP execution & error wrapping
│   ├── SdkError.ts       # Typed error class extending AppError
│   └── types.ts          # Common pagination & response envelope types
├── auth/
│   ├── auth.dto.ts       # Backend DTO schemas for Auth
│   ├── auth.mapper.ts    # DTO-to-UI model transformers
│   └── AuthSdk.ts        # Production Auth SDK class & singleton instance
├── campus/
│   ├── campus.dto.ts     # Backend DTO schemas for Campus & Facilities
│   ├── campus.mapper.ts  # Campus DTO-to-UI model transformers
│   └── CampusSdk.ts      # Production Campus SDK class & singleton instance
├── planner/
│   ├── planner.dto.ts    # Backend DTO schemas for Academic Planner
│   ├── planner.mapper.ts # Planner DTO-to-UI model transformers
│   └── PlannerSdk.ts     # Production Planner SDK class & singleton instance
├── atlas/
│   ├── atlas.dto.ts      # Backend DTO schemas for Atlas Maps & Wayfinding
│   ├── atlas.mapper.ts   # Atlas DTO-to-UI model transformers
│   └── AtlasSdk.ts      # Production Atlas SDK class & singleton instance
├── queryKeys.ts          # Centralized, domain-grouped query keys
└── index.ts              # Unified SDK export entrypoint
```

---

## Key Principles & Design Conventions

1. **DTO vs UI Model Separation**:
   - Backend DTOs (`*.dto.ts`) reflect raw server schemas (nullable fields, snake_case or specific JSON formats).
   - Frontend Models (`src/models/*.model.ts`) provide clean, predictable UI objects used across React components.
   - Domain Mappers (`*.mapper.ts`) convert DTOs to UI models upon response parsing.

2. **Single Responsibility & Endpoint Encapsulation**:
   - Component developers never need to know endpoint paths (e.g. `/api/events/upcoming`).
   - SDK methods (e.g. `campusSdk.getUpcomingEvents()`) handle URL formatting and query parameters.

3. **Unified Error Propagation**:
   - All network, timeout, or HTTP status errors thrown during request execution are caught by `BaseSdk` and transformed into `SdkError` instances containing `statusCode`, `code`, `details`, and `correlationId`.

---

## SDK Modules Reference

### 1. Authentication SDK (`AuthSdk`)
- Base Endpoint: `/api/v1/auth`
- Methods: `login`, `register` (with required `@NotBlank username`), `getCurrentUser` (`GET /api/v1/auth/me`), `refreshToken`, `updateProfile`, `changePassword`, `logout`

### 2. Campus SDK (`CampusSdk`)
- Base Endpoints: `/api/v1/events`, `/api/v1/councils`, `/api/v1/resources`, `/api/v1/academic`
- Methods: `getBuildings`, `getBuildingById`, `getLocations`, `getFloorPlans`, `getEvents`, `getUpcomingEvents`, `getEventById`, `createEvent`, `updateEvent`, `deleteEvent`, `registerForEvent`, `cancelEventRegistration`, `getEventRegistrationStatus` (`GET /api/v1/events/{id}/is-registered`)

### 3. Academic Planner SDK (`PlannerSdk`)
- Base Endpoint: `/api/v1/planner`
- Methods: `getTasks` (`GET /api/v1/planner`), `getTaskById`, `createTask` (`POST /api/v1/planner`), `updateTask` (`PUT /api/v1/planner/{id}`), `deleteTask` (`DELETE /api/v1/planner/{id}`), `archiveTask`, `restoreTask`, `markTaskComplete` (`PATCH /api/v1/planner/{id}/status`), `getSchedules`, `getTimetable`, `getStudyGoals`, `getDegreePlan`, `getAcademicCalendar`

### 4. Atlas Maps & AI SDK (`AtlasSdk`)
- Base Endpoints: `/api/v1/atlas`, `/api/v1/atlas/search`, `/api/v1/atlas/route`, `/api/v1/atlas/landmarks`, `/api/v1/atlas/layers`
- Methods: `chat` (`POST /api/v1/atlas/chat`), `getCapabilities`, `searchSpatial`, `calculateRoute`, `getLandmarks`, `getLandmarkById`, `getMapLayers`

### 5. Community SDK (`CommunitySdk`)
- Base Endpoints: `/api/v1/communities`, `/api/v1/posts`, `/api/v1/comments`
- Methods: `getCommunities`, `getFeaturedCommunities`, `getTrendingCommunities`, `getRecentlyActiveCommunities`, `getJoinedCommunities`, `getCommunityById`, `getCommunitiesByCouncil`, `joinCommunity`, `leaveCommunity`, `getCommunityPosts`

### 6. Council SDK (`CouncilSdk`)
- Base Endpoint: `/api/v1/councils`
- Methods: `getCouncils`, `getFeaturedCouncils`, `getRecentlyActiveCouncils`, `getJoinedCouncils`, `getCouncilById`, `getCouncilBySlug`, `joinCouncil`, `leaveCouncil`, `getCouncilLeadership`, `getCouncilEvents`, `getCouncilNotices`, `getCouncilResources`

### 7. Resource SDK (`ResourceSdk`)
- Base Endpoint: `/api/v1/resources`
- Methods: `getResources`, `getFeaturedResources`, `getRecentResources`, `getPopularResources`, `getBookmarkedResources`, `getResourceById`, `createResource` (multipart form upload), `updateResource`, `deleteResource`

### 8. Notice SDK (`NoticeSdk`)
- Base Endpoint: `/api/v1/notices`
- Methods: `getAllNotices`, `getNoticeById`, `getNoticeBySlug`, `createNotice`, `updateNotice`, `publishNotice` (`PATCH /api/v1/notices/{id}/publish`), `pinNotice` (`PATCH /api/v1/notices/{id}/pin`), `deleteNotice`

### 9. Notification SDK (`NotificationSdk` & `ScheduledNotificationSdk`)
- Base Endpoints: `/api/v1/notifications`, `/api/v1/scheduled-notifications`
- Methods: `getNotifications`, `getNotificationStats`, `getUnreadCount` (`GET /api/v1/notifications/unread/count`), `markAsRead` (`PATCH /api/v1/notifications/{id}/read`), `markAsUnread`, `markAllAsRead` (`PATCH /api/v1/notifications/read-all`), `archiveNotification`, `restoreNotification`, `deleteNotification`

### 10. Calendar SDK (`CalendarSdk`)
- Base Endpoint: `/api/v1/calendar`
- Methods: `getEntries` (`GET /api/v1/calendar`), `getEntriesInRange` (`GET /api/v1/calendar/range?from=...&to=...`), `getEntryById`, `createEntry`, `updateEntry`, `deleteEntry`

---

## Fallback Policy
In accordance with Pre-Phase 5 standards, obsolete local storage fallbacks, in-memory repository fallbacks, and seed datasets have been removed from all SDKs. Requests execute exclusively against live backend contracts and propagate strongly typed `SdkError` instances on failure.
