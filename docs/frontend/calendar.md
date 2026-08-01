# CampusGuide Frontend — Calendar Module Documentation

## 1. Overview & Architecture

The **Calendar Module** serves as the single source of truth for scheduling and time management within the CampusGuide application. It aggregates all time-bound data across personal events, academic planners, study goals, council events, community activities, reminder schedules, and semester milestones into a unified, interactive calendar experience.

### Architectural Principles
- **SDK-Only Communication**: Components never issue direct `fetch` or `axios` calls. All REST API communications are encapsulated inside type-safe SDK classes (`CalendarSdk`, `PlannerSdk`, `CampusSdk`, `ScheduledNotificationSdk`).
- **TanStack React Query Server State**: All data fetching, caching, parallel aggregation, and optimistic state updates are managed via React Query using a centralized hierarchy (`queryKeys.calendar`, `queryKeys.planner`, `queryKeys.campus`, etc.).
- **Unifying Adapter Layer**: The `useAggregatedCalendarEvents` custom hook acts as the central data fusion engine, converting heterogeneous domain entities into standard `AggregatedCalendarEvent` objects.
- **Visual Conflict Detection**: Non-all-day events are analyzed across time slots to compute overlap conditions, rendering visual warning badges without automatic, destructive conflict mutation.
- **Deep-Link Interoperability**: Query string parameters (`?view=...`, `?date=...`, `?eventId=...`, `?filter=...`) allow external modules (Planner, Councils, Academic) to navigate directly into Calendar views.

---

## 2. Data Flow & Aggregation Pipeline

```
[ CalendarSdk ] --------> Personal Events ---------\
[ PlannerSdk ] ---------> Tasks & Deadlines --------\
[ PlannerSdk ] ---------> Study Goals ---------------\
[ PlannerSdk ] ---------> Academic Calendar ---------> [ useAggregatedCalendarEvents ] ---> Standardized Events ---> Calendar Views
[ CampusSdk ] ----------> Council/Community Events --/    (Normalizer & Conflict Scorer)    (Month, Week, Day, Agenda)
[ ScheduledNotifSdk ] -> Reminder Schedules --------/
```

### Aggregation Pipeline Steps:
1. **Parallel Fetching**: React Query fetches parallel server state streams for Personal Entries, Planner Tasks, Study Goals, Academic Calendar Items, Campus/Council Events, and Scheduled Reminders.
2. **Normalization**: Each entity is transformed into the standard `AggregatedCalendarEvent` schema:
   - `id`: Unique composite identifier (`personal-{id}`, `planner-{id}`, `academic-{id}`, etc.)
   - `originalId`: Domain entity ID
   - `title` & `description`: Formatted title and summary text
   - `sourceModule`: Originating module tag (`personal`, `planner`, `study_goals`, `academic`, `council`, `community`, `reminder`, `milestone`)
   - `category`: Event category badge (`PERSONAL`, `ACADEMIC`, `TASK`, `EXAM`, `GOAL`, `COUNCIL`, `COMMUNITY`, `REMINDER`, `MILESTONE`)
   - `startTime` & `endTime`: Normalized JavaScript `Date` objects
   - `isAllDay`: Boolean flag indicating full-day events
   - `color`: Tailored brand hex color code
   - `linkUrl`: Deep link back to the originating module/entity page
3. **Conflict Detection**: Iterates over timed events to evaluate time-window overlaps (`eventA.startTime < eventB.endTime && eventA.endTime > eventB.startTime`). Sets `hasConflict = true` and populates `overlappingEventIds`.
4. **Filtering Engine**: Filters the aggregated list against client state (`searchQuery`, `selectedModules`, `selectedCategories`, `showCompleted`, `showSharedOnly`).

---

## 3. Calendar Views & Component Hierarchy

