# Routing & Route Protection

## Overview

The routing infrastructure (`src/core/routing/`) integrates React Router v7 with `AuthProvider` to guard application routes based on authentication state and user roles.

## Route Guards

### 1. `ProtectedRoute`

Guards routes requiring an active user session.

#### Features
- Renders `LoadingOverlay` during initial session restoration.
- Redirects unauthenticated users to `/login`, preserving the intended destination in `location.state.from`.
- Optionally restricts access based on user role (`allowedRoles`).

```tsx
import { ProtectedRoute } from '@/core/routing';
import { Dashboard } from '@/app/pages/Dashboard';

const routes = [
  {
    path: '/dashboard',
    element: (
      <ProtectedRoute>
        <Dashboard />
      </ProtectedRoute>
    ),
  },
  {
    path: '/admin',
    element: (
      <ProtectedRoute allowedRoles={['ADMIN']}>
        <AdminPanel />
      </ProtectedRoute>
    ),
  },
];
```

### 2. `PublicRoute`

Guards routes intended for public or unauthenticated visitors (e.g., Login, Register).

#### Features
- If `restricted={true}` and the user is already authenticated, automatically redirects them back to their previous intended location (`location.state.from`) or home `/`.

```tsx
import { PublicRoute } from '@/core/routing';
import { LoginPage } from '@/app/pages/LoginPage';

const routes = [
  {
    path: '/login',
    element: (
      <PublicRoute restricted>
        <LoginPage />
      </PublicRoute>
    ),
  },
];
```

## Redirect-After-Login Pattern

When an unauthenticated user attempts to access `/profile`:

1. `ProtectedRoute` intercepts the request.
2. Navigates to `/login` with `state: { from: location }`.
3. Upon successful login, the login handler checks `location.state?.from` and redirects back to `/profile`.
