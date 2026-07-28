# Scheduled Notification Module Overview

## Overview
The Scheduled Notification module (`com.campusguide.personal.notification`) manages time-based and event-triggered alerts, reminders, and delivery routing across the CampusGuide platform.

## Aggregate Root
- **`ScheduledNotification`**
  - Unique Primary Key: `UUID`
  - User Ownership: `@Indexed UUID userId` (resolved strictly via authentication context)
  - Scheduled Timing: `@Indexed LocalDateTime scheduledFor`
  - Delivery & Read Tracking: `deliveredAt`, `readAt`
  - Status: `@Indexed NotificationStatus` (`SCHEDULED`, `DELIVERED`, `READ`, `CANCELLED`)
  - Channel: `NotificationChannel` (`IN_APP`, `PUSH`, `EMAIL`, `WEBSOCKET`, `SMS`)
  - Priority: `NotificationPriority` (`LOW`, `NORMAL`, `HIGH`, `URGENT`)
  - Type: `NotificationType` (`REMINDER`, `EVENT`, `ACADEMIC`, `ACHIEVEMENT`, `SYSTEM`, `ATLAS_AI`)
  - Single Aggregate Reference: Optional link to at most one domain entity:
    - `linkedPlannerTaskId` (`UUID`)
    - `linkedCalendarEntryId` (`UUID`)
    - `linkedEventId` (`UUID`)
    - `linkedAchievementId` (`UUID`)
  - Metadata: `Map<String, Object>` for dynamic payload extensions and Atlas AI context data.

## Business Rules & Invariants
1. **Authenticated Ownership**: Notifications are strictly owned by the authenticated user. Client payloads must never include `userId`.
2. **Future Schedule Validation**: Newly created or updated notifications must specify a `scheduledFor` date-time strictly in the future (`isAfter(now)`).
3. **Single Entity Link Enforcement**: A notification can reference at most one external aggregate (`PlannerTask`, `CalendarEntry`, `Event`, or `AchievementProgress`).
4. **Reference Existence**: Any referenced entity ID must exist in its respective domain repository.
5. **State Transition Rules**:
   - `DELIVERED`: Automatically captures `deliveredAt` timestamp upon transition.
   - `READ`: Automatically captures `readAt` timestamp. Requires status to be `DELIVERED` first.
   - `CANCELLED`: Terminal state. Cannot transition back to `SCHEDULED`, `DELIVERED`, or `READ`.
   - `READ`: Terminal state. Cannot transition back to `SCHEDULED` or `DELIVERED`.

## Architecture & Layering
- **Controller**: `ScheduledNotificationController` (`/api/v1/scheduled-notifications`) handles REST API endpoints thin and delegating.
- **Service**: `ScheduledNotificationService` enforces business flow, transaction context, and user security checks.
- **Validator**: `ScheduledNotificationValidator` enforces domain invariants and state machine rules.
- **Mapper**: `ScheduledNotificationMapper` isolates Entity <-> DTO conversions.
- **Repository**: `ScheduledNotificationRepository` abstracts MongoDB queries for user collections and pending scheduler polls.

## Future Readiness & Extension Points
- **Background Worker & Scheduler Polling**: Supports high-efficiency indexing on `(status, scheduledFor)` for cron/worker processing.
- **Multi-channel Dispatch**: Prepared for push notifications, email routing, WebSocket streaming, and SMS alerts via `channel` enums.
- **Atlas AI Integration**: `metadata` map and `linked*` fields allow Atlas AI agents to inspect, schedule, and summarize student action items without schema modifications.
