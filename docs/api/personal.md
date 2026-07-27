# Personal Domain API Contract

## Base Endpoint: `/api/v1/planner`

All endpoints require authentication (`@PreAuthorize("isAuthenticated()")`). Bearer JWT token must be provided in `Authorization` header.

---

### 1. Create Planner Task
- **HTTP Method**: `POST`
- **Path**: `/api/v1/planner`
- **Request Body**:
```json
{
  "title": "Finish SE Assignment",
  "description": "Complete UML diagrams and Clean Architecture docs",
  "type": "ASSIGNMENT",
  "priority": "HIGH",
  "linkedEventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "dueAt": "2026-08-01T23:59:00",
  "reminderAt": "2026-08-01T09:00:00",
  "notes": "Review with team before submission"
}
```
- **Response Status**: `201 Created`
- **Response Body**: `PlannerTaskResponse`

---

### 2. Get All Planner Tasks
- **HTTP Method**: `GET`
- **Path**: `/api/v1/planner`
- **Response Status**: `200 OK`
- **Response Body**: `List<PlannerTaskResponse>` (sorted by `dueAt` ascending)

---

### 3. Get Planner Task by ID
- **HTTP Method**: `GET`
- **Path**: `/api/v1/planner/{id}`
- **Path Parameters**: `id` (UUID)
- **Response Status**: `200 OK`
- **Response Body**: `PlannerTaskResponse`

---

### 4. Update Planner Task
- **HTTP Method**: `PUT`
- **Path**: `/api/v1/planner/{id}`
- **Path Parameters**: `id` (UUID)
- **Request Body**:
```json
{
  "title": "Finish SE Assignment (Revised)",
  "description": "Updated assignment scope",
  "type": "ASSIGNMENT",
  "priority": "URGENT",
  "status": "IN_PROGRESS",
  "linkedEventId": null,
  "dueAt": "2026-08-02T18:00:00",
  "reminderAt": "2026-08-02T09:00:00",
  "notes": "Added test cases"
}
```
- **Response Status**: `200 OK`
- **Response Body**: `PlannerTaskResponse`

---

### 5. Update Task Status
- **HTTP Method**: `PATCH`
- **Path**: `/api/v1/planner/{id}/status`
- **Path Parameters**: `id` (UUID)
- **Request Body**:
```json
{
  "status": "COMPLETED"
}
```
- **Response Status**: `200 OK`
- **Response Body**: `PlannerTaskResponse`

---

### 6. Delete Planner Task
- **HTTP Method**: `DELETE`
- **Path**: `/api/v1/planner/{id}`
- **Path Parameters**: `id` (UUID)
- **Response Status**: `204 No Content`

---

## Calendar API Contract

### Base Endpoint: `/api/v1/calendar`

All endpoints require authentication (`@PreAuthorize("isAuthenticated()")`). Bearer JWT token must be provided in `Authorization` header.

---

### 1. Create Calendar Entry
- **HTTP Method**: `POST`
- **Path**: `/api/v1/calendar`
- **Request Body**:
```json
{
  "title": "Algorithms Lecture",
  "description": "Chapter 4 Algorithms",
  "type": "ACADEMIC",
  "linkedPlannerTaskId": null,
  "linkedEventId": null,
  "location": "Hall B",
  "startTime": "2026-08-15T10:00:00",
  "endTime": "2026-08-15T12:00:00",
  "isAllDay": false,
  "color": "#0000FF",
  "notes": "Bring notebook"
}
```
- **Response Status**: `201 Created`
- **Response Body**: `CalendarEntryResponse`

---

### 2. Get All Calendar Entries
- **HTTP Method**: `GET`
- **Path**: `/api/v1/calendar`
- **Response Status**: `200 OK`
- **Response Body**: `List<CalendarEntryResponse>` (sorted by `startTime` ascending, `endTime` ascending)

---

### 3. Get Calendar Entry by ID
- **HTTP Method**: `GET`
- **Path**: `/api/v1/calendar/{id}`
- **Path Parameters**: `id` (UUID)
- **Response Status**: `200 OK`
- **Response Body**: `CalendarEntryResponse`

---

### 4. Get Calendar Entries in Range
- **HTTP Method**: `GET`
- **Path**: `/api/v1/calendar/range`
- **Query Parameters**:
  - `from` (ISO LocalDateTime, required)
  - `to` (ISO LocalDateTime, required)
