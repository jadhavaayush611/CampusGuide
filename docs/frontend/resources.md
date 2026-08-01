# Resources Module Integration Documentation

## Overview

The **Resources Module** for CampusGuide provides campus-wide academic and institutional resource discovery, document previews, downloads, bookmarking with optimistic mutations, upload/management workflows, and category-based resource filtering.

All network communication flows exclusively through the frontend SDK (`ResourceSdk`), using TanStack React Query (`v5`) for server state management, caching, background refetching, and optimistic updates. Direct API communication inside UI components is strictly prohibited.

---

## 1. Architecture & Layering

```
 ┌────────────────────────────────────────────────────────┐
 │                    React UI Layer                      │
 │      ResourceCenter Page | Modals | Cards | Filter     │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                  React Query Hooks                     │
 │   useResources, useFeaturedResources, useRecentResources│
 │   usePopularResources, useBookmarkedResources, etc.    │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                     SDK Data Layer                     │
 │           ResourceSdk (extends BaseSdk)                │
 │          Mappers (DTO ↔ UI Domain Models)              │
 └───────────────────────────┬────────────────────────────┘
                             │
                             ▼
 ┌────────────────────────────────────────────────────────┐
 │                   Spring Boot Backend                  │
 │   /api/v1/resources | /api/v1/resources/search | etc.  │
 └────────────────────────────────────────────────────────┘
```

### Module File Structure

- **Domain Models**: `src/models/resource.model.ts`
- **SDK DTOs & Mappers**: `src/sdk/resources/resource.dto.ts`, `src/sdk/resources/resource.mapper.ts`
- **SDK Service**: `src/sdk/resources/ResourceSdk.ts`
- **Query Keys**: `src/sdk/queryKeys.ts` (`queryKeys.resources`)
- **React Query Hooks**: `src/hooks/resources/`
  - `useResources.ts`
  - `useFeaturedResources.ts`
  - `useRecentResources.ts`
  - `usePopularResources.ts`
  - `useBookmarkedResources.ts`
  - `useResourceDetails.ts`
  - `useCreateResource.ts`
  - `useUpdateResource.ts`
  - `useDeleteResource.ts`
  - `useBookmarkResource.ts`
  - `useDownloadResource.ts`
- **UI Components**: `src/app/components/resources/`
  - `ResourceCard.tsx`
  - `ResourceFilterBar.tsx`
  - `ResourcePreview.tsx`
  - `ResourceDetailsModal.tsx`
  - `ResourceUploadModal.tsx`
  - `ResourceSkeleton.tsx`
  - `ResourceErrorBoundary.tsx`
- **Page**: `src/app/pages/ResourceCenter.tsx`

---

## 2. Resource Discovery, Categories & Filtering

The discovery engine supports multi-faceted queries across 9 standard academic and institutional categories:

1. **Lecture Notes** — Semester slide decks, lecture write-ups, and unit summaries.
2. **Lab Manuals** — Practical experiment guidelines, code tasks, and lab rubrics.
3. **Past Papers** — Mid-term, final exam papers, and sample answer keys.
4. **Syllabi** — Course outlines, weightages, and grading schemes.
5. **Forms** — Administrative leave forms, course add/drop requests.
6. **Templates** — Capstone report LaTeX templates, project thesis formats.
7. **Handbooks** — Student charters, orientation guides, hostel rules.
8. **Policies** — Academic integrity, plagiarism guidelines, research codes.
9. **Miscellaneous** — Campus vector maps, software tools, and media.

### Discovery Tabs & Parallel Queries

The `ResourceCenter` page executes parallel React Query operations:
- `useResources(params)` for main paginated directory catalog.
- `useFeaturedResources()` for featured items.
- `useRecentResources()` for recently uploaded documents.
- `usePopularResources()` for items sorted by download counts.
- `useBookmarkedResources()` for user's saved items.

Filters include text search (title, tags, description, uploader, filename), file extension filtering (PDF, DOCX, ZIP, PNG, PPTX, Video), and sorting (Newest First, Most Popular, Title A-Z, File Size).

