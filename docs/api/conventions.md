# CampusGuide API Conventions & Standards

## 1. RESTful URL Conventions
- Resource URIs use lowercase plural nouns (e.g. `/api/v1/courses`, `/api/v1/events`).
- Sub-resources follow hierarchical paths (e.g. `/api/v1/ai/conversations/{id}/messages`).
- Actions use standard HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).

## 2. Request & Response Payload Conventions
- **Naming**: All JSON fields use `camelCase`.
- **DTO Scoping**: Raw database entities are never exposed across HTTP boundaries. Controllers accept request DTOs (`Create*Request`, `Update*Request`) and return response DTOs (`*Response`, `*SummaryResponse`).
- **Temporal Types**:
  - Entity timestamps use `java.time.Instant` internally.
  - API responses project ISO-8601 strings (`2026-07-28T22:00:00`).

## 3. Standard HTTP Status Codes
- `200 OK`: Successful fetch, update, or action.
- `201 Created`: Successful creation of a new resource.
- `204 No Content`: Successful deletion or action with no response body.
- `400 Bad Request`: Validation errors, malformed JSON, or domain rule violations.
- `401 Unauthorized`: Missing, expired, or invalid JWT authentication.
- `403 Forbidden`: Authenticated user lacks necessary role permissions (`@PreAuthorize`).
- `404 Not Found`: Resource does not exist or user lacks permission to access it.
- `409 Conflict`: Unique constraint violation (e.g., duplicate slug, duplicate email).

## 4. Error Response Schema
All error responses adhere to a consistent error schema:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Title is mandatory",
  "timestamp": "2026-07-28T22:00:00.000Z",
  "path": "/api/v1/planner"
}
```
