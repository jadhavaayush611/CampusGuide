# Personal Calendar Domain Specification

## 1. Overview
The Calendar module (`com.campusguide.personal.calendar`) provides a foundational aggregate root for managing personal, academic, task-linked, and event-linked time entries for students within the CampusGuide platform.

## 2. Domain Model & Invariants

### Aggregate Root: `CalendarEntry`
- **Primary Key**: `id` (`UUID`)
- **Ownership**: `userId` (`UUID`), extracted strictly from authentication context (`SecurityContext`).
- **Core Fields**:
  - `title` (`String`, mandatory, non-blank)
  - `description` (`String`, optional)
  - `type` (`CalendarEntryType`: `ACADEMIC`, `EVENT`, `TASK`, `PERSONAL`, `OTHER`)
  - `linkedPlannerTaskId` (`UUID`, optional, existence validated against `PlannerTaskRepository`)
  - `linkedEventId` (`UUID`, optional, existence validated against `EventRepository`)
  - `location` (`String`, optional)
  - `startTime` (`LocalDateTime`, mandatory)
  - `endTime` (`LocalDateTime`, mandatory)
  - `isAllDay` (`boolean`, defaults to `false`)
  - `color` (`String`, hex color representation)
  - `notes` (`String`, optional user notes)
  - Audit Timestamps: `createdAt`, `updatedAt` (`LocalDateTime`)

### Invariants & Business Rules
1. **Ownership Isolation**: Users can strictly query, modify, or delete only their own calendar entries. `userId` is derived from authenticated session details and cannot be specified by the API consumer.
2. **Chronological Validity**: `startTime` must strictly precede `endTime`.
3. **Reference Mutual Exclusivity**: A `CalendarEntry` can reference either a `linkedPlannerTaskId` or a `linkedEventId`, but never both simultaneously.
4. **Reference Existence**: If `linkedPlannerTaskId` or `linkedEventId` is supplied, the referenced entity must exist in the database.
5. **Overlapping Range Query**: Range queries retrieve entries overlapping the interval `[from, to]`, satisfying `startTime < to AND endTime > from`, ordered chronologically by `startTime` ascending, then `endTime` ascending.

## 3. Future Extension Points
- **Recurring Entries**: Aggregate schema design supports future non-breaking addition of `recurrenceRule`, `recurrenceType`, and `parentEntryId`.
- **Reminder Scheduling**: Easily extended with `reminderAt` or integration with the `Notification` domain.
- **External Sync**: Standard ISO timestamp structures enable seamless Google Calendar / iCal export and Atlas AI automatic scheduling integrations.
