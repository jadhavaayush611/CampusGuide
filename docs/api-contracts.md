# CampusGuide API Contracts (MVP)

## Authentication

POST /api/auth/register

POST /api/auth/login

GET /api/auth/me

---

## Users (Planned / Future Phase)

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

## Global Search

POST /api/search

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

## Announcements (Planned / Future Phase)

GET /api/announcements

POST /api/announcements

---

## Notices (Planned / Future Phase)

GET /api/notices

POST /api/notices

---

## Notifications

Every endpoint in this section operates only on the authenticated user's notifications.

### GET /api/notifications
* **Purpose**: Retrieve all notifications for the authenticated user, paginated.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Query Parameters**:
  - `page` (optional): Page number (0-based, default: 0).
  - `size` (optional): Page size (default: 20).
  - `sort` (optional): Fields to sort by (default: `createdAt,desc`).
* **Request Body**: None
* **Response Body** (application/json):
  A standard Spring Page structure containing a list of notification responses:
  ```json
  {
    "content": [
      {
        "id": "60c72b2f9b1d8a2a4c8e9b01",
        "title": "Roadmap Published",
        "message": "Your academic roadmap 'CS Roadmap' has been successfully published.",
        "type": "ACADEMIC",
        "priority": "NORMAL",
        "read": false,
        "createdAt": "2026-07-17T19:20:00"
      }
    ],
    "page": {
      "size": 20,
      "number": 0,
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/notifications/unread
* **Purpose**: Retrieve all unread notifications for the authenticated user, paginated.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Query Parameters**: Same as `GET /api/notifications`.
* **Request Body**: None
* **Response Body** (application/json): Same as `GET /api/notifications`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/notifications/unread/count
* **Purpose**: Retrieve the count of unread notifications for the authenticated user.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  {
    "count": 1
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### PATCH /api/notifications/{id}/read
* **Purpose**: Mark a specific notification as read.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner of the notification.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  {
    "id": "60c72b2f9b1d8a2a4c8e9b01",
    "title": "Roadmap Published",
    "message": "Your academic roadmap 'CS Roadmap' has been successfully published.",
    "type": "ACADEMIC",
    "priority": "NORMAL",
    "read": true,
    "createdAt": "2026-07-17T19:20:00"
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Mismatched ownership.
  - `404 Not Found`: Notification not found.

### PATCH /api/notifications/read-all
* **Purpose**: Mark all unread notifications for the authenticated user as read.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### DELETE /api/notifications/{id}
* **Purpose**: Delete a specific notification.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner of the notification.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `204 No Content`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Mismatched ownership.
  - `404 Not Found`: Notification not found.

---

## Vault (Planned / Future Phase)

GET /api/vault

POST /api/vault/upload

DELETE /api/vault/{id}

---

## Academic Roadmaps

### POST /api/roadmaps
* **Purpose**: Create a new academic roadmap.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (1-200 characters, required)",
    "description": "String (max 2000 characters, optional)",
    "degreeProgram": "String (required)",
    "department": "String (required)",
    "totalCredits": "Integer (minimum 1, required)",
    "expectedGraduationYear": "Integer (minimum 2000, required)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "title": "String",
    "description": "String",
    "degreeProgram": "String",
    "department": "String",
    "totalCredits": 120,
    "expectedGraduationYear": 2028,
    "createdBy": "String (User ID)",
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (blank title, program, department, invalid range for totalCredits or graduationYear).
  - `401 Unauthorized`: Unauthenticated.

### PUT /api/roadmaps/{roadmapId}
* **Purpose**: Update an existing academic roadmap.
* **Authentication**: Required (JWT Token).
* **Authorization**: The creator of the roadmap or `SUPER_ADMIN`.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (max 200 characters, optional)",
    "description": "String (max 2000 characters, optional)",
    "degreeProgram": "String (optional)",
    "department": "String (optional)",
    "totalCredits": "Integer (minimum 1, optional)",
    "expectedGraduationYear": "Integer (minimum 2000, optional)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/roadmaps`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not the creator and not a `SUPER_ADMIN`.
  - `404 Not Found`: Roadmap not found or soft-deleted.

### DELETE /api/roadmaps/{roadmapId}
* **Purpose**: Soft delete an existing roadmap (sets `isDeleted` to true).
* **Authentication**: Required (JWT Token).
* **Authorization**: The creator of the roadmap or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `204 No Content`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not the creator and not a `SUPER_ADMIN`.
  - `404 Not Found`: Roadmap not found or already deleted.

