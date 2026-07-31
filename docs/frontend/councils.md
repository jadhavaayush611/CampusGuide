# Councils Module Integration Documentation

## Overview

The **Councils Module** for CampusGuide provides campus-wide student council discovery, council detail viewing, governance & constitutional leadership tracking, membership management with optimistic updates, upcoming council events, council-specific notices/announcements, governance resources/documents, and searchable member directories.

All network interactions strictly flow through the frontend SDK (`CouncilSdk`), using TanStack React Query (`v5`) for server state management, caching, background refetching, and optimistic UI mutations. Direct API communication inside UI components is strictly prohibited.

---

## Architectural Boundary: Councils vs. Communities

To maintain architectural domain purity across CampusGuide, a clear boundary is enforced between **Councils** and **Communities**:

- **Councils**: Official campus organizations (e.g., Student Senate, Engineering Council, Computer Society of India, IEEE, Cultural Board, Sports Council, E-Cell). Councils possess **formal constitutional governance hierarchies**, executive board titles (Council Chair, President, Vice Chair, Treasurer, Faculty Advisor), and administrative budget oversight.
- **Communities**: Social, interest-based student groups (e.g., Photography Group, Rust Study Club, Chess Enthusiasts). Ownership is **lightweight** (Community Owner, Administrators, Moderators) without formal governance charters or executive officer titles.

The Councils page and detail views strictly embody this separation by highlighting constitutional governance structures, faculty advisory boards, and official notices/handbooks.

---

## 1. Architecture & Layering

```
 ┌────────────────────────────────────────────────────────┐
 │                    React UI Layer                      │
 │     Councils Page | Council Detail Page | Components   │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                  React Query Hooks                     │
 │   useCouncils, useCouncilDetails, useCouncilMembership │
 │   useCouncilLeadership, useCouncilEvents, etc.         │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                     SDK Data Layer                     │
 │           CouncilSdk (extends BaseSdk)                 │
 │          Mappers (DTO ↔ UI Domain Models)              │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                   Spring Boot Backend                  │
 │   /api/v1/councils | /api/v1/events | /api/v1/notices   │
 └───────────────────────────┬────────────────────────────┘
```

### Module File Structure

- **Domain Models**: `src/models/council.model.ts`
- **SDK DTOs & Mappers**: `src/sdk/council/council.dto.ts`, `src/sdk/council/council.mapper.ts`
- **SDK Service**: `src/sdk/council/CouncilSdk.ts`
- **Query Keys**: `src/sdk/queryKeys.ts` (`queryKeys.councils`)
- **React Query Hooks**: `src/hooks/council/`
  - `useCouncils.ts`
  - `useFeaturedCouncils.ts`
  - `useRecentlyActiveCouncils.ts`
  - `useJoinedCouncils.ts`
  - `useCouncilDetails.ts`
  - `useCouncilMembership.ts`
  - `useCouncilLeadership.ts`
  - `useCouncilEvents.ts`
  - `useCouncilNotices.ts`
  - `useCouncilResources.ts`
  - `useCouncilMembers.ts`
- **UI Components**: `src/app/components/councils/`
  - `CouncilCard.tsx`
  - `CouncilDiscovery.tsx`
  - `CouncilHeader.tsx`
  - `CouncilLeadership.tsx`
  - `CouncilEvents.tsx`
  - `CouncilNotices.tsx`
  - `CouncilResources.tsx`
  - `CouncilMembers.tsx`
  - `CouncilSkeletons.tsx`
- **Pages**: `src/app/pages/Councils.tsx`, `src/app/pages/Council.tsx`

---

## 2. Component Hierarchy

```
Councils Page (/councils)
 ├── Header
 └── CouncilDiscovery (wrapped in ErrorBoundary)
       ├── Search & Sort Bar (Category Pills, Sort Dropdown, Search Input)
       ├── View Mode Tabs (All, Featured, Recently Active, Joined)
       ├── CouncilCard Grid
       │     └── Optimistic Join/Leave Button
       └── Pagination Controls

Council Detail Page (/councils/:id)
 ├── Navigation Bar (Back to Directory)
 ├── CouncilHeader (wrapped in ErrorBoundary)
 │     ├── Banner, Logo Emoji/Image, Name, Category, Badges
 │     ├── Contact Bar (Email, Phone, Office Location, Official Website)
 │     ├── Activity Metrics Bar (Members, Active Events, Notices, Engagement Score)
 │     └── Optimistic Join/Leave/Pending Action Button
 ├── Detail Tabs (Leadership | Notices & Announcements | Events | Resources | Members)
 └── Tab Panels (each wrapped in section-level ErrorBoundary)
       ├── CouncilLeadership (Faculty Advisor, Chair/President, Officers, Hierarchy Tree)
       ├── CouncilNotices (Pinned updates, High priority flags, Category filters, Attachments)
       ├── CouncilEvents (Upcoming event feed, Registration status toggle, .ICS calendar generator)
       ├── CouncilResources (Handbooks, Forms, Meeting Minutes, Search & Category Filter)
       └── CouncilMembers (Searchable member list, Role filter tabs, Member cards)
```

---

## 3. Data Flow & Server State Management

