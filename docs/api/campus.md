# Campus Domain API Specifications

## Councils API (`/api/v1/councils`)

### 1. Create Council
- **Endpoint**: `POST /api/v1/councils`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Request Body**: `CreateCouncilRequest`
- **Response**: `201 Created` (`CouncilResponse`)
- **Errors**: `400 Bad Request` (Validation errors), `401 Unauthorized`, `403 Forbidden`, `409 Conflict` (Duplicate name/slug)

### 2. List All Councils
- **Endpoint**: `GET /api/v1/councils`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: `200 OK` (`List<CouncilResponse>`)
- **Errors**: `401 Unauthorized`

### 3. Get Council by ID
- **Endpoint**: `GET /api/v1/councils/{id}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Path Parameter**: `id` (UUID)
- **Response**: `200 OK` (`CouncilResponse`)
- **Errors**: `401 Unauthorized`, `404 Not Found`

### 4. Get Council by Slug
- **Endpoint**: `GET /api/v1/councils/slug/{slug}`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Path Parameter**: `slug` (String, e.g., `technical-council`)
- **Response**: `200 OK` (`CouncilResponse`)
- **Errors**: `401 Unauthorized`, `404 Not Found`

### 5. Update Council
- **Endpoint**: `PUT /api/v1/councils/{id}`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Path Parameter**: `id` (UUID)
- **Request Body**: `UpdateCouncilRequest`
- **Response**: `200 OK` (`CouncilResponse`)
- **Errors**: `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`

### 6. Update Council Status
- **Endpoint**: `PATCH /api/v1/councils/{id}/status`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Path Parameter**: `id` (UUID)
- **Request Body**: `UpdateCouncilStatusRequest` (`{"isActive": boolean}`)
- **Response**: `200 OK` (`CouncilResponse`)
- **Errors**: `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`

### 7. Delete Council
- **Endpoint**: `DELETE /api/v1/councils/{id}`
- **Security**: `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- **Path Parameter**: `id` (UUID)
- **Response**: `204 No Content`
- **Errors**: `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict` (Dependent entities exist)