- **Response Status**: `200 OK`
- **Response Body**: `List<CalendarEntryResponse>` (returns overlapping entries sorted chronologically)

---

### 5. Update Calendar Entry
- **HTTP Method**: `PUT`
- **Path**: `/api/v1/calendar/{id}`
- **Path Parameters**: `id` (UUID)
- **Request Body**:
```json
{
  "title": "Algorithms Lecture (Rescheduled)",
  "description": "Chapter 4 & 5",
  "type": "ACADEMIC",
  "linkedPlannerTaskId": null,
  "linkedEventId": null,
  "location": "Hall C",
  "startTime": "2026-08-15T11:00:00",
  "endTime": "2026-08-15T13:00:00",
  "isAllDay": false,
  "color": "#0000FF",
  "notes": "Revised timing"
}
```
- **Response Status**: `200 OK`
- **Response Body**: `CalendarEntryResponse`

---

### 6. Delete Calendar Entry
- **HTTP Method**: `DELETE`
- **Path**: `/api/v1/calendar/{id}`
- **Path Parameters**: `id` (UUID)
- **Response Status**: `204 No Content`

---

## Achievement API Contract

### Base Endpoint: `/api/v1/achievements`

All endpoints require authentication (`@PreAuthorize("isAuthenticated()")`). Bearer JWT token must be provided in `Authorization` header.

---

### 1. Create Achievement Progress
- **HTTP Method**: `POST`
- **Path**: `/api/v1/achievements`
- **Request Body**:
```json
{
  "achievementCode": "CODE_FIRST_A",
  "title": "First Straight A",
  "description": "Earned an A grade in all courses",
  "category": "ACADEMIC",
  "progress": 25,
  "evidenceUrl": "https://example.com/transcript.pdf",
  "metadata": {
    "term": "Fall 2026"
  }
}
```
- **Response Status**: `201 Created`
- **Response Body**: `AchievementProgressResponse`

---

### 2. Get All Achievements
- **HTTP Method**: `GET`
- **Path**: `/api/v1/achievements`
- **Query Parameters**:
  - `category` (`AchievementCategory`, optional)
  - `status` (`AchievementStatus`, optional)
- **Response Status**: `200 OK`
- **Response Body**: `List<AchievementProgressResponse>`

---

### 3. Get Achievement by ID
- **HTTP Method**: `GET`
- **Path**: `/api/v1/achievements/{id}`
- **Path Parameters**: `id` (UUID)
- **Response Status**: `200 OK`
- **Response Body**: `AchievementProgressResponse`

---

### 4. Update Achievement Progress
- **HTTP Method**: `PATCH`
- **Path**: `/api/v1/achievements/{id}/progress`
- **Path Parameters**: `id` (UUID)
- **Request Body**:
```json
{
  "progress": 100
}
```
- **Response Status**: `200 OK`
- **Response Body**: `AchievementProgressResponse`

---

### 5. Update Achievement Details
- **HTTP Method**: `PUT`
- **Path**: `/api/v1/achievements/{id}`
- **Path Parameters**: `id` (UUID)
- **Request Body**:
```json
{
  "title": "First Straight A (Verified)",
  "description": "Earned an A grade in all courses with high honors",
  "category": "ACADEMIC",
  "progress": 100,
  "evidenceUrl": "https://example.com/transcript_official.pdf",
  "metadata": {
    "term": "Fall 2026",
    "honors": true
  }
}
```
- **Response Status**: `200 OK`
- **Response Body**: `AchievementProgressResponse`

---

### 6. Delete Achievement Progress
- **HTTP Method**: `DELETE`
- **Path**: `/api/v1/achievements/{id}`
- **Path Parameters**: `id` (UUID)
- **Response Status**: `204 No Content`

---

## Response Status & Error Codes

| Status Code | Description | Scenario |
|---|---|---|
| `200 OK` | Success | Fetch or update successful |
| `201 Created` | Resource Created | New planner task, calendar entry, or achievement created |
| `204 No Content` | Deleted | Task, calendar entry, or achievement deleted |
| `400 Bad Request` | Validation Error | Mandatory fields missing, invalid time range, progress out of bounds [0, 100], or attempting to downgrade EARNED achievement |
| `401 Unauthorized` | Unauthenticated | Missing or invalid JWT token |
| `403 Forbidden` | Access Denied | User trying to access another user's resource |
| `404 Not Found` | Not Found | Resource ID does not exist |
| `409 Conflict` | Conflict | Achievement code already exists for the user |


