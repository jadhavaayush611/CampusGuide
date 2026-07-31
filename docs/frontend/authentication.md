# Authentication Infrastructure

## Overview

The authentication layer (`src/core/auth/`) centralizes state management, user session restoration, token storage, and HTTP interceptor integration.

## Key Components

### 1. `TokenStorage` & `TokenManager`

- **`TokenStorage`**: Abstracts persistence of JWT access tokens, refresh tokens, and expiration timestamps using `IStorage` (defaulting to `LocalStorageAdapter`).
- **`TokenManager`**: Manages token updates, computes expiration times using lightweight JWT parsing, and exposes token change listeners.

```ts
import { tokenManager } from '@/core/auth';

// Save tokens
tokenManager.setTokens({
  accessToken: 'eyJhbGci...',
  refreshToken: 'def456...',
});

// Check status
if (tokenManager.hasValidAccessToken()) {
  const token = tokenManager.getAccessToken();
}
```

### 2. `AuthContext` & `useAuth()`

The `AuthProvider` maintains global `AuthState`:

```ts
export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  error: string | null;
}
```

#### Hook Usage

```tsx
import { useAuth } from '@/core/auth';

function UserProfile() {
  const { user, isAuthenticated, logout } = useAuth();

  if (!isAuthenticated) return <div>Please log in</div>;

  return (
    <div>
      <p>Welcome, {user?.name}</p>
      <button onClick={logout}>Log Out</button>
    </div>
  );
}
```

## Session Restoration Sequence

On application startup, `AuthProvider`:

1. Invokes `restoreSession()`.
2. Reads access token from `TokenManager`.
3. Verifies token presence and expiration timestamp (`isAccessTokenExpired()`).
4. Decodes JWT payload to populate user session data (`id`, `email`, `name`, `role`).
5. Sets `isAuthenticated: true` and `isLoading: false`.
6. If the token is expired or invalid, clears tokens and initializes as unauthenticated.

## Automatic 401 Interceptor Integration

`AuthProvider` automatically attaches two interceptors to `apiClient`:

- **Request Interceptor**: Adds `Authorization: Bearer <accessToken>` header to all outgoing requests unless `skipAuth: true` is passed.
- **Error Interceptor**: Catches `401 Unauthorized` API responses and triggers `logout()`, clearing expired tokens and resetting session state.
