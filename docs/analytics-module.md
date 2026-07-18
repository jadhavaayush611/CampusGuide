# Admin Analytics Module

The Admin Analytics module provides platform administrators with operational insights and high-level platform metrics. This module aggregates existing application data without introducing database redundancy or duplicating business logic.

---

## Architecture Overview

The analytics layer is built using Spring Boot's standard layered structure, querying existing MongoDB repositories using efficient count methods:

```
[AnalyticsController]
         ↓
 [AnalyticsService]
         ↓
[Existing Repositories] (UserRepository, RoadmapRepository, etc.)
```

---

## Dashboard Metrics

The complete platform analytics dashboard compiles the following metrics into a single response payload:

| Metric | Field | Description | Target Collection | Filtering Logic |
|---|---|---|---|---|
| **Total Users** | `totalUsers` | Total registered accounts on the platform | `users` | All documents |
| **Active Users** | `activeUsers` | Total email-verified users | `users` | `isVerified: true` |
| **Total Roadmaps** | `totalRoadmaps` | Total roadmaps created (including deleted/archived) | `roadmaps` | All documents |
| **Published Roadmaps** | `publishedRoadmaps` | Active, published academic roadmaps | `roadmaps` | `isDeleted: false` |
| **Total Communities** | `totalCommunities` | Total student communities created | `communities` | All documents |
| **Active Communities** | `activeCommunities` | Active communities currently open for members | `communities` | `isActive: true` |
| **Total Campus Events** | `totalEvents` | Non-deleted campus-wide events | `events` | `isDeleted: false` |
| **Upcoming Events** | `upcomingEvents` | Future, active (non-cancelled and non-deleted) events | `events` | `isDeleted: false`, `isCancelled: false`, `startTime >= now` |
| **Total AI Conversations** | `totalAiConversations` | Total chat-assistant sessions generated | `conversations` | All documents |
| **Total Notifications** | `totalNotifications` | Total notifications generated and sent | `notifications` | All documents |
| **Total Resources** | `totalResources` | Total active (non-deleted) uploaded files/resources | `resources` | `isDeleted: false` |

---

## Repository Aggregation Approach

To ensure optimal performance and avoid **N+1 queries**, the `AnalyticsService` does not fetch collections or iterate through documents. Instead, it delegates calculations directly to MongoDB query engine via Spring Data JPA/MongoDB count methods:

- `userRepository.countByIsVerifiedTrue()`
- `roadmapRepository.countByIsDeletedFalse()`
- `communityRepository.countByIsActiveTrue()`
- `eventRepository.countByIsDeletedFalse()`
- `eventRepository.countByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqual(LocalDateTime now)`
- `resourceRepository.countByIsDeletedFalse()`

---

## Security Restrictions

All endpoints in the Admin Analytics module are secured and strictly limited to administrators:

- **Role Requirement**: Access requires `ROLE_SUPER_ADMIN`.
- **Enforcement**: Configured declaratively at the class level in `AnalyticsController` using `@PreAuthorize("hasRole('SUPER_ADMIN')")`.
- **Response behavior**: Unauthenticated requests will return `401 Unauthorized`. Authenticated users without the required role (e.g., standard `ROLE_STUDENT`) will receive `403 Forbidden` with an `"Access Denied"` payload structured via the global exception handler.

---

## REST Endpoints

### 1. Dashboard Summary
* **Endpoint**: `GET /api/admin/analytics/dashboard`
* **Purpose**: Returns the consolidated platform metrics dashboard.
* **Authentication**: Required (JWT).
* **Authorization**: `SUPER_ADMIN`.
* **Success Response (`200 OK`)**:
  ```json
  {
    "totalUsers": 100,
    "activeUsers": 80,
    "totalRoadmaps": 50,
    "publishedRoadmaps": 45,
    "totalCommunities": 20,
    "activeCommunities": 18,
    "totalEvents": 30,
    "upcomingEvents": 5,
    "totalAiConversations": 200,
    "totalNotifications": 1000,
    "totalResources": 150,
    "generatedAt": "2026-07-18T09:46:14.123"
  }
  ```

### 2. User Statistics
* **Endpoint**: `GET /api/admin/analytics/users`
* **Purpose**: Reusable module-specific statistics for users.
* **Success Response (`200 OK`)**:
  ```json
  {
    "total": 100,
    "active": 80,
    "archived": 0
  }
  ```

### 3. Event Statistics
* **Endpoint**: `GET /api/admin/analytics/events`
* **Purpose**: Reusable module-specific statistics for events.
* **Success Response (`200 OK`)**:
  ```json
  {
    "total": 30,
    "active": 5,
    "archived": 0
  }
  ```

### 4. Community Statistics
* **Endpoint**: `GET /api/admin/analytics/communities`
* **Purpose**: Reusable module-specific statistics for communities.
* **Success Response (`200 OK`)**:
  ```json
  {
    "total": 20,
    "active": 18,
    "archived": 0
  }
  ```

---

## Future Considerations & Architecture Decisions (YAGNI)

In accordance with the **YAGNI (You Aren't Gonna Need It)** principle, the following designs were explicitly considered and deferred:

### 1. Metrics Snapshot DTO
* **Concept**: Creating a domain-level `DashboardMetrics` model separate from the API-level `DashboardSummaryResponse`.
* **Decision**: Defer. Currently unnecessary as there is no mapping mismatch or storage of historical analytics snapshots.

### 2. MongoDB Aggregation Pipelines
* **Concept**: Using a single MongoDB aggregation pipeline to calculate all counts in one round trip instead of running multiple independent `.count()` queries.
* **Decision**: Defer. While beneficial if metrics grow significantly (e.g., 20–30 metrics), it is not worthwhile today because the current simple count queries are highly efficient and execute in milliseconds.

### 3. Analytics API Versioning
* **Concept**: Adding a `"version": 1` field in the response payload for future frontend compatibility.
* **Decision**: Defer. Not needed now as there is no versioning complexity or API drift.
