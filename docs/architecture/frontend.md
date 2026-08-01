# CampusGuide Frontend Architectural Boundaries & Ownership Guide

This document defines the strict domain ownership boundaries, responsibilities, and cross-module communication rules across the CampusGuide React frontend.

---

## 1. Atlas Orchestrator
- **Role**: Campus Workflow Orchestrator.
- **Execution Pipeline**: 
  `Conversation` ➔ `Streaming Response` ➔ `Thinking Timeline` ➔ `Planning` ➔ `Tool Execution` ➔ `Campus Result`
- **Responsibilities**:
  - Orchestrates campus workflows (degree plans, room finding, course planning, community notices).
  - Streams real-time tokens and visualizes reasoning steps in the Thinking Timeline.
  - Displays high-level tool execution status and durations.
  - Deep-links users directly into owning modules via `CampusResultCard`.
- **Strict Invariants**:
  - Does NOT imitate generic ChatGPT clones.
  - Does NOT duplicate business logic of owning modules.
  - Does NOT own standalone business entities.
  - Does NOT expose raw internal prompts, vector embeddings, or raw payloads.

---

## 2. Calendar Domain
- **Exclusive Owner Of**:
  - Full Month, Week, Day, and Agenda calendar views (`/calendar`).
  - Scheduling and time slot allocations.
  - Drag-and-drop event rescheduling.
  - Personal calendar events management.
  - Time slot conflict visualization (`ConflictIndicator`).
- **Cross-Module Constraint**:
  - Every other module (Dashboard, Planner, Academic, Events) may ONLY preview dates and offer "Open in Calendar" or "Add to Calendar" deep-links.

---

## 3. Resources Center
- **Exclusive Owner Of**:
  - Document repository browsing, searching, and filtering.
  - File downloads and bookmarking.
  - Resource file uploading and editing.
  - Document details and preview modals.
- **Cross-Module Constraint**:
  - Other modules (Academic, Councils, Communities) link to Resources for repository operations rather than duplicating storage/uploading logic.

---

## 4. Dashboard Orchestrator
- **Role**: High-level Aggregator and Navigator (`/dashboard`).
- **Responsibilities**:
  - Aggregates status summaries across User Overview, Academic Summary, Notifications, Planner, Campus Activity, and Atlas Quick Actions.
  - Previews key metrics and recent activities.
  - Navigates users into full owning modules.
- **Strict Invariants**:
  - Holds zero business logic or CRUD mutations.
  - Does NOT render duplicated full calendar month/week grids (delegates to `/calendar`).

---

## 5. Academic Domain
- **Exclusive Owner Of**:
  - Course catalog browsing and details (`useCourses`).
  - Student weekly timetable schedule (`useTimetable`).
  - Degree progress audit and GPA credit tracking (`useDegreePlan`).
  - Academic calendar key dates (exams, registration, holidays).
- **Cross-Module Constraint**:
  - Deep-links to Resource Center (`/resources?category=Academic`) for course syllabi, past papers, and lecture materials.

---

## 6. Planner Domain
- **Exclusive Owner Of**:
  - Tasks management (priorities, categories, progress tracking, archiving).
  - Study goals and target hours tracking (`useStudyGoals`).
  - Combined assignment and task deadlines monitor (`DeadlinesView`).
- **Cross-Module Constraint**:
  - Full calendar scheduling and time slot visualization remains delegated to Calendar via `/calendar` deep-linking.

---

## 7. Domain Separation: Communities vs. Councils

| Domain | Governance Structure | Key Roles | Primary Purpose |
| :--- | :--- | :--- | :--- |
| **Communities** | Informal ownership & interest-driven | `OWNER`, `ADMINISTRATOR`, `MODERATOR`, `MEMBER` | Student clubs, interest groups, discussion feeds, social interaction. |
| **Councils** | Constitutional governance under Student Senate | `PRESIDENT`, `VICE_PRESIDENT`, `SECRETARY`, `TREASURER`, `FACULTY_ADVISOR` | Student government, department councils, policy notices, formal budgets. |

- **Strict Invariant**: Terminology and responsibilities between Communities and Councils never overlap.

---

## 8. Notifications Domain
- **Exclusive Owner Of**:
  - Event-driven delivery status tracking (DELIVERED, SCHEDULED, FAILED).
  - Read/unread state toggling.
  - Archive/restore state management.
  - Unified notification history and category filtering.
- **Cross-Module Constraint**:
  - Notifications always deep-link (`actionLink`) into owning modules rather than duplicating module UI or logic.

---

## 9. Cross-Module Communication Standards
- **No Duplicated CRUD**: Entities belong strictly to their owning domain.
- **Communication Channels**:
  1. **Deep Links**: Standardized URL routing (`/calendar?date=...`, `/resources?category=...`, `/communities/:id`).
  2. **Domain SDKs**: Decoupled TypeScript client SDKs in `src/sdk/`.
  3. **React Query Hooks**: Unified cache keys and server state synchronization (`src/sdk/queryKeys.ts`).
