# Calendar Module Architecture

## 1. Purpose
The Calendar module provides a consolidated, read-only temporal view of all student-relevant schedules, deadlines, events, and academic milestones across the platform.

> [!IMPORTANT]
> **ARCHITECTURAL INVARIANT**: The Calendar module **owns no primary data**. It functions strictly as an aggregation view layer over source domains (Academic, Campus, Personal).

---

## 2. Responsibilities
- Aggregate academic schedule items (course class times, assignment deadlines, exam schedules).
- Aggregate campus events (council meetings, community workshops, RSVPs).
- Display personal reminders and system notification deadlines in a unified timeline.

---

## 3. Entities
- *No dedicated primary database entities*. Uses transient view models:
  - `CalendarItem` (DTO): Standardized calendar event payload containing title, startTime, endTime, category, referenceUrl, and sourceDomain.

---

## 4. Services
- `CalendarAggregationService`: Queries Academic, Campus, and Personal services in parallel or sequence, converting domain models into unified `CalendarItem` payloads.

---

## 5. APIs
- `GET /api/calendar`: Fetch aggregated schedule events within a specified date range (`startDate`, `endDate`).
- `GET /api/calendar/export.ics`: Export calendar events in iCalendar standard format for sync with external apps (Google Calendar, Apple Calendar).

---

## 6. Future Improvements
- Personal custom event overlays (non-academic personal reminders).
- Two-way synchronization with external calendar providers (OAuth Google Calendar API).

---

## Cross-References
- [Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Mobile Calendar Specification](file:///D:/CampusGuide/docs/mobile/calendar.md)