```
CalendarPage
├── Header
├── CalendarHeader
│   ├── Date Navigation Controls (Prev, Next, Today, Date Picker)
│   ├── Title Display (Formatted per view)
│   ├── Search Bar Input
│   └── View Mode Switcher (Month | Week | Day | Agenda)
├── CalendarSidebarFilter
│   ├── Conflict Status Alert Widget
│   ├── Source Module Checkboxes
│   └── Display Options (Show Completed)
├── CalendarErrorBoundary
│   ├── MonthView (7x5/6 Grid, Day Headers, Event Pills, Badges, Overflow)
│   ├── WeekView (7-Day Hourly Grid 00:00-23:00, All-Day Banner, Drag & Drop Reschedule)
│   ├── DayView (Single Day Detailed Hourly Grid, Overlap Columns, Conflict Banner)
│   └── AgendaView (Grouped Chronological List by Date, Module Badges, Actions)
├── EventDetailsModal (Metadata, Participants, Attachments, Originating Link, Edit/Delete)
└── EventFormModal (Create / Edit Personal Event Form)
```

### Supported Views:
- **Month View**: Standard 7x5 or 7x6 month grid. Renders up to 3 events per day with overflow badges (`+N more`), today highlighting, and conflict indicators.
- **Week View**: 7-day hourly grid with scrollable 24-hour time slots. Supports drag-and-drop rescheduling for personal events and duration resize.
- **Day View**: Focused single-day timeline grid displaying high-resolution event cards, all-day items, and conflict warnings.
- **Agenda View**: Chronological grouped list of events categorized by date with quick action links to originating modules.

---

## 4. Event Lifecycle & Actions

### Event Lifecycle:
1. **Creation**:
   - Personal events are created via `EventFormModal` calling `useCreateCalendarEntry` mutation (`POST /api/v1/calendar`).
   - Events from other modules (Planner tasks, Study goals, Council events) are created within their respective modules and aggregated automatically.
2. **Reading & Inspection**:
   - Clicking any event card opens `EventDetailsModal`, showing title, description, source module, category, start/end time, all-day status, location, participants, and attachments.
3. **Updating & Rescheduling**:
   - Dragging a personal event block in Week View updates `startTime` and `endTime` via `useUpdateCalendarEntry` (`PUT /api/v1/calendar/{id}`).
   - Form editing updates title, description, location, category, color, and times.
4. **Deletion**:
   - Personal events can be deleted via `EventDetailsModal` calling `useDeleteCalendarEntry` (`DELETE /api/v1/calendar/{id}`).

---

## 5. Navigation & Inter-Module Deep Linking

### Supported Query Parameters:
- `view`: Set default view (`month`, `week`, `day`, `agenda`). Example: `/calendar?view=week`
- `date`: Focus on a specific date (`YYYY-MM-DD`). Example: `/calendar?date=2026-08-05`
- `eventId`: Automatically open details modal for a target event ID. Example: `/calendar?eventId=cal-123`
- `filter`: Pre-select a source module filter (`planner`, `academic`, `council`, `personal`). Example: `/calendar?filter=planner`

### Navigation Entrypoints:
- **Planner Page**: "Open in Calendar" button navigates to `/calendar?filter=planner`.
- **Council Events**: "View in Calendar" button navigates to `/calendar?filter=council&eventId={id}`.
- **Academic Calendar**: "Open Calendar" button navigates to `/calendar?filter=academic`.
- **Dashboard**: "Full Campus Calendar" tab navigates directly to `/calendar`.

---

## 6. Performance & Verification

- **Query Caching & Parallelization**: Server state streams are cached with stale times between 2 and 5 minutes to prevent redundant network calls.
- **Lazy View Rendering**: Calendar views only process and position events relevant to the current date window.
- **Section Error Boundaries**: Wrapped inside `CalendarErrorBoundary` to catch UI rendering exceptions without crashing the main application.
- **Verification**: Frontend builds cleanly with zero TypeScript errors (`npm run build`).
