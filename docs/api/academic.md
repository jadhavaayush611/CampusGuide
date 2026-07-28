# Academic Domain API Specifications

## Courses API (`/api/v1/courses`)

### 1. Create Course
- **Endpoint**: `POST /api/v1/courses`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Request Body**: `CreateCourseRequest`
- **Response**: `201 Created` (`CourseResponse`)
- **Errors**: `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `409 Conflict` (Duplicate course code)

### 2. Get All Active Courses
- **Endpoint**: `GET /api/v1/courses`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`List<CourseSummaryResponse>`)

### 3. Get Course by ID
- **Endpoint**: `GET /api/v1/courses/{id}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`CourseResponse`)
- **Errors**: `404 Not Found`

### 4. Get Course by Code
- **Endpoint**: `GET /api/v1/courses/code/{code}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`CourseResponse`)

### 5. Update Course
- **Endpoint**: `PUT /api/v1/courses/{id}`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Response**: `200 OK` (`CourseResponse`)

### 6. Delete Course (Soft Delete)
- **Endpoint**: `DELETE /api/v1/courses/{id}`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Response**: `204 No Content`

---

## Academic Roadmaps API (`/api/v1/roadmaps`)

### 1. Create Roadmap
- **Endpoint**: `POST /api/v1/roadmaps`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Request Body**: `CreateRoadmapRequest`
- **Response**: `201 Created` (`RoadmapResponse`)

### 2. List Active Roadmaps
- **Endpoint**: `GET /api/v1/roadmaps`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`List<RoadmapSummaryResponse>`)

### 3. Get Roadmap by ID
- **Endpoint**: `GET /api/v1/roadmaps/{id}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`RoadmapResponse`)

### 4. Update Roadmap
- **Endpoint**: `PUT /api/v1/roadmaps/{id}`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Response**: `200 OK` (`RoadmapResponse`)

### 5. Delete Roadmap
- **Endpoint**: `DELETE /api/v1/roadmaps/{id}`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Response**: `204 No Content`

---

## Semester Planner API (`/api/v1/semester-plans`)

### 1. Create Semester Plan
- **Endpoint**: `POST /api/v1/semester-plans`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `201 Created` (`SemesterPlanResponse`)

### 2. List Current User Semester Plans
- **Endpoint**: `GET /api/v1/semester-plans`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`List<SemesterPlanResponse>`)

### 3. Finalize Semester Plan
- **Endpoint**: `PATCH /api/v1/semester-plans/{id}/finalize`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`SemesterPlanResponse`)

---

## Student Progress API (`/api/v1/progress`)

### 1. Get Current Student Progress
- **Endpoint**: `GET /api/v1/progress`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`StudentProgressResponse`)

### 2. Mark Course Completed
- **Endpoint**: `POST /api/v1/progress/completed-courses/{courseId}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`StudentProgressResponse`)

### 3. Remove Completed Course
- **Endpoint**: `DELETE /api/v1/progress/completed-courses/{courseId}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`StudentProgressResponse`)
