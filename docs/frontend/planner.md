# Frontend Planner Module Architecture

This document provides a comprehensive overview of the frontend Planner module architecture in CampusGuide, including data flows, component hierarchy, task lifecycle, study goal tracking, React Query integration, and module boundaries.

---

## 1. Overview & Architecture

The Planner module is built on a decoupled, layered architecture separating UI presentation, TanStack React Query server state hooks, domain models, and SDK-backed API integration.

```
┌───────────────────────────────────────────────────────────────────┐
│                          UI Layer                                 │
│ PlannerPage | PlannerHeader | TaskFilters | TaskCard | Modals     │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                      React Query Hooks                            │
│ useTasks | useCreateTask | useUpdateTask | useMarkTaskComplete... │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                        SDK & Mapper Layer                         │
│ PlannerSdk (BaseSdk) | planner.mapper | planner.dto               │
└─────────────────────────────────┬─────────────────────────────────┘
                                  │
                                  ▼
┌───────────────────────────────────────────────────────────────────┐
│                       Backend REST API                            │
│ /api/planner/tasks | /api/planner/goals | /api/planner/schedules   │
└───────────────────────────────────────────────────────────────────┘
```

---

## 2. Component Hierarchy

```
PlannerPage (Page Component)
 ├── Header (App Navigation)
 ├── PlannerHeader (Hero Banner, Quick Stats Counters, Action Buttons)
 ├── PlannerTabs (Tab Switcher: Tasks, Goals, Deadlines, Academic Summary, Archived)
 └── PlannerErrorBoundary (Section-Level Error Boundary)
      ├── [ Tab: Tasks & Productivity / Archived Tasks ]
      │    ├── TaskFilters (Search, Category, Priority, Status, Due Date, Sort, View Mode)
      │    ├── TaskSkeleton (Loading Fallback)
      │    ├── TaskEmptyState (Empty/Filter Fallback)
      │    ├── TaskCard (Grid & List View Cards)
      │    │    ├── CategoryBadge & PriorityBadge
      │    │    ├── CompleteCheckbox & Line-through Title
      │    │    ├── Interactive ProgressBar Step Controls (-10% / +10%)
      │    │    ├── DueDate & Overdue Indicator
      │    │    ├── TagsList & AttachmentsIndicator
      │    │    └── ActionDropdown (Edit, Archive/Restore, Delete)
      │    └── PaginationControls (Page X of Y, Previous/Next Buttons)
      │
      ├── [ Tab: Study Goals ]
      │    ├── StudyGoalCard (Target vs Completed Hours, Progress Bar, Quick Log +1h/+2h)
      │    └── StudyGoalModal (Create & Edit Goal)
      │
      ├── [ Tab: Deadlines & Milestones ]
      │    └── DeadlinesView (Overdue Alert Box, Upcoming Deadlines, Open Calendar Shortcut)
      │
      ├── [ Tab: Academic Overview ]
      │    └── AcademicSummaryTab (Degree Audit Progress, Enrolled Courses, Timetable Overview)
      │
      ├── TaskFormModal (Create / Edit Task Form with Attachments)
      └── TaskDetailsModal (Full Task Inspection, File Links, Progress Slider)
```

---

## 3. Planner Lifecycle & Data Flow

```
[ User Interaction ] ──> [ React Query Hook ] ──> [ PlannerSdk ] ──> [ REST API / Fallback Storage ]
        │                                                                     │
        └─────────────────── Optimistic Cache Updates ────────────────────────┘
```

1. **Initial Load**:
   - `PlannerPage` mounts and executes parallel queries:
     - `useTasks(filters)`: fetches paginated, filtered, sorted task items.
     - `useStudyGoals()`: fetches active study goals.
     - `useUpcomingDeadlines()`: aggregates tasks, academic calendar items, and study goal deadlines.
     - `useDegreePlan()`, `useEnrolledCourses()`, `useTimetable()`: fetches academic background.
2. **Filtering & Pagination**:
   - Filter state (`search`, `category`, `priority`, `status`, `dueDateFilter`, `sortBy`, `sortOrder`, `page`) triggers automatic React Query key re-fetching without full page re-renders.
3. **Optimistic Mutations**:
   - Completing tasks or updating progress instantly updates local React Query cache while executing `PlannerSdk` calls in the background.

---

## 4. Task Lifecycle

A task transitions through the following status and archiving lifecycle states:

```
[ TODO ] ──(Start / Progress > 0)──> [ IN_PROGRESS ] ──(Progress = 100)──> [ COMPLETED ]
   │                                      │                                      │
   └──────────────────────────────────────┴──────────────────────────────────────┘
                                          │
                               (Archive Action)
                                          ▼
                                     [ ARCHIVED ] ──(Restore Action)──> [ ACTIVE ]
                                          │
                                   (Delete Action)
                                          ▼
                                     [ DELETED ]
```

1. **Creation**:
   - Initiated via `TaskFormModal` (`useCreateTask`).
   - Accepts title, description, category, priority, status, due date, tags, initial progress, and attachments.
2. **Category & Priority Support**:
   - Categories: `PERSONAL`, `ACADEMIC`, `ASSIGNMENT`, `PROJECT`, `STUDY_GOAL`, `EXAMINATION`, `REMINDER`, `MISCELLANEOUS`.
   - Priorities: `URGENT`, `HIGH`, `MEDIUM`, `LOW`.
3. **Progress Updates & Completion**:
   - Quick step buttons (-10% / +10%) or slider on `TaskCard` and `TaskDetailsModal`.
   - Setting progress to 100% automatically sets status to `COMPLETED` and sets `completedDate`.
   - Unchecking completed task resets status to `TODO` and progress to 0%.
4. **Archiving & Restoration**:
   - Moving a task to `ARCHIVED` hides it from active task views while preserving it under the `Archived Tasks` tab.
   - Restoring a task returns it to its previous active state (`TODO`, `IN_PROGRESS`, or `COMPLETED`).
5. **Deletion**:
   - Permanently deletes task item (`useDeleteTask`).

---

## 5. Study Goal Lifecycle

```
[ Create Goal ] ──> [ Log Hours (+1h / +2h) ] ──> [ Target Reached (Completed) ]
```

1. **Definition**:
   - Created via `StudyGoalModal` specifying `title`, `targetHours`, `category`, and optional target `deadline`.
2. **Progress Logging**:
   - Users can log progress using quick `+1h` or `+2h` buttons on `StudyGoalCard` or via manual entry.
   - When `completedHours >= targetHours`, the goal automatically completes.
3. **Deletion**:
   - Goal can be deleted via `useDeleteStudyGoal`.

---

## 6. React Query Cache & SDK Integration

### Query Keys Hierarchy
Defined in `src/sdk/queryKeys.ts`:
- `queryKeys.planner.tasks(params)`
- `queryKeys.planner.task(id)`
- `queryKeys.planner.studyGoals()`
- `queryKeys.planner.upcomingDeadlines()`
- `queryKeys.planner.degreePlan()`
- `queryKeys.planner.enrolledCourses()`
- `queryKeys.planner.timetable(scheduleId)`
- `queryKeys.planner.academicCalendar(term)`

---

## 7. Phase 4.9 Calendar Boundary Safeguards

The Planner module remains strictly focused on productivity and task management:
- **Included**: Upcoming deadlines summary, overdue indicators, recently completed lists, and an "Open in Calendar" shortcut toast preview.
- **Excluded (Phase 4.9 Calendar Responsibilities)**: Full interactive calendar grid views, drag-and-drop scheduling, recurring events engine, agenda management, and timetable conflict detection.
