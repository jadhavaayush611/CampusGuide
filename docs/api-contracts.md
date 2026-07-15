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
* **Purpose**: Update student progress metrics. Students can update their own progress. `SUPER_ADMIN` can specify `studentId` to update other students.
* **Authentication**: Required (JWT Token).
* **Authorization**: Owner or `SUPER_ADMIN`.
* **Request Body** (application/json):
  ```json
  {
    "studentId": "String (optional, for SUPER_ADMIN use only)",
    "roadmapId": "String (optional)",
    "currentSemester": "Integer (minimum 1, optional)",
    "currentGpa": "Double (0.0 to 10.0, optional)",
    "totalCreditsEarned": "Integer (minimum 0, optional)",
    "graduationEligible": "Boolean (optional)"
  }
  ```
* **Response Body** (application/json):
  Same format as `POST /api/progress`.
* **Success Status Codes**: `200 OK`
* **Error Status Codes**:
  - `400 Bad Request`: Validation failure (GPA out of range, semester < 1, etc.).
  - `401 Unauthorized`: Unauthenticated.
  - `403 Forbidden`: User is not owner and not `SUPER_ADMIN`.
  - `404 Not Found`: Progress record not found.

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
