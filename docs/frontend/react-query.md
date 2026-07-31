# React Query & Server State Management

## Overview

TanStack React Query serves as the application's server-state management engine. It handles caching, deduplication, background synchronization, stale-time management, garbage collection, and optimistic mutation rollbacks across all feature modules.

---

## Global QueryClient Configuration

The `QueryClient` singleton is configured in `src/core/query/queryClient.ts`:

```typescript
export const queryClientConfig: QueryClientConfig = {
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,   // Data remains fresh for 5 minutes
      gcTime: 10 * 60 * 1000,      // Inactive cache garbage collected after 10 minutes
      retry: queryRetryPolicy,     // Smart retry strategy
      refetchOnWindowFocus: false, // Prevents unintended refetches when switching desktop tabs
      refetchOnReconnect: true,   // Automatic refetching on network reconnect
      refetchOnMount: true,        // Standard fresh fetch on mount if stale
    },
    mutations: {
      retry: false,
      onError: (error) => {
        logger.error('[QueryClient Mutation Error]:', error);
      },
    },
  },
};
```

---

## Smart Retry Strategy (`queryRetryPolicy`)

Queries automatically attempt retries based on the HTTP status code returned by the SDK:

1. **Client Errors (HTTP 400 - 499)**: Never retried (e.g. 401 Unauthorized, 403 Forbidden, 404 Not Found, 400 Bad Request).
2. **Server/Network Errors (HTTP 500+ or Network Timeout)**: Retried up to 3 times with exponential backoff.

---

## Provider Integration Order

`QueryClientProvider` is integrated directly into the application provider stack (`src/core/providers/AppProviders.tsx`):

```
ErrorBoundary
↓
ToastProvider
↓
LoadingProvider
↓
QueryClientProvider
↓
AuthProvider
↓
Application
```

This placement guarantees that:
- Error boundaries capture rendering failures from query hooks.
- Notification toasts are accessible for mutation notifications.
- Loading indicators can sync with global network states.
- Auth context can trigger query cache clearances upon session logout.

---

## Centralized Query Keys (`src/sdk/queryKeys.ts`)

Query keys are organized into a strict domain hierarchy:

```typescript
export const queryKeys = {
  auth: {
    all: ['auth'] as const,
    user: () => [...queryKeys.auth.all, 'user'] as const,
  },
  campus: {
    all: ['campus'] as const,
    buildings: () => [...queryKeys.campus.all, 'buildings'] as const,
    building: (id: string) => [...queryKeys.campus.buildings(), id] as const,
    events: () => [...queryKeys.campus.all, 'events'] as const,
    event: (id: string) => [...queryKeys.campus.events(), id] as const,
  },
  planner: {
    all: ['planner'] as const,
    schedules: () => [...queryKeys.planner.all, 'schedules'] as const,
    schedule: (id: string) => [...queryKeys.planner.schedules(), id] as const,
  },
  atlas: {
    all: ['atlas'] as const,
    search: (query: string, category?: string) => [...queryKeys.atlas.all, 'search', { query, category }] as const,
    route: (originLat: number, originLng: number, destLat: number, destLng: number) =>
      [...queryKeys.atlas.all, 'route', { originLat, originLng, destLat, destLng }] as const,
  },
};
```

---

## Query Lifecycle & Cache Strategy

1. **Initial Mount**: Component calls hook -> React Query checks cache.
2. **Cache Hit (Fresh)**: Returns cached data immediately without network request.
3. **Cache Hit (Stale)**: Returns cached data immediately, then triggers background fetch to update cache.
4. **Cache Miss**: Displays loading state -> Fetches via SDK -> Updates cache -> Renders data.
5. **Unmount & GC**: When no components subscribe to a query key, garbage collection timer (`gcTime`: 10 mins) starts.
