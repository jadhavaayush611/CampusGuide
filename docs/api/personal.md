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

## Response Status & Error Codes

| Status Code | Description | Scenario |
|---|---|---|
| `200 OK` | Success | Fetch, update, or patch successful |
| `201 Created` | Resource Created | New planner task created |
| `204 No Content` | Deleted | Task deleted |
| `400 Bad Request` | Validation Error | Missing mandatory title/type/priority, dueAt preceding createdAt, reminderAt not before dueAt, or modifying non-notes on completed task |
| `401 Unauthorized` | Unauthenticated | Missing or invalid JWT token |
| `403 Forbidden` | Access Denied | User trying to access another user's task |
| `404 Not Found` | Not Found | Task ID or linked Event ID does not exist |