1. **Query Keys Hierarchy**:
   - `queryKeys.councils.all`: Root key `['councils']`
   - `queryKeys.councils.list(params)`: Filtered/paginated directory `['councils', 'list', params]`
   - `queryKeys.councils.featured()`: Featured councils `['councils', 'featured']`
   - `queryKeys.councils.recentlyActive()`: Recently active councils `['councils', 'recentlyActive']`
   - `queryKeys.councils.joined()`: User joined councils `['councils', 'joined']`
   - `queryKeys.councils.detail(id)`: Single council detail `['councils', 'detail', id]`
   - `queryKeys.councils.leadership(id)`: Council leadership `['councils', 'detail', id, 'leadership']`
   - `queryKeys.councils.events(id)`: Council events `['councils', 'detail', id, 'events']`
   - `queryKeys.councils.notices(id, filter)`: Council notices `['councils', 'detail', id, 'notices', { filter }]`
   - `queryKeys.councils.resources(id, cat)`: Council resources `['councils', 'detail', id, 'resources', { cat }]`
   - `queryKeys.councils.members(id, params)`: Member directory `['councils', 'detail', id, 'members', params]`

2. **Parallel Queries & Performance Optimization**:
   - `Council` detail page executes parallel queries for detail (`useCouncilDetails`), leadership (`useCouncilLeadership`), events (`useCouncilEvents`), notices (`useCouncilNotices`), resources (`useCouncilResources`), and members (`useCouncilMembers`).
   - Query stale time is configured to `5 minutes` to eliminate duplicate backend network requests.

---

## 4. Membership Lifecycle & Optimistic Updates

The membership lifecycle (`join` and `leave`) utilizes `useOptimisticMutation` (`src/hooks/common/useOptimisticMutation.ts`):

1. **User Action**: User clicks `Join Council` or `Joined Council` on `CouncilCard` or `CouncilHeader`.
2. **On Mutate (Optimistic Step)**:
   - Ongoing refetches for `queryKeys.councils.detail(councilId)` are cancelled to prevent race conditions.
   - Cache snapshot is saved for rollback on failure.
   - The query cache for `queryKeys.councils.detail(councilId)` is updated immediately:
     - `isJoined` toggles boolean state
     - `myRole` toggles between `'MEMBER'` and `'NONE'`
     - `memberCount` increments or decrements immediately
   - The `queryKeys.councils.joined()` and `queryKeys.councils.all` query caches are updated immediately.
3. **SDK Call**: `councilSdk.joinCouncil(councilId)` or `councilSdk.leaveCouncil(councilId)` sends HTTP request to Spring Boot backend (`/api/v1/councils/{id}/join`).
4. **On Success / Settled**: Toast notification displays confirmation ("Successfully joined council!"), and associated query keys are invalidated to synchronize exact server state.
5. **On Error**: Cache automatically rolls back to the previous snapshot, and a standardized toast error notification is presented.

---

## 5. Endpoints & SDK Mapping

| Feature | Backend Endpoint | SDK Method | React Query Hook |
|---|---|---|---|
| Council Directory | `GET /api/v1/councils` | `councilSdk.getCouncils` | `useCouncils` |
| Featured Councils | `GET /api/v1/councils` | `councilSdk.getFeaturedCouncils` | `useFeaturedCouncils` |
| Council Detail | `GET /api/v1/councils/{id}` | `councilSdk.getCouncilById` | `useCouncilDetails` |
| Council Slug Detail | `GET /api/v1/councils/slug/{slug}` | `councilSdk.getCouncilBySlug` | `useCouncilDetails` |
| Join Council | `POST /api/v1/councils/{id}/join` | `councilSdk.joinCouncil` | `useCouncilMembership` |
| Leave Council | `DELETE /api/v1/councils/{id}/leave` | `councilSdk.leaveCouncil` | `useCouncilMembership` |
| Leadership Structure | `GET /api/v1/councils/{id}/leadership` | `councilSdk.getCouncilLeadership` | `useCouncilLeadership` |
| Council Events Feed | `GET /api/v1/events/council/{councilId}` | `councilSdk.getCouncilEvents` | `useCouncilEvents` |
| Notices & Announcements | `GET /api/v1/notices` | `councilSdk.getCouncilNotices` | `useCouncilNotices` |
| Resources & Documents | `GET /api/v1/resources/council/{councilId}` | `councilSdk.getCouncilResources` | `useCouncilResources` |
| Member Directory | `GET /api/v1/councils/{id}/members` | `councilSdk.getCouncilMembers` | `useCouncilMembers` |

---

## 6. UX & Error Boundary Safeguards

- **Section-Level Error Boundaries**: Primary views and sub-panels (`CouncilDiscovery`, `CouncilHeader`, `CouncilLeadership`, `CouncilNotices`, `CouncilEvents`, `CouncilResources`, `CouncilMembers`) are wrapped with `ErrorBoundary` (`src/core/errors/ErrorBoundary.tsx`), preventing total page crashes if a sub-component errors.
- **Skeleton Loaders**: Skeletal loaders (`CouncilCardSkeleton`, `CouncilHeaderSkeleton`, `CouncilSectionSkeleton`) display during initial query fetching.
- **Empty States**: Clear empty state cards guide users when searches return no results, or when a council has no published notices, events, or resources yet.
- **Calendar Shortcuts**: Every event provides an inline `.ICS` file download generator for Google Calendar / Apple Calendar integration.
