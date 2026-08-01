# Frontend Notices Module Architecture

This document provides a comprehensive overview of the frontend Notices module architecture in CampusGuide, including data flows, component hierarchy, notice lifecycle, read/unread status management, and React Query integration.

---

## 1. Overview & Architecture

The Notices module is built on a layered architecture separating UI components, TanStack React Query hooks, domain models, and SDK-backed API communication layer.

```
┌───────────────────────────────────────────────────────────────────┐
│                          UI Layer                                 │
│ NoticeBoard (Page) | NoticeCard | NoticeFilters | NoticeModals    │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                      React Query Hooks                            │
│ useNotices | usePinnedNotices | useToggleNoticeRead | useCreate...│
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                        SDK & Mapper Layer                         │
│ NoticeSdk (Extends BaseSdk) | notice.mapper | notice.dto          │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                       Backend REST API                            │
│ /api/v1/notices (Spring Boot NoticeController)                     │
└───────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Hierarchy

```
NoticeBoard (Page)
 ├── Header
 ├── NoticeHeroBanner (Hero & Stats)
 └── NoticeErrorBoundary (Section-Level Boundary)
      ├── NoticeFilters (Search, Categories, Priority, View Modes, Tabs)
      ├── NoticeSkeleton (Loading Skeleton)
      ├── NoticeEmptyState (Empty & Error Fallbacks)
      ├── NoticeCard (Grid/List Card Display)
      │    ├── CategoryBadge
      │    ├── PriorityBadge
      │    ├── ScopeBadge
      │    └── ReadUnreadToggle
      ├── NoticeDetailsModal (Full Detail View)
      │    ├── NoticeAttachmentViewer (Preview, Download, External Link)
      │    └── ManagementActionButtons (Edit, Pin, Delete)
      └── NoticeFormModal (Create / Edit Form)
           └── AttachmentInputControls
```

---

## 3. Notice Lifecycle

A notice transitions through several lifecycle states:

```
[ DRAFT ]  ──(Publish Action)──>  [ PUBLISHED / ACTIVE ]
                                         │
                         ┌───────────────┴───────────────┐
                         ▼                               ▼
                 [ PINNED TO TOP ]             [ EXPIRED / ARCHIVED ]
                         │                               │
                         └───────────────┬───────────────┘
                                         ▼
                                    [ DELETED ]
```

1. **Creation**: Generated via `NoticeFormModal` using `useCreateNotice` hook calling `NoticeSdk.createNotice()`.
2. **Publication State**: Controlled via `isPublished` boolean.
3. **Pinning**: Controlled via `isPinned` boolean. Pinned notices remain elevated at top of default list sorting.
4. **Expiration / Archiving**: Governed by `expiresAt` timestamp. Expired notices move automatically to the Archived view.
5. **Deletion**: Permanently removes notice via `useDeleteNotice` hook.

---

## 4. Read / Unread Lifecycle & Optimistic Updates

Read status is tracked client-side via `localStorage` ('campusguide_read_notices') and prepared for backend user read tracking endpoints.

```
       User Action (Toggle Read/Unread)
                       │
                       ▼
        Optimistic Query Cache Mutation
     (UI updates immediately without flicker)
                       │
                       ▼
       NoticeSdk.markAsRead / markAsUnread
                       │
                       ▼
  Persisted in LocalStorage & Invalidate Query Keys
```

* **Optimistic Updates**: `useToggleNoticeRead` optimistically modifies TanStack React Query cache (`queryKeys.notices.all`) on click.
* **Rollback & Resilience**: If an API mutation fails, the cache state rolls back seamlessly.

---

## 5. Supported Categories & Filtering

The Notices module supports 8 core categories:

- **Academic**: Exam regulations, credit transfer, syllabus updates.
- **Administrative**: Facilities updates, library maintenance.
- **Examination**: Timetables, hall tickets, grade re-evaluations.
- **Events**: Hackathons, cultural fests, workshops.
- **Councils**: General election notifications, student council circulars.
- **Placements**: Recruitment drives, interview schedules.
- **Scholarships**: Merit-cum-means financial aid deadlines.
- **General**: General notices and announcements.

### Filtering Capabilities
- **Title / Content / Tag / Publisher Search**
- **Category Filter Pills**
- **Priority Filter (URGENT, HIGH, MEDIUM, LOW)**
- **Tab Views (All, Pinned, Important, Unread, Archived)**
- **Sorting Options (Newest First, Priority High-to-Low, Title A-Z)**

---

## 6. SDK Methods & Query Keys

### SDK Methods (`NoticeSdk`)
- `getAllNotices(params?: NoticeQueryParams)`
- `getPinnedNotices()`
- `getRecentNotices()`
- `getImportantNotices()`
- `getArchivedNotices()`
- `getNoticeById(id: string)`
- `getNoticeBySlug(slug: string)`
- `createNotice(payload: CreateNoticePayload)`
- `updateNotice(id: string, payload: UpdateNoticePayload)`
- `publishNotice(id: string, isPublished: boolean)`
- `pinNotice(id: string, isPinned: boolean)`
- `deleteNotice(id: string)`
- `markAsRead(id: string)` / `markAsUnread(id: string)`
- `downloadAttachment(attachmentId: string, url: string)`

### Query Keys (`queryKeys.notices`)
- `queryKeys.notices.all`: `['notices']`
- `queryKeys.notices.list(params)`: `['notices', 'list', params]`
- `queryKeys.notices.pinned()`: `['notices', 'pinned']`
- `queryKeys.notices.recent()`: `['notices', 'recent']`
- `queryKeys.notices.important()`: `['notices', 'important']`
- `queryKeys.notices.archived()`: `['notices', 'archived']`
- `queryKeys.notices.detail(id)`: `['notices', 'detail', id]`
- `queryKeys.notices.detailBySlug(slug)`: `['notices', 'slug', slug]`
- `queryKeys.notices.unreadCount()`: `['notices', 'unreadCount']`
