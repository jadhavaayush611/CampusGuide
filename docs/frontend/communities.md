# Communities Module Integration Documentation

## Overview

The **Communities Module** for CampusGuide provides campus-wide student community discovery, community detail viewing, discussions/feed management, membership status tracking, member directory lookups, and community creation/editing capabilities. 

All network interactions strictly flow through the frontend SDK (`CommunitySdk`), using TanStack React Query (`v5`) for server state management, caching, background refetching, and optimistic UI mutations. Direct API communication inside UI components is strictly prohibited.

---

## Architectural Boundary: Communities vs. Councils

To maintain architectural domain purity across CampusGuide, a clear boundary is preserved between **Communities** and **Councils**:

- **Communities**: Social, interest-based student spaces (e.g., Robotics Club, Photography Society, Hackathon Group). Ownership is **lightweight** and informal (Community Owner, Administrators, Moderators acting as facilitators and content safety managers). Communities **do not** feature formal governance structures or executive officer titles.
- **Councils**: Official campus organizations (e.g., Student Senate, Engineering Council, Sports Board). Councils own formal governance hierarchies, constitutional executive boards (President, Vice President, Secretary, Treasurer, Faculty Advisor, Executive Committees), and official administrative budgets.

The Community Detail page strictly adheres to this separation by omitting formal leadership titles and presenting only lightweight community ownership.

---

## 1. Architecture & Layering

```
 ┌────────────────────────────────────────────────────────┐
 │                    React UI Layer                      │
 │   Communities Page | CommunityDetail Page | Components │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                  React Query Hooks                     │
 │  useCommunities, useCommunityDetails, useCommunityFeed │
 │  useCommunityMembership, useCommunityMembers           │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                     SDK Data Layer                     │
 │          CommunitySdk (extends BaseSdk)                │
 │          Mappers (DTO ↔ UI Domain Models)              │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                   Spring Boot Backend                  │
 │   /api/v1/communities | /api/v1/posts | /api/v1/...    │
 └───────────────────────────┬────────────────────────────┘
```

### Module File Structure

- **Domain Models**: `src/models/community.model.ts`
- **SDK DTOs & Mappers**: `src/sdk/community/community.dto.ts`, `src/sdk/community/community.mapper.ts`
- **SDK Service**: `src/sdk/community/CommunitySdk.ts`
- **Query Keys**: `src/sdk/queryKeys.ts` (`queryKeys.communities`)
- **React Query Hooks**: `src/hooks/community/`
  - `useCommunities.ts`
  - `useFeaturedCommunities.ts`
  - `useJoinedCommunities.ts`
  - `useCommunityDetails.ts`
  - `useCommunityMembership.ts`
  - `useCommunityFeed.ts`
  - `useCreateCommunityPost.ts`
  - `useCommunityMembers.ts`
  - `useCreateCommunity.ts`
  - `useUpdateCommunity.ts`
- **UI Components**: `src/app/components/communities/`
  - `CommunityCard.tsx`
  - `CommunityHeader.tsx`
  - `CommunityDiscovery.tsx`
  - `CommunityFeed.tsx`
  - `CommunityMembers.tsx`
  - `CommunityAbout.tsx`
  - `CommunityActivityPanel.tsx`
  - `CommunityCreateModal.tsx`
  - `CommunitySkeletons.tsx`
- **Pages**: `src/app/pages/Communities.tsx`, `src/app/pages/CommunityDetail.tsx`

---

## 2. Component Hierarchy

```
Communities Page (/communities)
 ├── Header
 ├── CommunityDiscovery
 │     ├── Search & Filter Bar (Category Pills, Sorting, Search input)
 │     ├── View Tabs (All, Featured, Trending, Joined)
 │     ├── CommunityCard Grid
 │     │     └── Optimistic Join/Leave Button
 │     └── Pagination Controls
 ├── CommunityActivityPanel
 │     ├── Recent Campus Activity Widget
 │     └── Most Active Societies List
 └── CommunityCreateModal (Modal for creation & editing)

CommunityDetail Page (/communities/:id)
 ├── Navigation Bar (Back button)
 ├── CommunityHeader
 │     ├── Banner, Logo, Title, Category, Tags
 │     ├── Activity Metrics Bar (Members, Posts/wk, Admins, Engagement)
 │     └── Actions (Join/Leave, Share, Edit)
 ├── Detail Tabs (Discussions Feed | Members | About)
 └── Tab Panels
       ├── CommunityFeed
       │     ├── Feed Filters (All, Announcements, Pinned)
       │     ├── Discussion Composer Modal
       │     └── Post Cards (Author, Timestamp, Badges, Likes, Comments)
       ├── CommunityMembers
       │     ├── Member Search & Role Filter Tabs
       │     └── Member Card Grid
       └── CommunityAbout
             ├── Community Information (Description, Category, Tags, Creation Date, Privacy)
             └── Lightweight Management Ownership (Owner, Administrators, Moderators)
```