### GET /api/roadmaps
* **Purpose**: Retrieve all active (non-deleted) roadmaps, sorted by creation date descending.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "String",
      "title": "String",
      "degreeProgram": "String",
      "department": "String",
      "expectedGraduationYear": 2028
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/roadmaps/{roadmapId}
* **Purpose**: Retrieve details of a specific active roadmap.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/roadmaps`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Roadmap not found or soft-deleted.

### GET /api/roadmaps/creator/{userId}
* **Purpose**: Retrieve all active roadmaps created by a specific user.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of roadmap summaries, same format as `GET /api/roadmaps`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Creator user not found.

### GET /api/roadmaps/degree/{degreeProgram}
* **Purpose**: Retrieve active roadmaps for a specific degree program (case-insensitive).
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of roadmap summaries, same format as `GET /api/roadmaps`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Degree program is blank.
  - `401 Unauthorized`: Unauthenticated.

### GET /api/roadmaps/department/{department}
* **Purpose**: Retrieve active roadmaps for a specific department (case-insensitive).
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of roadmap summaries, same format as `GET /api/roadmaps`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Department is blank.
  - `401 Unauthorized`: Unauthenticated.

---

## Courses

### POST /api/courses
* **Purpose**: Create a new course in the catalog.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` only.
* **Request Body** (application/json):
  ```json
  {
    "courseCode": "String (1-20 characters, required, unique)",
    "courseName": "String (1-200 characters, required)",
    "description": "String (max 2000 characters, optional)",
    "department": "String (required)",
    "credits": "Integer (minimum 1, required)",
    "semester": "Integer (minimum 1, required)",
    "prerequisiteCourseIds": "List of Strings (optional)",
    "elective": "Boolean (required)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "courseCode": "String",
    "courseName": "String",
    "description": "String",
    "department": "String",
    "credits": 4,
    "semester": 1,
    "prerequisiteCourseIds": ["String"],
    "elective": false,
    "active": true,
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (missing required fields, credits < 1, semester < 1).
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User does not have `SUPER_ADMIN` role.
  - `409 Conflict`: Course code already exists.

### PUT /api/courses/{courseId}
* **Purpose**: Update an existing course's details.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` only.
* **Request Body** (application/json):
  ```json
  {
    "courseCode": "String (max 20 characters, optional)",
    "courseName": "String (max 200 characters, optional)",
    "description": "String (max 2000 characters, optional)",
    "department": "String (optional)",
    "credits": "Integer (minimum 1, optional)",
    "semester": "Integer (minimum 1, optional)",
    "prerequisiteCourseIds": "List of Strings (optional)",
    "elective": "Boolean (optional)",
    "active": "Boolean (optional)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/courses`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User does not have `SUPER_ADMIN` role.
  - `404 Not Found`: Course not found.
  - `409 Conflict`: Course code already exists on another course.

### DELETE /api/courses/{courseId}
* **Purpose**: Delete a course from the catalog.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` only.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `204 No Content`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User does not have `SUPER_ADMIN` role.
  - `404 Not Found`: Course not found.

### GET /api/courses
* **Purpose**: Retrieve all courses in the system.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "String",
      "courseCode": "String",
      "courseName": "String",
      "department": "String",
      "credits": 4,
      "semester": 1,
      "elective": false
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/courses/{courseId}
* **Purpose**: Retrieve detailed information of a specific course.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/courses`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Course not found or inactive.

### GET /api/courses/department/{department}
* **Purpose**: Retrieve all courses belonging to a specific department.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of course summaries, same format as `GET /api/courses`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/courses/semester/{semester}
* **Purpose**: Retrieve all courses scheduled for a specific semester number.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of course summaries, same format as `GET /api/courses`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/courses/electives
* **Purpose**: Retrieve all elective courses.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of course summaries, same format as `GET /api/courses`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/courses/mandatory
* **Purpose**: Retrieve all mandatory (non-elective) courses.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  List of course summaries, same format as `GET /api/courses`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

---

## Student Progress

