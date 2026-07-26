# [Module Name] API Specification

## Overview
High-level description of the module and its domain capabilities.

---

## Base Path
`/api/[module-name]`

---

## Endpoints

### 1. [Get Resource List]
- **HTTP Method**: `GET`
- **Path**: `/api/[module-name]`
- **Authentication**: Required (`JWT`)
- **Roles Allowed**: `STUDENT`, `FACULTY`, `COUNCIL_ADMIN`, `SUPER_ADMIN`
- **Request Parameters**:
  - `page` (optional, default: 0)
  - `size` (optional, default: 20)
- **Response Headers**: `Content-Type: application/json`
- **Success Response** (`200 OK`):
  ```json
  [
    {
      "id": "60d5ec49f1b2c81234567890",
      "title": "Example Resource",
      "createdAt": "2026-07-26T08:00:00Z"
    }
  ]
  ```

---

### 2. [Create Resource]
- **HTTP Method**: `POST`
- **Path**: `/api/[module-name]`
- **Authentication**: Required (`JWT`)
- **Roles Allowed**: `COUNCIL_ADMIN`, `SUPER_ADMIN`
- **Request Body**:
  ```json
  {
    "title": "New Resource",
    "description": "Resource description..."
  }
  ```
- **Success Response** (`201 Created`):
  ```json
  {
    "id": "60d5ec49f1b2c81234567891",
    "title": "New Resource",
    "createdAt": "2026-07-26T08:05:00Z"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Validation failure.
  - `403 Forbidden`: Insufficient user role permissions.
