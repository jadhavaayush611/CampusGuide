# Personal Domain Overview

## Overview
The Personal domain (`com.campusguide.personal`) manages user-centric sub-systems, including AI conversations, notifications, achievements, recommendations, student task planning, and personal calendar management.

## Modules

### 1. Planner (`com.campusguide.personal.planner`)
Manages personal tasks, study goals, assignment tracking, and event-linked planning.
- **Aggregate Root**: `PlannerTask`
- **Key Features**: Task creation, due date validation, reminder boundaries, status transitions, completed task immutability, ownership isolation.

### 2. Calendar (`com.campusguide.personal.calendar`)
Manages student personal calendar entries, academic schedules, task/event time-blocks, and range queries.
- **Aggregate Root**: `CalendarEntry`
- **Key Features**: Authenticated user ownership isolation, date-time range validation, mutual exclusivity for PlannerTask vs. Event references, all-day flag support, overlapping range queries.

### 3. Notifications (`com.campusguide.personal.notification`)
Delivers user notifications across campus events, academic updates, and personal reminders.
- **Aggregate Root**: `ScheduledNotification`
- **Key Features**: Authenticated user ownership, future schedule validation (`scheduledFor`), single aggregate reference enforcement, state transition machine (`SCHEDULED` -> `DELIVERED` -> `READ`, `CANCELLED` terminal state), automatic timestamping (`deliveredAt`, `readAt`), cross-channel delivery readiness (`IN_APP`, `PUSH`, `EMAIL`, `WEBSOCKET`, `SMS`), and Atlas AI integration support.

### 4. AI Gateway (`com.campusguide.personal.ai`)
Manages Atlas AI assistant conversations, message histories, and context generation.

### 5. Achievements (`com.campusguide.personal.achievement`)
Tracks student milestones, badges, skill progression, and academic accomplishments.
- **Aggregate Root**: `AchievementProgress`
- **Key Features**: Authenticated user ownership isolation, progress range validation (0-100), compound unique constraint `(userId, achievementCode)`, automatic `EARNED` transition and `earnedAt` timestamping, earned status immutability against downgrades, optional evidence URL and flexible metadata.