---

## 3. Data Flow & Server State Management

1. **Query Keys Hierarchy**:
   - `queryKeys.communities.all`: Root key `['communities']`
   - `queryKeys.communities.list(params)`: Filtered/paginated list `['communities', 'list', params]`
   - `queryKeys.communities.featured()`: Featured communities `['communities', 'featured']`
   - `queryKeys.communities.trending()`: Trending communities `['communities', 'trending']`
   - `queryKeys.communities.joined()`: User joined communities `['communities', 'joined']`
   - `queryKeys.communities.detail(id)`: Single community detail `['communities', 'detail', id]`
   - `queryKeys.communities.feed(id, filter)`: Community posts `['communities', 'detail', id, 'feed', { filter }]`
   - `queryKeys.communities.members(id, params)`: Member directory `['communities', 'detail', id, 'members', params]`

2. **Parallel Queries & Performance Optimization**:
   - `CommunityDiscovery` executes parallel queries for Discovery directory (`useCommunities`), Featured (`useFeaturedCommunities`), Trending (`useTrendingCommunities`), and Joined (`useJoinedCommunities`).
   - Query stale time is configured to `5 minutes` to minimize redundant backend network roundtrips.

---

## 4. Membership Lifecycle & Optimistic Updates

The membership lifecycle (`join` and `leave`) utilizes `useOptimisticMutation` (`src/hooks/common/useOptimisticMutation.ts`):

1. **User Action**: User clicks `Join` or `Leave` on a `CommunityCard` or `CommunityHeader`.
2. **On Mutate (Optimistic Step)**:
   - Ongoing refetches for `queryKeys.communities.all` are cancelled to prevent race conditions.
   - Cache snapshot is saved for rollback on failure.
   - The query cache for `queryKeys.communities.detail(communityId)` is immediately updated:
     - `isJoined` toggles boolean state
     - `myRole` toggles between `'MEMBER'` and `'NONE'`
     - `memberCount` increments or decrements immediately
   - The `queryKeys.communities.joined()` query cache is updated to add or remove the community immediately.
3. **SDK Call**: `communitySdk.joinCommunity(communityId)` or `communitySdk.leaveCommunity(communityId)` sends the HTTP request to the Spring Boot backend (`/api/v1/communities/{id}/join`).
4. **On Success / Settled**: Toast notification displays confirmation ("Joined community successfully!"), and associated query keys are invalidated to synchronize exact server state.
5. **On Error**: Cache automatically rolls back to the previous snapshot, and a standardized toast error notification is presented to the user.

---

## 5. Endpoints & SDK Mapping

| Feature | Backend Endpoint | SDK Method | React Query Hook |
|---|---|---|---|
| Community Directory | `GET /api/v1/communities` | `communitySdk.getCommunities` | `useCommunities` |
| Community Detail | `GET /api/v1/communities/{id}` | `communitySdk.getCommunityById` | `useCommunityDetails` |
| Council Communities | `GET /api/v1/communities/councils/{id}/communities` | `communitySdk.getCommunitiesByCouncil` | `useCommunitiesByCouncil` |
| Create Community | `POST /api/v1/communities` | `communitySdk.createCommunity` | `useCreateCommunity` |
| Update Community | `PUT /api/v1/communities/{id}` | `communitySdk.updateCommunity` | `useUpdateCommunity` |
| Join Community | `POST /api/v1/communities/{id}/join` | `communitySdk.joinCommunity` | `useCommunityMembership` |
| Leave Community | `DELETE /api/v1/communities/{id}/leave` | `communitySdk.leaveCommunity` | `useCommunityMembership` |
| Community Feed | `GET /api/v1/posts/community/{id}` | `communitySdk.getCommunityPosts` | `useCommunityFeed` |
| Create Post | `POST /api/v1/posts` | `communitySdk.createPost` | `useCreateCommunityPost` |
| Member Directory | `GET /api/v1/communities/{id}/members` | `communitySdk.getCommunityMembers` | `useCommunityMembers` |

---

## 6. UX & Error Boundary Safeguards

- **Section-Level Error Boundaries**: Each primary component block (`CommunityDiscovery`, `CommunityActivityPanel`, `CommunityHeader`, `CommunityFeed`, `CommunityMembers`, `CommunityAbout`) is wrapped with `ErrorBoundary` (`src/core/errors/ErrorBoundary.tsx`), preventing full-page crashes if a rendering error occurs in a sub-section.
- **Skeleton Loaders**: Dedicated skeleton loaders (`CommunityCardSkeleton`, `CommunityHeaderSkeleton`, `CommunityFeedSkeleton`, `CommunityMembersSkeleton`) render during initial query fetches.
- **Empty States**: Clean empty state cards guide the user when search yields no results or a community has no discussions yet.
