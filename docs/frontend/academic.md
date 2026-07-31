# Academic Module Architecture & Data Integration

## Overview

The CampusGuide Production Academic Module (`src/app/pages/Academic.tsx`) provides comprehensive course management, weekly class timetables, degree progress tracking, academic calendar milestone tracking, and quick access to study resources.

The module strictly enforces SDK-only communication, domain mapping, and TanStack React Query server-state hooks. Direct API communication, fetch calls, or business logic inside UI components are strictly prohibited.

---

## Component Hierarchy

The Academic page is built using modular, self-contained sections located in `src/app/components/academic/`:

```
Academic Page (src/app/pages/Academic.tsx)
│
├── AcademicSectionErrorBoundary (src/app/components/academic/AcademicSectionErrorBoundary.tsx)
│   └── Top-level & section-level fault-tolerant error boundaries
│
├── AcademicHeader (src/app/components/academic/AcademicHeader.tsx)
│   ├── Enrolled Courses count & Total Credits summary
│   ├── Today's lectures counter & active term badge
│   └── Cumulative GPA & degree progress percentage
│
├── CourseCatalogSection (src/app/components/academic/CourseCatalogSection.tsx)
│   ├── Search bar (course code, title, instructor)
│   ├── Dynamic filters (Department, Term, Registration Status)
│   ├── Sort control (Code, Title, Credits)
│   ├── Course cards grid with prerequisite pills & status badges
│   └── Select course callback to trigger CourseDetailsModal
│
├── TimetableSection (src/app/components/academic/TimetableSection.tsx)
│   ├── Weekly Grid View vs Daily List View toggle
│   ├── Day of week selector pills (Monday .. Sunday)
│   ├── Today's schedule alert banner
│   └── Lecture hall, building name, room, & instructor details
│
├── DegreeProgressSection (src/app/components/academic/DegreeProgressSection.tsx)
│   ├── Graduation progress bar & completed vs remaining credits
│   ├── Cumulative GPA & estimated graduation term
│   ├── Curriculum category breakdown (Core, GenEd, Electives, Capstone)
│   └── Planned & completed terms semester roadmap accordion
│
├── AcademicCalendarSection (src/app/components/academic/AcademicCalendarSection.tsx)
│   ├── Upcoming exams, registration windows, milestones, & holidays
│   └── Category filter pills (Exams, Registration, Milestones, Holidays)
│
├── AcademicResourcesSection (src/app/components/academic/AcademicResourcesSection.tsx)
│   ├── Quick access to course notes, past papers, lab manuals, & syllabi
│   └── Reuses useResources() hook from Campus SDK
│
└── CourseDetailsModal (src/app/components/academic/CourseDetailsModal.tsx)
    └── Accessible modal drawer displaying complete course syllabus & prerequisites
```

---

## Data Flow Architecture

All academic data flows through a strict single-direction pipeline:

```
[ Backend REST APIs / Fallback DTOs ]
                │
                ▼
[ BaseSdk / PlannerSdk / CampusSdk ]
                │
                ▼
[ Domain Mappers (mapCourseDtoToModel, mapTimetableSlotDtoToModel, mapDegreePlanDtoToModel, mapAcademicCalendarItemDtoToModel) ]
                │
                ▼
[ TanStack React Query Hooks (useCourses, useEnrolledCourses, useTimetable, useDegreePlan, useAcademicCalendar, useResources) ]
                │
                ▼
[ Reusable Academic UI Components (AcademicHeader, CourseCatalogSection, TimetableSection, DegreeProgressSection, AcademicCalendarSection, AcademicResourcesSection) ]
```

---

## Query Strategy & Performance

1. **Parallel Execution**: `Academic.tsx` invokes all React Query hooks concurrently upon mounting, preventing waterfall requests.
2. **Cache Reuse**:
   - `useCourses()`: 10 minutes stale time.
   - `useEnrolledCourses()`: Reuses course query cache with TanStack React Query `select` filtering (`status === 'ENROLLED' || status === 'IN_PROGRESS'`).
   - `useDegreePlan()`: 10 minutes stale time.
   - `useAcademicCalendar()`: 10 minutes stale time.
3. **Resilient Pending Endpoints**: `PlannerSdk` includes fallback mock data for pending endpoints, keeping the UI fully operational during dev/test cycles.

---

## Query Key Hierarchy (`src/sdk/queryKeys.ts`)

```typescript
export const queryKeys = {
  planner: {
    all: ['planner'] as const,
    schedules: () => [...queryKeys.planner.all, 'schedules'] as const,
    schedule: (id: string) => [...queryKeys.planner.schedules(), id] as const,
    courses: (department?: string) => [...queryKeys.planner.all, 'courses', { department }] as const,
    course: (id: string) => [...queryKeys.planner.all, 'courses', id] as const,
    timetable: (scheduleId?: string) => [...queryKeys.planner.all, 'timetable', { scheduleId }] as const,
    studyGoals: () => [...queryKeys.planner.all, 'studyGoals'] as const,
    degreePlan: () => [...queryKeys.planner.all, 'degreePlan'] as const,
    academicCalendar: (term?: string) => [...queryKeys.planner.all, 'academicCalendar', { term }] as const,
  },
};
```

---

## Search & Filtering Strategy

- **Client-Side Dynamic Filtering**: Filtering in `CourseCatalogSection` and `AcademicCalendarSection` operates in-memory over cached React Query server state, providing instant response times without unnecessary API refetches.
- **Dynamic Filter Extraction**: Department, term, and status options in filter dropdowns are computed dynamically from active dataset items.
- **Sorting Options**: Courses support sorting by course code, title, and credit weight.

---

## Error Boundaries & UX Resilience

Each section of the Academic module is wrapped in `AcademicSectionErrorBoundary`. If an individual endpoint fails or encounters an unhandled exception:
1. The error is isolated to that section.
2. The user is presented with a clean error fallback banner and a "Retry Loading" button.
3. All other academic sections remain functional and visible.

---

## Standards & Verification

- `npm run typecheck` (`tsc --noEmit`) verified cleanly (0 errors).
- `npm run build` (`vite build`) verified cleanly (production bundle built successfully).
