# Frontend Resilience & Recovery Architecture

This document describes the production reliability, error handling, offline support, and recovery mechanisms implemented across the **CampusGuide** frontend.

---

## 1. Global Error Handling & Localized Boundaries

To prevent single-component failures from crashing the entire application, CampusGuide utilizes a hierarchical Error Boundary architecture:

- **Root Error Boundary**: Wraps the entire application in [AppProviders.tsx](file:///D:/CampusGuide/frontend/src/core/providers/AppProviders.tsx) to catch unhandled layout or routing exceptions.
- **Localized Error Boundaries**: Standard feature-level boundaries isolate component failures:
  - `WidgetErrorBoundary` (Dashboard Widgets)
  - `AcademicSectionErrorBoundary` (Academic Summary, Course Catalog, degree progress)
  - `AtlasErrorBoundary` (Atlas panels, timeline, message composer)
  - `CalendarErrorBoundary` (Full Calendar views)
  - `NoticeErrorBoundary` (Notice board panels)
  - `NotificationErrorBoundary` (Real-time notifications list)
  - `ResourceErrorBoundary` (Resource search and center components)

All boundaries utilize a unified recovery loop. Clicking **Try Again** or **Retry Section** resets the local boundary state and triggers a layout re-render.

---

## 2. API Error Sanitization

All network failures are intercepted and normalized inside [ApiClient.ts](file:///D:/CampusGuide/frontend/src/core/api/ApiClient.ts) and [ErrorHandler.ts](file:///D:/CampusGuide/frontend/src/core/errors/ErrorHandler.ts):

- **Friendly Messages**: Tech-heavy error details (e.g. database errors, JavaScript `TypeError`s, `Cannot read properties of undefined`) are masked from end-users. Masked errors return a standard, readable feedback message: *"An unexpected error occurred. Please try again."*
- **Hidden Technical Details & No Stack Traces**: Raw developer-focused stack traces are printed exclusively to the developer `logger` console and never exposed in the UI text elements.
- **Unified Error Model**: Network errors are mapped to specialized error classes: `NetworkError`, `TimeoutError`, `ApiError`, or `AuthError` (inheriting from `AppError`).

---

## 3. React Query Retry Policies & De-duplication

TanStack React Query handles server state and transient network recovery:

- **Centralized Retry Policy**: Default query retry behavior is defined in [queryClient.ts](file:///D:/CampusGuide/frontend/src/core/query/queryClient.ts) via `queryRetryPolicy`.
  - Transient errors (HTTP 502/503/504, `NetworkError`, `TimeoutError`) are retried up to **3 times** with exponential backoff.
  - Client errors (HTTP 4xx: Bad Request, Unauthorized, Forbidden, Not Found) are **not retried**.
- **De-duplicated Retry Logic**: Automatic GET request retries inside `ApiClient` are set to `0` (`DEFAULT_MAX_RETRIES = 0`). This ensures that only React Query manages retries, avoiding nested, redundant network calls (`3 * 2 = 6` nested requests).
- **Mutations Policy**: Mutations (`POST`, `PUT`, `DELETE`) default to `retry: false` to avoid unintended duplicate writes, unless explicit safe idempotent retries are defined.

---

## 4. Offline Detection & Banner Recovery

CampusGuide implements browser online/offline status listeners with strict mutations blocking:

- **State Hook**: The `useOnlineStatus` hook monitors the `window` online/offline events.
- **Offline Banner**: A bottom-floating glassmorphic notification panel ([OfflineBanner.tsx](file:///D:/CampusGuide/frontend/src/app/components/OfflineBanner.tsx)) slides in when connection is lost. Upon reconnection, it briefly switches to a green success status before auto-dismissing.
- **Mutations Blocking**: If the browser is offline, [ApiClient.ts](file:///D:/CampusGuide/frontend/src/core/api/ApiClient.ts) blocks all non-GET requests immediately and throws a descriptive `NetworkError`.
- **Query Auto-Pause**: TanStack React Query automatically pauses active queries when the network goes offline, and resumes them seamlessly on reconnect (`refetchOnReconnect: true`).

---

## 5. Optimistic Mutations & Correct Rollbacks

To deliver a responsive user experience, mutations use the optimistic mutation wrapper [useOptimisticMutation.ts](file:///D:/CampusGuide/frontend/src/hooks/common/useOptimisticMutation.ts):

- **State Snapshotting**: Before executing a mutation, `onMutate` cancels ongoing query refetches targeting the key, takes a snapshot of the current query cache (`previousData`), and applies the new state optimistically.
- **Reliable Cache Rollback**: If the mutation fails, the cache is rolled back to the captured `previousData`. The rollback loop is built to correctly handle `undefined` or empty initial states (restoring them to an empty state rather than leaking the failed optimistic state).
- **Cache Invalidation**: Upon success or failure (`onSettled`), queries are invalidated to force a fresh refetch from the database.

---

## 6. Authentication Recovery Flow

Authentication tokens are verified and refreshed silently to avoid premature logging out:

- **Preemptive Token Refresh**: The request interceptor in [AuthProvider.tsx](file:///D:/CampusGuide/frontend/src/core/auth/AuthProvider.tsx) checks if the access token is expired before dispatching the request. If expired, it triggers a preemptive token refresh.
- **401 Interceptor Recovery**: If a request receives a `401 Unauthorized` response due to an expired session, the error interceptor halts execution, invokes `tokenManager.refreshTokens()`, updates the request headers with the new access token, and retries the request transparently.
- **Token Refresh Failure**: If the refresh token is missing or has expired, a forced logout is triggered. This clears all active tokens, resets cached queries (`queryClient.clear()`), and redirects the user to `/login` while preserving the redirection source route via `state: { from: location }`.

---

## 7. Atlas Streaming Recovery

The Atlas AI search and navigation stream operates on manual SSE recovery:

- **State Tracking**: `useAtlasStreamChat` captures progress events following the workflow:
  `Conversation ➔ Streaming Response ➔ Thinking Timeline ➔ Tool Execution ➔ Campus Result`
- **Offline Check**: `startStream` validates network connectivity before opening the SSE stream. If the browser is offline, it blocks the connection, sets an offline error message, and enables the red banner's **Retry** button.
- **Cancelled Streams & Abort Signals**: Streaming sessions can be aborted using an `AbortController`. Triggering `cancelStream` aborts the request signal, transitions the UI status cleanly, and terminates connection resources.
- **Reconnect Backoff**: The SDK `StreamingClient.js` automatically retries interrupted connections up to **3 times** with exponential backoff before throwing a terminal stream error.