### POST /api/progress
* **Purpose**: Initialize a student progress record. One progress record is allowed per student.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Body** (application/json):
  ```json
  {
    "roadmapId": "String (required, must exist)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "studentId": "String",
    "roadmapId": "String",
    "completedCourseIds": [],
    "currentSemester": 1,
    "totalCreditsEarned": 0,
    "currentGpa": 0.0,
    "graduationEligible": false,
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Roadmap not found.
  - `409 Conflict`: Progress record already exists for the student.

### PUT /api/progress
* **Purpose**: Update student progress metrics (permitted fields only). Students can update their own progress. `SUPER_ADMIN` can specify `studentId` to update other students. Restricted academic record fields (`currentGpa`, `totalCreditsEarned`, `graduationEligible`) are server-controlled and cannot be edited via this endpoint.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body** (application/json):
  ```json
  {
    "studentId": "String (optional, for SUPER_ADMIN use only)",
    "roadmapId": "String (optional)",
    "currentSemester": "Integer (minimum 1, optional)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (semester < 1, etc.).
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Progress record not found.

### PUT /api/progress/admin
* **Purpose**: Perform administrative academic record updates on student progress records (e.g. GPA). Only accessible by `SUPER_ADMIN`.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` only.
* **Request Body** (application/json):
  ```json
  {
    "studentId": "String (required)",
    "roadmapId": "String (optional)",
    "currentSemester": "Integer (minimum 1, optional)",
    "currentGpa": "Double (0.0 to 10.0, optional)",
    "totalCreditsEarned": "Integer (minimum 0, optional, server-calculated and derived)",
    "graduationEligible": "Boolean (optional, server-calculated and derived)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (GPA out of range, semester < 1, missing studentId, etc.).
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User does not have `SUPER_ADMIN` role.
  - `404 Not Found`: Student progress record not found.

### GET /api/progress
* **Purpose**: Retrieve the progress record of the authenticated student.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Student progress record does not exist.

### PATCH /api/progress/complete/{courseId}
* **Purpose**: Mark a course as completed, adding its credits to the student's total credits.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Parameters**:
  - `studentId` (Query parameter, optional, for `SUPER_ADMIN` use only)
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Course or progress record not found.
  - `409 Conflict`: Course is already marked completed.

### PATCH /api/progress/remove/{courseId}
* **Purpose**: Remove a course from the completed courses list, deducting its credits (never allows total credits to drop below 0).
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Parameters**:
  - `studentId` (Query parameter, optional, for `SUPER_ADMIN` use only)
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Course is not marked completed.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Course or progress record not found.

### GET /api/progress/student/{studentId}
* **Purpose**: Retrieve the progress record of a specific student ID.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` only.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User does not have `SUPER_ADMIN` role.
  - `404 Not Found`: Student progress record not found.

---

## Semester Planner

### POST /api/semester-plans
* **Purpose**: Initialize a semester plan. Enforces one plan per student per semester number.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Body** (application/json):
  ```json
  {
    "roadmapId": "String (required)",
    "semesterNumber": "Integer (minimum 1, required)",
    "plannedCourseIds": "List of Strings (optional)",
    "finalized": "Boolean (optional)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "studentId": "String",
    "roadmapId": "String",
    "semesterNumber": 1,
    "plannedCourseIds": ["String"],
    "totalPlannedCredits": 16,
    "finalized": false,
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Student progress or roadmap not found.
  - `409 Conflict`: Semester plan already exists for this student and semester.

### PUT /api/semester-plans/{planId}
* **Purpose**: Update a semester plan. Cannot update a plan once finalized.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body** (application/json):
  ```json
  {
    "roadmapId": "String (optional)",
    "semesterNumber": "Integer (minimum 1, optional)",
    "plannedCourseIds": "List of Strings (optional)",
    "finalized": "Boolean (optional)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/semester-plans`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Modified a finalized plan, finalized an empty plan, or inactive/non-existent course.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Semester plan not found.
  - `409 Conflict`: Update conflicts with another semester number plan.

### GET /api/semester-plans
* **Purpose**: Retrieve all semester plans of the authenticated student, sorted by semester number ascending.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "String",
      "studentId": "String",
      "roadmapId": "String",
      "semesterNumber": 1,
      "plannedCourseIds": ["String"],
      "totalPlannedCredits": 16,
      "finalized": false,
      "createdAt": "String (ISO-8601 DateTime)",
      "updatedAt": "String (ISO-8601 DateTime)"
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/semester-plans/{planId}
* **Purpose**: Retrieve details of a specific semester plan.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/semester-plans`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Semester plan not found.

### PATCH /api/semester-plans/{planId}/add/{courseId}
* **Purpose**: Add a course to the semester plan. Checks that all course prerequisites are met in student's completed courses.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/semester-plans`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Plan is already finalized, prerequisite not met, or student progress not found.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Plan or course not found.
  - `409 Conflict`: Course already added to this plan.

### PATCH /api/semester-plans/{planId}/remove/{courseId}
* **Purpose**: Remove a course from the semester plan, deducting credits.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/semester-plans`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Plan is already finalized, or course not present in plan.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Plan or course not found.

### PATCH /api/semester-plans/{planId}/finalize
* **Purpose**: Finalize a semester plan (no further modifications allowed). Cannot finalize an empty plan.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body**: None
* **Response Body** (application/json):
  Same format as `POST /api/semester-plans`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Plan is empty or already finalized.
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Plan not found.

### GET /api/semester-plans/student/{studentId}
* **Purpose**: Retrieve semester plans for a specific student ID.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` only.
* **Request Body**: None
* **Response Body** (application/json):
  List of semester plans, same format as `GET /api/semester-plans`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User does not have `SUPER_ADMIN` role.
  - `404 Not Found`: Student progress or plans not found.

---

## Academic Integration

### GET /api/academic/dashboard
* **Purpose**: Retrieve the consolidated academic dashboard metrics for the authenticated student.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  {
    "roadmapTitle": "String",
    "degreeProgram": "String",
    "department": "String",
    "currentSemester": 3,
    "totalCreditsRequired": 120,
    "totalCreditsEarned": 45,
    "remainingCredits": 75,
    "completionPercentage": 37.5,
    "currentGpa": 8.5,
    "graduationEligible": false,
    "plannedCredits": 16,
    "finalizedSemesterPlan": true,
    "completedCourses": [
      {
        "id": "String",
        "courseCode": "String",
        "courseName": "String",
        "credits": 4,
        "semester": 1,
        "elective": false,
        "active": true
      }
    ],
    "remainingCourses": [
      {
        "id": "String",
        "courseCode": "String",
        "courseName": "String",
        "credits": 4,
        "semester": 4,
        "elective": false,
        "active": true
      }
    ]
  }
  ```
* **Success Status Codes**: `200 OK`
* **Dashboard Calculations**:
  - `totalCreditsRequired`: Evaluated from the associated Roadmap's `totalCredits`.
  - `totalCreditsEarned`: Taken directly from the StudentProgress's `totalCreditsEarned`.
  - `remainingCredits`: Calculated as `totalCreditsRequired - totalCreditsEarned`.
  - `completionPercentage`: Aggregated as `(totalCreditsEarned / totalCreditsRequired) * 100` (rounded to 2 decimal places).
  - `plannedCredits`: Summed from `totalPlannedCredits` across all of the student's semester plans.
  - `finalizedSemesterPlan`: Flag indicating if the student's current semester plan is marked finalized.
  - `completedCourses`: Resolves detailed `CourseResponse` models for the student's completed courses list.
  - `remainingCourses`: Lists active courses under the student's department that have not been completed and are not included in any semester plan.
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Student progress or associated roadmap not found.

### GET /api/academic/progress
* **Purpose**: Retrieve simple progress tracking lists and metrics for the authenticated student.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  {
    "completedCourseIds": ["String"],
    "plannedCourseIds": ["String"],
    "remainingCourseIds": ["String"],
    "creditsEarned": 45,
    "creditsRemaining": 75,
    "completionPercentage": 37.5
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Student progress or associated roadmap not found.

### GET /api/academic/recommended-semester
* **Purpose**: Generate course recommendations and prerequisite warnings for a targeted semester.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated student.
* **Request Parameters**:
  - `semesterNumber` (Query parameter, optional, defaults to student's `currentSemester + 1`)
* **Response Body** (application/json):
  ```json
  {
    "semesterNumber": 4,
    "recommendedCourseIds": ["String"],
    "totalCredits": 16,
    "prerequisiteWarnings": ["String"]
  }
  ```
* **Success Status Codes**: `200 OK`
* **Recommendation Logic**:
  - Identifies target semester (either provided parameter or `currentSemester + 1`).
  - Fetches active courses matching this semester and matching the student's roadmap department.
  - Excludes courses that the student has already completed.
  - Performs Prerequisite Checks:
    - If all prerequisite courses are in the student's completed courses list, the course ID is recommended and its credits contribute to `totalCredits`.
    - If any prerequisite is missing, the course is omitted and a warning description is added to `prerequisiteWarnings` (e.g. `"Course {courseCode} requires prerequisite {prereqCode} which is not completed."`).
* **Error Status Codes**:
  - `400 Bad Request`: Targeted semester is less than or equal to 0.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Student progress or associated roadmap not found.

---

## Resume Builder

POST /api/resume

GET /api/resume

GET /api/resume/pdf

---

## AI Assistant

### POST /api/ai/conversations
* **Purpose**: Create a new AI assistant conversation.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (1-100 characters, required)",
    "type": "ConversationType (GENERAL_CHAT, ACADEMIC_ADVISOR, CAREER_GUIDANCE, CAMPUS_ASSISTANT, required)",
    "metadata": "Map of key-value pairs (optional)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "userId": "String",
    "title": "String",
    "type": "ConversationType",
    "metadata": {},
    "active": true,
    "createdAt": "String (ISO-8601 DateTime)",
    "updatedAt": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (title blank/too long, type missing/invalid).
  - `401 Unauthorized`: Unauthenticated.

### GET /api/ai/conversations
* **Purpose**: Retrieve the list of active conversations for the authenticated user.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "String",
      "title": "String",
      "type": "ConversationType",
      "active": true,
      "createdAt": "String (ISO-8601 DateTime)",
      "updatedAt": "String (ISO-8601 DateTime)"
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/ai/conversations/{id}
* **Purpose**: Retrieve conversation details along with its chronological message history.
* **Authentication**: Required (JWT Token).
* **Authorization**: Conversation owner only.
* **Request Body**: None
* **Response Body** (application/json):
  ```json
  {
    "conversation": {
      "id": "String",
      "userId": "String",
      "title": "String",
      "type": "ConversationType",
      "metadata": {},
      "active": true,
      "createdAt": "String (ISO-8601 DateTime)",
      "updatedAt": "String (ISO-8601 DateTime)"
    },
    "messages": [
      {
        "id": "String",
        "conversationId": "String",
        "role": "MessageRole (USER, ASSISTANT, SYSTEM)",
        "content": "String",
        "metadata": {},
        "timestamp": "String (ISO-8601 DateTime)"
      }
    ]
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Conversation not found or belongs to another user.

### PUT /api/ai/conversations/{id}
* **Purpose**: Rename/update conversation title.
* **Authentication**: Required (JWT Token).
* **Authorization**: Conversation owner only.
* **Request Body** (application/json):
  ```json
  {
    "title": "String (1-100 characters, required)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/ai/conversations`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Conversation not found or belongs to another user.

### DELETE /api/ai/conversations/{id}
* **Purpose**: Soft delete a conversation (sets active to false).
* **Authentication**: Required (JWT Token).
* **Authorization**: Conversation owner only.
* **Request Body**: None
* **Response Body**: None
* **Success Status Codes**: `204 No Content`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Conversation not found or belongs to another user.

### POST /api/ai/conversations/{id}/messages
* **Purpose**: Store a message in a conversation. Does not call any AI model.
* **Authentication**: Required (JWT Token).
* **Authorization**: Conversation owner only.
* **Request Body** (application/json):
  ```json
  {
    "role": "MessageRole (USER, ASSISTANT, SYSTEM, required)",
    "content": "String (required)",
    "metadata": "Map of key-value pairs (optional)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "id": "String",
    "conversationId": "String",
    "role": "MessageRole",
    "content": "String",
    "metadata": {},
    "timestamp": "String (ISO-8601 DateTime)"
  }
  ```
* **Success Status Codes**: `201 Created`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Conversation not found, is inactive, or belongs to another user.

### POST /api/ai/conversations/{id}/chat
* **Purpose**: Send a message to the AI Gateway and retrieve the assistant response. Both the user message and assistant response are persisted to the conversation history.
* **Authentication**: Required (JWT Token).
* **Authorization**: Conversation owner only.
* **Request Body** (application/json):
  ```json
  {
    "message": "String (required, cannot be blank)"
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "assistantMessage": "String",
    "conversationId": "String",
    "model": "String",
    "provider": "String",
    "processingTime": 0.0
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure.
  - `401 Unauthorized`: Unauthenticated.
  - `404 Not Found`: Conversation not found, is inactive, or belongs to another user.
  - `502 Bad Gateway`: Gateway failure (though usually intercepted to return a graceful fallback response).


### GET /api/ai/recommendations
* **Purpose**: Retrieve all recommendation categories (Academic, Events, Communities, Resources) for the authenticated user, sorted by relevance score.
* **Authentication**: Required (JWT Token).
* **Request Parameters**:
  - `page`: Integer (optional, 0-based page index)
  - `size`: Integer (optional, page size)
* **Response Body** (application/json):
  ```json
  [
    {
      "id": "course-123",
      "title": "CS102 - Data Structures",
      "description": "Study of fundamental data structures.",
      "recommendationType": "ACADEMIC",
      "recommendationSource": "ROADMAP",
      "reasonCode": "PREREQUISITE_MATCH",
      "score": 0.90,
      "explanation": "This course is the next prerequisite in your roadmap.",
      "metadata": {
        "courseCode": "CS102",
        "semester": 2,
        "credits": 4
      }
    },
    {
      "id": "event-456",
      "title": "Computer Science Hackathon",
      "description": "CS Department programming challenge.",
      "recommendationType": "EVENT",
      "recommendationSource": "EVENT",
      "reasonCode": "DEPARTMENT_MATCH",
      "score": 0.85,
      "explanation": "This workshop matches your Computer Science department.",
      "metadata": {
        "councilId": "council-111",
        "startTime": "2026-10-15T10:00:00",
        "location": "Lobby A"
      }
    }
  ]
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.

### GET /api/ai/recommendations/{type}
* **Purpose**: Retrieve recommendations of a specific category (academic, events, communities, resources) for the authenticated user, sorted by relevance score.
* **Authentication**: Required (JWT Token).
* **Path Parameters**:
  - `type`: String (required, one of `academic`, `events`, `communities`, `resources`)
* **Request Parameters**:
  - `page`: Integer (optional, 0-based page index)
  - `size`: Integer (optional, page size)
* **Response Body** (application/json):
  Same format as `GET /api/ai/recommendations`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Invalid/Unsupported recommendation type.
  - `401 Unauthorized`: Unauthenticated.

---

## Admin Analytics

### GET /api/admin/analytics/dashboard
* **Purpose**: Retrieve high-level operational metrics of the platform.
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` role only.
* **Response Body** (application/json):
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
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not an administrator.

### GET /api/admin/analytics/users
* **Purpose**: Retrieve user-specific counts (total and active).
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` role only.
* **Response Body** (application/json):
  ```json
  {
    "total": 100,
    "active": 80,
    "archived": 0
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not an administrator.

### GET /api/admin/analytics/events
* **Purpose**: Retrieve event-specific counts (total active and upcoming).
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` role only.
* **Response Body** (application/json):
  ```json
  {
    "total": 30,
    "active": 5,
    "archived": 0
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not an administrator.

### GET /api/admin/analytics/communities
* **Purpose**: Retrieve community-specific counts (total and active).
* **Authentication**: Required (JWT Token).
* **Authorization**: `SUPER_ADMIN` role only.
* **Response Body** (application/json):
  ```json
  {
    "total": 20,
    "active": 18,
    "archived": 0
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: Authenticated user is not an administrator.

### POST /api/search
* **Purpose**: Perform a unified search across multiple modules (Courses, Roadmaps, Communities, Events, Resources) with relevance scoring and in-memory pagination.
* **Authentication**: Required (JWT Token).
* **Authorization**: Any authenticated user.
* **Request Body** (application/json):
  ```json
  {
    "query": "String (required, cannot be blank)",
    "types": [
      "COURSE"
    ]
  }
  ```
* **Response Body** (application/json):
  ```json
  {
    "query": "String",
    "totalResults": 15,
    "results": [
      {
        "id": "String",
        "title": "String",
        "description": "String",
        "searchType": "String (COURSE, ROADMAP, COMMUNITY, EVENT, RESOURCE)",
        "relevanceScore": 0.9,
        "metadata": {
          "courseCode": "CS101",
          "department": "Computer Science"
        }
      }
    ]
  }
  ```
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (blank query).
  - `401 Unauthorized`: Unauthenticated.