---

## 3. Resource Lifecycle

```
 ┌───────────────┐        ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
 │    Upload     │ ────►  │   Discovery   │ ────►  │ Edit / Update │ ────►  │  Soft Delete  │
 │ (Multipart)   │        │ & Search      │        │ (Metadata)    │        │ (isDeleted)   │
 └───────────────┘        └───────────────┘        └───────────────┘        └───────────────┘
```

1. **Upload**: Users submit document metadata (Title, Description, Category, Tags) along with the file binary via `ResourceUploadModal`. The SDK builds a `FormData` multipart body sent to `POST /api/v1/resources`.
2. **Discovery**: Uploaded resources are indexed and made available instantly across category tabs, search queries, and recent items lists.
3. **Update / Edit**: Resource uploader or administrators can edit title, description, category, and tags via `PUT /api/v1/resources/{id}` using `useUpdateResource`.
4. **Deletion**: Authorized users can soft-delete a resource via `DELETE /api/v1/resources/{id}`, removing it from queries while preserving audit trail integrity.

---

## 4. Bookmark Lifecycle & Optimistic Updates

The bookmarking system provides instant UI feedback using React Query's `onMutate` optimistic updates:

```
  User Clicks Bookmark
           │
           ▼
  Cancel Active Queries ──► Mutate Query Cache Optimistically ──► Render Updated Icon Immediately
                                      │
                                      ▼
                           Execute SDK API Request
                             /               \
                    (Success)                 (Failure)
                       │                         │
                       ▼                         ▼
             Keep Optimistic State      Rollback Cache to Previous
             & Invalidate Cache         Snapshot & Show Notification
```

- When the bookmark icon is clicked, `useBookmarkResource` immediately updates the cache for both item details and list collections.
- If the network request fails, the hook automatically rolls back to the prior state snapshot.

---

## 5. Previews & Downloads Integration

- **PDF Viewer**: Embedded iframe document viewer.
- **Images**: High-resolution image viewer modal.
- **Audio / Video**: Native HTML5 media player controls.
- **External Links**: Direct navigation link wrapper.
- **Fallback**: For binary formats without in-browser rendering (DOCX, ZIP, PPTX), a graceful fallback notice is rendered with a direct download button.
- **Downloads**: Handled via `useDownloadResource` which triggers browser file downloading via `/api/v1/resources/download/{id}` and increments download metrics.

---

## 6. Component Hierarchy

```
ResourceCenter (Page)
 ├── Header
 ├── Hero Banner & CTAs
 ├── Discovery Navigation Tabs (Directory | Featured | Recent | Popular | Bookmarked)
 └── ResourceErrorBoundary (Section-Level Boundary)
      ├── ResourceFilterBar (Category Pills, Search, File Type Filter, Sorting, View Toggle)
      ├── ResourceSkeleton (Loading Skeletons for Grid & List)
      ├── ResourceCard (Grid/List Item Card with Bookmark, Preview, Edit, Delete, Download)
      ├── ResourceDetailsModal (Full Detail View & Previews)
      │    └── ResourcePreview (PDF, Image, Video, Link, or Fallback UI)
      └── ResourceUploadModal (Upload & Edit Form Modal)
```

---

## 7. Performance & Standards Checklist

- [x] **SDK-Only Communication**: All network calls strictly use `resourceSdk` (extending `BaseSdk`).
- [x] **React Query Server State**: All data fetching, caching, and invalidation handled via TanStack React Query v5.
- [x] **Parallel Queries**: Catalog, featured, recent, and bookmarked queries run in parallel without duplicate calls.
- [x] **Optimistic UI Mutations**: Bookmarking and updates reflect immediately before server response.
- [x] **Section Error Boundary**: Isolated failures in the resource grid are caught gracefully with retry capability.
- [x] **Typed Models**: Strict TypeScript interfaces for `Resource`, `ResourceCategory`, `ResourceQueryParams`, and `ResourceDto`.
