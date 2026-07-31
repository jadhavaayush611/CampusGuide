# Authentication Infrastructure & Integration

## Overview

The authentication layer (`src/core/auth/`, `src/sdk/auth/`, `src/hooks/auth/`) centralizes state management, user session restoration, token storage, silent refresh, and HTTP interceptor integration for CampusGuide.

---

## Key Components

### 1. `TokenStorage` & `TokenManager`

- **`TokenStorage`**: Abstracts persistence of JWT access tokens, refresh tokens, and expiration timestamps using `IStorage` (defaulting to `LocalStorageAdapter`).
- **`TokenManager`**: Manages token updates, computes expiration times, deduplicates concurrent token refresh requests (`refreshTokens()`), and exposes token change listeners.

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

---

## Workflows & Integration Flows

### 1. Login Flow

1. User submits credentials via the `Login` form component (`src/app/pages/Login.tsx`).
2. Client-side field validation verifies required fields and email/username format.
3. Form triggers `useLogin()` mutation hook, which delegates exclusively to `authSdk.login(credentials)`.
4. On backend success:
   - `useLogin` invokes `login(tokens, user)` on `AuthContext`.
   - `TokenManager` stores access and refresh tokens in persistent storage.
   - React Query invalidates cached auth queries.
   - User receives a success toast notification and is redirected to their origin route (`location.state.from`) or `/`.
5. On backend error (e.g., 401 Invalid Credentials):
   - Form displays an accessible error alert with the specific backend failure reason.

### 2. Registration Flow

1. User fills out registration details in `Register` page (`src/app/pages/Register.tsx`).
2. Client-side validation checks name, valid email format, minimum password length (>= 8 chars), matching confirm password, and role selection.
3. Form invokes `useRegister()` mutation hook, calling `authSdk.register(payload)`.
4. On backend success:
   - User is automatically logged in via `AuthContext.login()`.
   - Tokens and session state are persisted.
   - Toast notification confirms account creation and user is navigated into the application.
5. On backend error (e.g., duplicate email/username):
   - Form displays inline error alerts.

### 3. Session Restoration Sequence

On application startup, `AuthProvider`:

1. Invokes `restoreSession()`.
2. Reads stored access token from `TokenManager`.
3. If access token is valid:
   - Attempts to fetch current user profile via `authSdk.getCurrentUser()`.
   - On success, populates `AuthState.user` and sets `isAuthenticated: true`.
   - On network error, falls back to parsing claims from JWT payload.
4. If access token is expired/absent but a refresh token exists:
   - Executes silent refresh via `tokenManager.refreshTokens()`.
   - Fetches current user profile and restores authenticated session state.
5. If no tokens exist or refresh fails:
   - Clears stored tokens and sets `isAuthenticated: false`.
6. Sets `isLoading: false` to allow router navigation.

### 4. Token Refresh & 401 Interceptor Integration

`AuthProvider` configures HTTP interceptors on `apiClient`:

- **Request Interceptor**: Automatically attaches `Authorization: Bearer <accessToken>` header to all outgoing requests unless `skipAuth: true` is specified.
- **Silent Refresh & 401 Handling**:
  - When an API call returns `401 Unauthorized`:
  - `TokenManager` triggers single-flight silent token refresh using `authSdk.refreshToken(refreshToken)`.
  - If refresh succeeds: updated tokens are stored, and the original failed request is retried.
  - If refresh fails (e.g. invalid/revoked refresh token): `logout()` is invoked for forced cleanup and redirect to `/login`.

### 5. Logout Flow

1. User triggers sign-out via Header user menu or Sidebar button.
2. Triggers `useLogout()` mutation hook, calling `authSdk.logout()`.
3. In `onSuccess` and `onError`:
   - `AuthContext.logout()` clears stored tokens from `TokenStorage`.
   - React Query cache is wiped via `queryClient.clear()`.
   - Toast notification confirms sign-out.
   - User is redirected to `/login`.

---

## Protected & Public Routes

Routes in `src/app/routes.tsx` enforce strict route protection:

- **`PublicRoute`**: Restricted routes (e.g. `/login`, `/register`) accessible only to unauthenticated users. Logged-in users are redirected to their destination or `/`.
- **`ProtectedRoute`**: Authenticated routes (e.g. `/`, `/councils`, `/resources`, `/profile`). Unauthenticated users are redirected to `/login` with `state: { from: location }` preserved.
