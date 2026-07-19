# Authentication & Users Module

The **Authentication & Users** module provides registration, authentication, token issuance, and profile fetching mechanisms for all CampusGuide users.

---

## 1. Domain Overview
The authentication and user model supports secure password hashing, JSON Web Tokens (JWT) for stateless session handling, and role assignment.

### Role Architecture
The platform defines four major roles for authorization controls:
* **STUDENT**: Standard student user. Assigned automatically on registration (`Role.STUDENT`).
* **FACULTY**: Academic authority (future role placeholder).
* **COUNCIL_ADMIN**: Administrator managing student councils and community moderation.
* **SUPER_ADMIN**: Global system administrator with complete platform overrides.

---

## 2. Core Security & Lifecycle
* **Password Hashing**: Enforced using the standard `BCryptPasswordEncoder` before database persistence.
* **Stateless Authentication**: Upon successful registration or login, the server issues a signed JWT containing user identity and roles. Clients must attach this token as a Bearer token in the `Authorization` header for all secured endpoints.
* **User Profile**: Profiles capture core demographic details including department, enrollment year, verification state, and premium subscription indicator.

---

## 3. Implemented REST Endpoints

### POST `/api/auth/register`
* **Purpose**: Registers a new user account with default role `STUDENT`.
* **Request DTO (`RegisterRequest`)**:
  ```json
  {
    "email": "student@university.edu (required, unique, validated email format)",
    "password": "String (required)",
    "firstName": "String (required)",
    "lastName": "String (required)",
    "department": "String (required)",
    "year": "Integer (required)"
  }
  ```
* **Response DTO (`AuthResponse`)**:
  ```json
  {
    "token": "JWT String",
    "email": "String",
    "role": "STUDENT"
  }
  ```
* **Response Statuses**: `201 Created`, `400 Bad Request` (validation failed), `409 Conflict` (email already exists).

### POST `/api/auth/login`
* **Purpose**: Authenticates credentials and issues a JWT token.
* **Request DTO (`LoginRequest`)**:
  ```json
  {
    "email": "String (required, validated email format)",
    "password": "String (required)"
  }
  ```
* **Response DTO (`AuthResponse`)**: Same as register response.
* **Response Statuses**: `200 OK`, `401 Unauthorized` (invalid credentials), `404 Not Found` (email does not exist).

### GET `/api/auth/me`
* **Purpose**: Returns the authenticated user's profile details.
* **Authentication**: Required (JWT).
* **Response DTO (`UserResponse`)**:
  ```json
  {
    "id": "String",
    "email": "String",
    "firstName": "String",
    "lastName": "String",
    "role": "Role (STUDENT, FACULTY, COUNCIL_ADMIN, SUPER_ADMIN)",
    "department": "String",
    "year": 2026,
    "profilePictureUrl": "String or null",
    "phoneNumber": "String or null",
    "bio": "String or null",
    "isPremium": false,
    "isVerified": false
  }
  ```
* **Response Statuses**: `200 OK`, `401 Unauthorized` (missing/invalid token).
