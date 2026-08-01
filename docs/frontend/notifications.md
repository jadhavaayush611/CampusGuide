# Frontend Notifications Module Architecture

This document provides a comprehensive overview of the frontend Notifications module architecture in CampusGuide, including data flow, notification lifecycle, read/archive lifecycle, component hierarchy, and SDK-backed React Query integration.

---

## 1. Overview & Architecture

The Notifications module operates as the central hub for discovering, organizing, and acting upon user alerts across the platform. It enforces a clean layered architecture, ensuring components never invoke HTTP endpoints directly.

```
┌───────────────────────────────────────────────────────────────────┐
│                          UI Layer                                 │
│ NotificationsPage | Header | CategoryTabs | StatsWidget | Cards  │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                      React Query Hooks                            │
│ useNotifications | useNotificationStats | useNotificationMutations│
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                        SDK & Mapper Layer                         │
│ NotificationSdk (Extends BaseSdk) | mapper | DTO schemas          │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                       Backend REST API                            │
│ /api/v1/notifications & /api/v1/scheduled-notifications           │
└───────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Hierarchy

```
NotificationsPage (Page)
 ├── NotificationHeader (Search, Priority & Delivery Filters, Sorting, Mark All Read)
 ├── NotificationStatsWidget (Active Inbox, Unread, Read, Archived, Infrastructure Status)
 ├── NotificationCategoryTabs (Academic, Planner, Calendar, Communities, Councils, Resources, Notices, Atlas, Auth, System)
 └── NotificationErrorBoundary (Section-Level Error Isolation)
      ├── NotificationSkeleton (Animated Skeleton Grid)
      ├── NotificationEmptyState (Empty Filter / Empty Status Fallbacks)
      ├── NotificationItemCard (Interactive Notification Card)
      │    ├── CategoryIcon & Color Badge
      │    ├── PriorityBadge (URGENT, HIGH, NORMAL, LOW)
      │    ├── DeliveryStatusBadge (DELIVERED, SCHEDULED, FAILED)
      │    ├── RelatedEntityBadge
      │    └── QuickActionButtons (Read/Unread, Archive/Restore, Delete, Deep Link)
      ├── PaginationBar (Page Navigation Controls)
      └── NotificationDetailModal (Full Detail & Action Modal)
           └── DeepLinkAction (Originating Module Deep Link)
```

---

## 3. Data Flow

1. **Query Dispatch**: `NotificationsPage` mounts and invokes `useNotifications(params)` and `useNotificationStats()`.
2. **SDK Request**: Hooks call `notificationSdk.getNotifications(params)` via `BaseSdk`.
3. **Endpoint Resolution & Fallback**:
   - The SDK sends GET requests to `/api/v1/notifications`.
   - If the backend returns paginated data, `notification.mapper` transforms `NotificationDto` into domain `NotificationItem` instances.
   - If backend connection fails or returns empty data, the SDK seamlessly falls back to local seed data and merges local storage persistence (`campusguide_read_notifications`, `campusguide_archived_notifications`).
4. **Cache & UI Update**: React Query caches server state under `queryKeys.notifications.list(params)`. Components re-render reactively with updated data.
5. **Optimistic Mutations**: User actions (e.g. marking read, archiving) invoke optimistic mutation hooks (`useMarkAsRead`, `useArchiveNotification`) which immediately update cache and UI before settling with backend.

---

## 4. Notification Lifecycle

A notification progresses through distinct delivery and attention states:

### Delivery Statuses:
- **`DELIVERED`**: The alert has been successfully dispatched to the user's in-app inbox.
- **`SCHEDULED`**: A future reminder (e.g. Capstone deadline, platform maintenance) queued for automated dispatch via scheduler service.
- **`FAILED`**: A delivery failure (e.g. SMS gateway timeout) flag triggering in-app fallback alerts.

---

## 5. Read & Archive Lifecycle

```
             ┌──────────────┐
             │   UNREAD     │
             └──────┬───────┘
                    │  Mark as Read / Mark All
                    ▼
             ┌──────────────┐
  Restore    │     READ     │     Archive
  ┌──────────┤  (In Inbox)  ├──────────┐
  │          └──────────────┘          │
  │                                    ▼
┌─┴────────────┐               ┌──────────────┐
│  ACTIVE LIST │               │   ARCHIVED   │
└──────────────┘               └──────────────┘
```

1. **Inbox Entry**: New notifications arrive as `UNREAD` and non-archived.
2. **Mark as Read**: Clicking a card or the read action updates `isRead: true` and captures `readAt`.
3. **Mark as Unread**: Reverts notification to `isRead: false`.
4. **Archive**: Removes notification from primary inbox view (`isArchived: true`), storing it under the `ARCHIVED` status filter for historical reference.
5. **Restore**: Moves an archived notification back to the primary active inbox (`isArchived: false`).
6. **Delete**: Permanently removes notification from cache and storage.

---

## 6. Supported Categories & Deep Links

The module supports 10 distinct domain categories with automated deep-linking:

| Category | Icon | Deep Link Destination |
| :--- | :--- | :--- |
| **Academic** | `GraduationCap` | `/academic` |
| **Planner** | `CalendarCheck` | `/planner?taskId={id}` |
| **Calendar** | `Calendar` | `/calendar?eventId={id}` |
| **Communities** | `Users` | `/communities/{id}` |
| **Councils** | `Shield` | `/councils/{id}` |
| **Resources** | `BookOpen` | `/resources?id={id}` |
| **Notices** | `ClipboardList` | `/notices?id={id}` |
| **Atlas** | `Compass` | `/academic` |
| **Authentication** | `KeyRound` | `/profile` |
| **System** | `Server` | `/notifications` |

---

## 7. Unauthorized Page Integration

The 403 Unauthorized page (`Unauthorized.tsx`) is registered under `/unauthorized` and integrated into `ProtectedRoute`.

### Features:
- Clear access denied badge and role context.
- **Dashboard Button**: Direct navigation to `/`.
- **Go Back Button**: Browser state navigation via `navigate(-1)`.
- Authentication state preservation during redirects.
