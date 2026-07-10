# CampusGuide API Contracts (MVP)

## Authentication

POST /api/auth/register

POST /api/auth/login

GET /api/auth/me

---

## Users

GET /api/users/{id}

PUT /api/users/{id}

---

## Councils

GET /api/councils

GET /api/councils/{id}

POST /api/councils

PUT /api/councils/{id}

GET /api/councils/category/{category}

---

## Membership Applications (Planned / Future Phase)

POST /api/memberships/apply

GET /api/memberships

PUT /api/memberships/{id}/approve

PUT /api/memberships/{id}/reject

---

## Communities

GET /api/communities

GET /api/communities/{id}

POST /api/communities

PUT /api/communities/{id}

GET /api/communities/councils/{councilId}/communities

---

## Posts

POST /api/posts

GET /api/posts

GET /api/posts/{id}

PUT /api/posts/{id}

DELETE /api/posts/{id}

GET /api/posts/community/{communityId}

GET /api/posts/author/{authorId}

---

## Comments

POST /api/comments

GET /api/comments/{id}

PUT /api/comments/{id}

DELETE /api/comments/{id}

GET /api/comments/post/{postId}

GET /api/comments/author/{authorId}

---

## Events

### POST /api/events
* **Purpose**: Create a new event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (1-150 characters, required)",
    "description": "String (1-5000 characters, required)",
    "councilId": "String (required, must exist)",
    "location": "String (1-200 characters, required)",
    "startTime": "String (ISO-8601 DateTime, e.g. 2026-07-05T12:00:00, required)",
    "endTime": "String (ISO-8601 DateTime, required, must be after startTime)",
    "registrationDeadline": "String (ISO-8601 DateTime, required, must be before startTime)",
    "maxParticipants": "Integer (minimum 1, optional)",
    "imageUrl": "String (optional)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "title": "String",
    "description": "String",
    "councilId": "String",
    "organizerId": "String",
    "location": "String",
    "startTime": "String (ISO-8601 DateTime)",
    "endTime": "String (ISO-8601 DateTime)",
    "registrationDeadline": "String (ISO-8601 DateTime)",
    "maxParticipants": 100,
    "attendeeCount": 0,
    "imageUrl": "String or null",
    "isCancelled": false,
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (empty required fields, invalid text length, end time before start time, or registration deadline after start time).
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Council or user not found.

### PUT /api/events/{eventId}
* **Purpose**: Update an existing event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Organizer of the event or `SUPER_ADMIN`.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (max 150 characters, optional)",
    "description": "String (max 5000 characters, optional)",
    "location": "String (max 200 characters, optional)",
    "startTime": "String (ISO-8601 DateTime, optional)",
    "endTime": "String (ISO-8601 DateTime, optional)",
    "registrationDeadline": "String (ISO-8601 DateTime, optional)",
    "maxParticipants": "Integer (minimum 1, optional)",
    "imageUrl": "String (optional)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/events`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not the organizer of the event or a `SUPER_ADMIN`.
  - `404 Not Found`: Event not found or soft-deleted.

### DELETE /api/events/{eventId}
* **Purpose**: Soft delete an existing event (sets `isDeleted` to true).
* **Authentication**: Required (JWT Token).
* **Authorization**: Organizer of the event or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `204 No Content`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not the organizer of the event or a `SUPER_ADMIN`.
  - `404 Not Found`: Event not found or already soft-deleted.

### GET /api/events
* **Purpose**: Retrieve all active (non-deleted) events in the system, sorted by start time ascending.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "String",
      "title": "String",
      "councilId": "String",
      "location": "String",
      "startTime": "String (ISO-8601 DateTime)",
      "attendeeCount": 0,
      "maxParticipants": 100,
      "imageUrl": "String or null"
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/events/upcoming
* **Purpose**: Retrieve all upcoming active and non-cancelled events (start time greater than or equal to current time), sorted by start time ascending.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of event summaries, same format as `GET /api/events`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/events/{eventId}
* **Purpose**: Retrieve detailed information of a specific event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/events`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Event not found or soft-deleted.

### GET /api/events/council/{councilId}
* **Purpose**: Retrieve all active events associated with a specific council.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of event summaries, same format as `GET /api/events`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Council not found.

---

## Event Registrations

### POST /api/events/{eventId}/register
* **Purpose**: Register the authenticated user for an event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/events` (returns updated event details).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Cannot register for a cancelled event, past event, event after registration deadline, or when event capacity is reached.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Event not found or soft-deleted.
  - `409 Conflict`: User is already registered for this event.

### DELETE /api/events/{eventId}/register
* **Purpose**: Cancel the authenticated user's registration for an event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user who is currently registered.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/events` (returns updated event details).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: User is not registered for this event.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Event not found or soft-deleted.

