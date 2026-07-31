# Frontend Application Architecture

## Overview

CampusGuide's frontend infrastructure is structured around a centralized core layer located at `src/core/`. This architecture provides reusable, production-grade application services for networking, authentication, state persistence, routing protection, loading states, notification toasts, error handling, and environment configuration.

## Core Layer Architecture (`src/core/`)

```
src/core/
├── api/          # Centralized HTTP client, request/response interceptors, retry & error normalization
├── auth/         # Authentication state context, JWT session restoration, token manager & storage
├── config/       # Typed environment configuration abstraction (only layer accessing import.meta.env)
├── errors/       # AppError hierarchy, ErrorHandler service, and ErrorBoundary React component
├── loading/      # Global, page, and component loading context, spinner, and translucent overlay
├── routing/      # ProtectedRoute & PublicRoute route guards with redirect-after-login state
├── storage/      # IStorage abstraction, LocalStorage, SessionStorage, MemoryStorage & StorageManager
├── toast/        # Reusable useToast() wrapper around notification library (Sonner)
├── utils/        # Logger, JWT decoder, UUID generator utilities
└── providers/    # AppProviders root hierarchy composing all core providers
```

## Architectural Principles

1. **Dependency Inversion**: Feature modules depend on abstractions (interfaces like `IStorage`, `AppConfig`, `ApiClient`) rather than direct environment variables or browser APIs.
2. **Centralized Environment Isolation**: Only `src/core/config/env.ts` accesses `import.meta.env`. All other modules import `config` or `getConfig()` from `@/core/config`.
3. **Strict Type Safety**: All interfaces, options, HTTP request parameters, and tokens are fully typed.
4. **Single Source of Truth**: Authentication and network states are managed globally through context providers, eliminating fragmented token handling.

## Root Provider Hierarchy

The application root (`App.tsx`) wraps the router inside `AppProviders`, which composes core services in strict order:

```tsx
<ErrorBoundary>        {/* 1. Catches unhandled React rendering errors */}
  <ToastProvider>      {/* 2. Mounts global notification portal */}
    <LoadingProvider>  {/* 3. Manages global/keyed loading spinners & overlays */}
      <AuthProvider>   {/* 4. Restores token sessions and configures HTTP interceptors */}
        <RouterProvider router={router} />
      </AuthProvider>
    </LoadingProvider>
  </ToastProvider>
</ErrorBoundary>
```

## Usage Guidelines for Feature Modules

- **DO NOT** create custom `fetch()` calls or Axios instances. Always use `apiClient` from `@/core/api`.
- **DO NOT** access `localStorage` directly. Use `appStorage` or `TokenStorage` from `@/core/storage`.
- **DO NOT** read `import.meta.env`. Import `config` from `@/core/config`.
- **DO NOT** import directly from external toast libraries. Use `useToast()` from `@/core/toast`.
