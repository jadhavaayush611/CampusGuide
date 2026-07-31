# Networking Infrastructure

## Overview

The networking layer (`src/core/api/`) provides a centralized, type-safe HTTP client abstraction (`ApiClient`) built on top of native `fetch`. Direct `fetch` or Axios calls in feature modules are prohibited.

## Core Classes & Constants

- **`ApiClient`**: Central HTTP service managing request execution, interceptor pipelines, timeout abortion, retry logic, and error normalization.
- **`ApiConfig`**: Defines request configuration interfaces (`ApiRequestConfig`, `ApiResponse`, `ApiClientConfig`).
- **`ApiConstants`**: Standardized HTTP status codes, headers, and default timeouts.

## Making API Requests

Use the global `apiClient` instance exported from `@/core/api`:

```ts
import { apiClient } from '@/core/api';

interface CampusNotice {
  id: string;
  title: string;
  content: string;
}

// GET Request
const notices = await apiClient.get<CampusNotice[]>('/notices');

// POST Request
const newNotice = await apiClient.post<CampusNotice>('/notices', {
  title: 'Orientation Week',
  content: 'Details for new students...',
});
```

## Interceptor Pipeline

Requests pass through registered request and response interceptors before returning data or throwing normalized errors.

```
Client Call -> Request Interceptors -> Fetch -> Response Interceptors -> Return Data
                     │                                │
        Attach Correlation ID & Bearer      Catch Errors / Retries
```

### 1. Request Interceptors

- **JWT Attachment**: Automatically appends `Authorization: Bearer <token>` unless `skipAuth: true` is configured.
- **Correlation ID**: Generates a UUID v4 via `generateCorrelationId()` and sets `X-Correlation-ID` for distributed tracing.
- **Default Headers**: Sets `Content-Type: application/json` and `Accept: application/json`.

### 2. Response & Error Interceptors

- **Error Normalization**: All non-2xx responses are mapped into structured `ApiError` objects containing `statusCode`, `correlationId`, and `responseData`.
- **Transient Failures & Automatic Retry**: Idempotent `GET` requests encountering 502/503/504 status codes or network disconnections are automatically retried up to 2 times with exponential backoff.
- **Session Expiry**: 401 Unauthorized responses trigger automatic session cleanup.

## Timeout Configuration

Requests default to a 15-second timeout, controlled via `AbortController`. Custom timeouts can be specified per request:

```ts
const data = await apiClient.get('/analytics', { timeoutMs: 30000 });
```