### GET /api/events/{eventId}/registration-status
* **Purpose**: Retrieve the registration status of the authenticated user for a specific event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  {
    "registered": true
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Event not found or soft-deleted.

### GET /api/events/{eventId}/registrations
* **Purpose**: Retrieve a list of user IDs registered for a specific event.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    "userId1",
    "userId2"
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Event not found or soft-deleted.

---

## Resources

### POST /api/resources
* **Purpose**: Upload a new resource and its metadata.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user (including `STUDENT`, `FACULTY`, `COUNCIL_ADMIN`, `SUPER_ADMIN`).
* **Request Format**: `multipart/form-data`
* **Multipart Parameters**:
  - `file`: MultipartFile (required). Must be a non-empty file. Supported MIME types:
    - `application/pdf`
    - `application/msword`
    - `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
    - `application/vnd.ms-powerpoint`
    - `application/vnd.openxmlformats-officedocument.presentationml.presentation`
    - `application/vnd.ms-excel`
    - `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
    - `image/jpeg`
    - `image/png`
    - *Maximum file size: 20MB.*
  - `title`: String (1-200 characters, required).
  - `description`: String (max 2000 characters, optional).
  - `councilId`: String (optional, must exist if provided).
  - `communityId`: String (optional, must exist if provided).
  - `tags`: List of Strings (max 20 tags, optional).
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "title": "String",
    "description": "String or null",
    "uploaderId": "String",
    "councilId": "String or null",
    "communityId": "String or null",
    "tags": ["String"],
    "fileName": "String (UUID-based unique stored filename)",
    "originalFileName": "String",
    "fileType": "String (MIME type)",
    "fileSize": 1024,
    "downloadUrl": "String (relative path /api/resources/download/{resourceId})",
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (missing/empty file, file size > 20MB, unsupported MIME type, missing title, title > 200 characters, description > 2000 characters, or > 20 tags).
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Council or community not found for the provided IDs.

### PUT /api/resources/{resourceId}
* **Purpose**: Update metadata of an existing resource.
* **Authentication**: Required (JWT Token).
* **Authorization**: The uploader (owner) of the resource or `SUPER_ADMIN`.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (max 200 characters, optional)",
    "description": "String (max 2000 characters, optional)",
    "tags": ["String"] (max 20 items, optional)
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/resources`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (title > 200 characters, description > 2000 characters, or > 20 tags).
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not the uploader/owner of the resource or `SUPER_ADMIN`.
  - `404 Not Found`: Resource not found or soft-deleted.

### DELETE /api/resources/{resourceId}
* **Purpose**: Soft delete an existing resource (sets `isDeleted` to true).
* **Authentication**: Required (JWT Token).
* **Authorization**: The uploader (owner) of the resource or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `204 No Content`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not the uploader/owner of the resource or `SUPER_ADMIN`.
  - `404 Not Found`: Resource not found or already soft-deleted.

### GET /api/resources
* **Purpose**: Retrieve all active (non-deleted) resources, sorted by creation date descending.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "String",
      "title": "String",
      "fileType": "String",
      "fileSize": 1024,
      "uploaderId": "String",
      "createdAt": "String (ISO-8601 DateTime)"
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/resources/{resourceId}
* **Purpose**: Retrieve detailed metadata of a specific resource.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/resources`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Resource not found or soft-deleted.

### GET /api/resources/search
* **Purpose**: Search active resources by matching query string case-insensitively with title or description.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Parameters**:
  - `query`: String (required).
* **Response Body** (application/json):
  List of resource summaries (same format as `GET /api/resources`).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/resources/recent
* **Purpose**: Retrieve the latest active resources, sorted by creation date descending.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of resource summaries (same format as `GET /api/resources`).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/resources/tag/{tag}
* **Purpose**: Retrieve active resources filtered by tag (case-insensitive).
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Path Parameters**:
  - `tag`: String (required).
* **Response Body** (application/json):
  List of resource summaries (same format as `GET /api/resources`).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Tag parameter is blank.
  - `401 Unauthorized`: Unauthenticated.

### GET /api/resources/uploader/{uploaderId}
* **Purpose**: Retrieve active resources uploaded by a specific user.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Path Parameters**:
  - `uploaderId`: String (required).
* **Response Body** (application/json):
  List of resource summaries (same format as `GET /api/resources`).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Uploader user not found.

### GET /api/resources/council/{councilId}
* **Purpose**: Retrieve active resources associated with a specific council.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Path Parameters**:
  - `councilId`: String (required).
* **Response Body** (application/json):
  List of resource summaries (same format as `GET /api/resources`).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Council not found.

### GET /api/resources/community/{communityId}
* **Purpose**: Retrieve active resources associated with a specific community.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Path Parameters**:
  - `communityId`: String (required).
* **Response Body** (application/json):
  List of resource summaries (same format as `GET /api/resources`).
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Community not found.

### GET /api/resources/download/{resourceId}
* **Purpose**: Download the physical file associated with the resource.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Path Parameters**:
  - `resourceId`: String (required).
* **Response Headers**:
  - `Content-Type`: Set to the stored resource's MIME type (e.g. `application/pdf`, `image/png`, etc.).
  - `Content-Disposition`: `attachment; filename="original_filename.ext"`
* **Response Body**: Binary file stream.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Resource not found, soft-deleted, or physical file missing in storage.

---

## Resource Requests (Planned / Future Phase)

POST /api/resources/{id}/request

PUT /api/resource-requests/{id}/approve

PUT /api/resource-requests/{id}/reject

---

## Announcements

GET /api/announcements

POST /api/announcements

---

## Notices

GET /api/notices

POST /api/notices

---

## Notifications

GET /api/notifications

PUT /api/notifications/{id}/read

---

## Vault

GET /api/vault

POST /api/vault/upload

DELETE /api/vault/{id}

---

## Roadmaps

GET /api/roadmaps

GET /api/roadmaps/{id}

---

## Resume Builder

POST /api/resume

GET /api/resume

GET /api/resume/pdf
