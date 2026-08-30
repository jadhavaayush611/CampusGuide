# Planner Domain Specifications

## Overview
The Planner module (`com.campusguide.personal.planner`) handles personal task management and study planning for students within the Personal domain of CampusGuide. It serves as the core task management aggregate root (`PlannerTask`), storing user tasks, priorities, statuses, linked campus events, due dates, reminders, and completed timestamps.

## Domain Model
- **Aggregate Root**: `PlannerTask` (`planner_tasks` collection in MongoDB).
- **Primary Key**: `UUID` (`id`).
- **User Ownership**: `UUID` (`userId` - indexed, mandatory).
- **Audit Fields**: `createdAt`, `updatedAt` (System-managed `LocalDateTime`).

### Schema Definition
| Field | Type | Constraint / Validation | Description |
|---|---|---|---|
| `id` | UUID | Primary Key | Unique task identifier |
| `userId` | UUID | Indexed, Mandatory | Authenticated owner's user ID |
| `title` | String | Mandatory, Non-Blank | Task title |
| `description` | String | Nullable | Detailed task description |
| `type` | TaskType Enum | Mandatory | Task category (`TODO`, `ASSIGNMENT`, `EXAM`, `PROJECT`, `MEETING`, `STUDY`, `PERSONAL`, `EVENT`, `OTHER`) |
| `priority` | TaskPriority Enum | Mandatory | Priority level (`LOW`, `MEDIUM`, `HIGH`, `URGENT`) |
| `status` | TaskStatus Enum | Mandatory | Lifecycle status (`TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`) |
| `linkedEventId` | UUID | Nullable, Indexed | Foreign reference to an Event in `campus.event` |
| `dueAt` | LocalDateTime | Nullable | Task due date and time |
| `completedAt` | LocalDateTime | Nullable, Auto-set | Timestamp when task transitioned to `COMPLETED` |
| `reminderAt` | LocalDateTime | Nullable | Reminder trigger date and time |
| `notes` | String | Nullable | User notes |
| `createdAt` | LocalDateTime | System-managed | Task creation timestamp |
| `updatedAt` | LocalDateTime | System-managed | Last modification timestamp |

## Business Rules & Invariants
1. **User Ownership**: Every planner task belongs to exactly one authenticated user. Ownership is resolved strictly from the authenticated security principal (`UserDetails`). Client API DTOs never accept or expose `userId`.
2. **Title Invariant**: Title is mandatory and cannot be empty or blank.
3. **Linked Event Validation**: When `linkedEventId` is provided, the target Event must exist in `campus.event` (`EventRepository`). Otherwise, raises a `ResourceNotFoundException` (`404`).
4. **Due Date Rule**: `dueAt` cannot precede `createdAt`.
5. **Reminder Rule**: `reminderAt` must be strictly before `dueAt`. Setting a `reminderAt` without a `dueAt` is invalid.
6. **Automatic Completed Timestamp**: Transitioning status to `COMPLETED` automatically records `completedAt = LocalDateTime.now()`. Transitioning away from `COMPLETED` clears `completedAt`.
7. **Reminder Cleanup**: Transitioning status to `CANCELLED` automatically clears `reminderAt`.
8. **Immutability of Completed Tasks**: Once a task is in `COMPLETED` status, only the `notes` field may be updated. Any attempt to modify non-notes fields raises a `PlannerTaskValidationException` (`400 Bad Request`).
9. **Access Control & ID Enumeration Prevention**: Task lookups, updates, status patches, and deletes are strictly scoped by the authenticated user's ID (`findByIdAndUserId`). Inaccessible tasks (whether nonexistent or owned by another user) return `404 Not Found`, eliminating task UUID enumeration.

## Study Goal Domain Model
- **Aggregate Entity**: `StudyGoal` (`study_goals` collection in MongoDB).
- **Primary Key**: `UUID` (`id`).
- **User Ownership**: `UUID` (`userId` - indexed, mandatory).
- **Fields**: `title`, `description`, `targetHours`, `completedHours`, `deadline`, `isCompleted`, `category`, audit fields (`createdAt`, `updatedAt`).
- **Invariants**:
  - Scoped strictly to authenticated student.
  - Inaccessible or foreign goals return `404 Not Found`.
  - Reaching `completedHours >= targetHours` automatically marks `isCompleted = true`.

## Future Readiness & Extensibility
The `PlannerTask` aggregate root is designed for backward-compatible extension in future phases:
- **Calendar Synchronization**: `linkedEventId` and `dueAt`/`completedAt` timestamps provide clean integration hooks for two-way iCal / Google Calendar sync without modifying core entity properties.
- **Reminder Scheduling**: `reminderAt` field provides an indexable trigger for async notification worker jobs.
- **AI Planning**: Task metadata (`type`, `priority`, `dueAt`, `notes`) enables zero-friction ingestion by Atlas AI gateway for automated study schedule generation.
- **Habit Tracking**: `TaskType` enum and `status` lifecycle extend seamlessly into recurring habit series by linking task instances via parent/series references in future migrations.
