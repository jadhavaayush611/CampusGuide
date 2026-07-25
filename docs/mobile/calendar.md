# Mobile Schedule Calendar Specification

This document details the mobile schedule view, native calendar syncing, and event reminder integrations.

---

## 1. Feature Overview
The Mobile Calendar displays a daily, weekly, and monthly consolidated view of classes, exam schedules, council events, and personal reminders.

---

## 2. Native Calendar Sync Integration

Mobile apps can sync platform calendar items with native device calendars:
- **iOS**: Uses `EventKit` framework to write `EKEvent` items to Apple Calendar.
- **Android**: Uses `CalendarContract` provider to register events in Google Calendar.

---

## 3. Local Push Reminders

Local scheduled push notifications alert students prior to upcoming class times, assignment deadlines, or RSVP'd campus events (e.g. 15-minute / 1-hour pre-event alerts).

---

## Cross-References
- [Calendar Module Architecture](file:///D:/CampusGuide/docs/modules/calendar.md)
- [Mobile Notifications](file:///D:/CampusGuide/docs/mobile/notifications.md)
