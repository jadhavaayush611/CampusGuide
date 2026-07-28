# Authentication API Specifications

## Base URL
`/api/v1/auth`

---

## 1. Register User
- **Endpoint**: `POST /api/v1/auth/register`
- **Security**: Public (`PermitAll`)
- **Request Body**: `RegisterRequest`
  ```json
  {
    "email": "user@campusguide.com",
    "password": "Password123!",
    "username": "johndoe",
    "role": "STUDENT"
  }
  ```
- **Response**: `201 Created` (`AuthenticationResponse`)
  ```json
  {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": "user-uuid",
      "email": "user@campusguide.com",
      "username": "johndoe",
      "role": "STUDENT",
      "createdAt": "2026-07-28T22:00:00.000Z",
      "updatedAt": "2026-07-28T22:00:00.000Z"
    }
  }
  ```
- **Errors**:
  - `400 Bad Request`: Validation failure (weak password, invalid email format)
  - `409 Conflict`: Email or username already exists

---

## 2. Authenticate / Login User
- **Endpoint**: `POST /api/v1/auth/login`
- **Security**: Public (`PermitAll`)
- **Request Body**: `AuthenticationRequest`
  ```json
  {
    "email": "user@campusguide.com",
    "password": "Password123!"
  }
  ```
- **Response**: `200 OK` (`AuthenticationResponse`)
- **Errors**:
  - `400 Bad Request`: Missing mandatory fields
  - `401 Unauthorized`: Invalid credentials

---

## 3. Refresh Access Token
- **Endpoint**: `POST /api/v1/auth/refresh`
- **Security**: Public / Refresh Token Validation (`PermitAll`)
- **Request Body**: `RefreshTokenRequest`
  ```json
  {
    "refreshToken": "eyJhbGci..."
  }
  ```
- **Response**: `200 OK` (`AuthenticationResponse`)
- **Errors**:
  - `401 Unauthorized`: Expired or invalid refresh token

---

## 4. Get Current User Details
- **Endpoint**: `GET /api/v1/auth/me`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Headers**: `Authorization: Bearer <accessToken>`
- **Response**: `200 OK` (`UserResponse`)
- **Errors**:
  - `401 Unauthorized`: Missing or invalid Bearer token
