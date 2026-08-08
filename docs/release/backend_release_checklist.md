# Release Candidate (RC1) Checklist

This document details the checklist required to stabilize the CampusGuide Release Candidate 1 (RC1) for the `v1.0.0-MVP` milestone.

---

## 1. Build & Compilation Verification
- [x] **Backend Compilation**: Clean compile via `./mvnw.cmd clean test-compile` passes with zero errors.
- [x] **Backend Verification Suite**: Complete suite run via `./mvnw.cmd clean verify` executes 1032 tests (732 unit tests and 300 integration tests) with zero failures.
- [x] **Frontend Type-Checking**: Static analysis check via `npm run typecheck` completes with zero diagnostics errors.
- [x] **Frontend Production Bundle**: Build command `npm run build` succeeds, generating Vite chunks inside `dist/`.

## 2. Authentication & Authorization Controls
- [x] **Public & Protected Route Rules**:
  - Public routes (`/login`, `/register`, `/unauthorized`) accessible when unauthenticated.
  - Protected routes (`/dashboard`, `/academic`, `/planner`, etc.) redirect to `/login` when unauthenticated.
  - Role-based routing enforces proper access bounds.
- [x] **JWT Lifecycle & Refresh**:
  - Silent token refresh behaves correctly without breaking user UI.
  - Sessions restore correctly from local storage upon page refresh.
  - Expired tokens redirect gracefully to `/login`.
- [x] **Method-Level Security Rules**:
  - `@PreAuthorize` guards correctly configured on REST endpoints.
  - Endpoint integration tests (`*IT.java`) verify security blocks (401/403).

## 3. Core Domain Orchestrator Validations
- [x] **Dashboard Domain**:
  - Dashboard component renders correctly as an orchestrator without duplicate business logic.
  - Counters and skeletons render smoothly during async loads.
- [x] **Academic Domain**:
  - Course catalog and prerequisite trees load correctly.
  - Degree progress and credit completion metrics compute on the server side.
- [x] **Campus Domain**:
  - Communities forum behaves independently (posts, comments, discovery, tags).
  - Councils directory operates independently from communities (notices, events, resources).
  - Resource sharing uploads, previews, downloads, and bookmarks pass safety and validation checks.
- [x] **Personal Domain**:
  - Planner tasks and study goals sync correctly.
  - Calendar agenda, day, week, and month views update, integrated with Planner tasks.
  - Atlas AI Student Advisor streams prompts and replies step-by-step with a visible thinking timeline.
  - Notification drawer renders read/unread alerts and supports deep linking.

## 4. Resilience & Offline Readiness
- [x] **Caching Strategies**: TanStack Query cache configurations prevent duplicate network fetches.
- [x] **Offline Capabilities**: UI falls back gracefully to cached datasets and indicates offline status instead of crashing.
- [x] **Error Boundaries**: Hardened error screens catch runtime rendering issues without visual leakage.

## 5. UI/UX & Accessibility Audit
- [x] **Color Contrast & Dark/Light Mode**: Smooth switches, consistent visual palettes, and correct text-to-background contrast ratio.
- [x] **Keyboard Navigation**: Active elements support correct tab indices and enter key triggers.
- [x] **Screen Reader Support**: ARIA labeling checks applied on critical cards, buttons, and form inputs.

## 6. Repository Hygiene
- [x] **Dead Code Cleanup**: No unused imports, development debug code, or dangling comments.
- [x] **No Hardcoded Secrets**: Secrets and JWT properties are managed strictly via environment variables or Spring application properties.
- [x] **Version Standardization**: Pom.xml and package.json versions aligned to `1.0.0-MVP`.
