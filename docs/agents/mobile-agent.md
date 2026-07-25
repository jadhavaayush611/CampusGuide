# Mobile Agent Operational Guide

This document defines operational rules, offline-first principles, and code quality expectations for AI coding agents developing CampusGuide mobile applications.

---

## 1. Project Vision
Deliver a native-grade, offline-resilient mobile experience allowing students to access their academic planner, calendar, community feeds, and Atlas AI advisor on the go.

---

## 2. Architecture
- **Platforms**: iOS / Android / React Native.
- **Offline Persistence**: Local SQLite / WatermelonDB cache layer.
- **Security**: Device Keychain / Keystore encrypted JWT storage.

---

## 3. Responsibilities
- Implement mobile screen views, navigation stacks, and deep link handlers.
- Manage local offline caching, pending mutation queues, and background sync routines.
- Integrate native device capabilities (EventKit / CalendarContract, FCM / APNs notifications).

---

## 4. Coding Standards
- Implement strict offline fallback logic for all read operations.
- Encrypt sensitive authentication tokens inside secure hardware storage.
- Adhere to WCAG 2.1 AA accessibility guidelines and touch target sizes (48x48 dp minimum).

---

## 5. Naming Conventions
- Screens: PascalCase with `Screen` suffix (`RoadmapScreen`, `CouncilDetailScreen`).
- Stores / ViewModels: CamelCase with `Store` or `VM` suffix (`plannerStore`, `calendarVM`).

---

## 6. What NOT to Do

> [!CAUTION]
> **CRITICAL INVARIANTS**:
> - **Calendar owns no data**: Native calendar integrations must aggregate schedule items from domain endpoints without storing independent primary events.
> - **Atlas never mutates data directly**: AI assistant suggestions must require explicit user tap approval before firing API mutations.
> - **Business logic belongs in services**: Do not hardcode prerequisite rules or GPA calculation formulas in mobile view components.
> - **Councils and Communities are separate concepts**: Maintain distinct screens and navigation paths for student councils and interest communities.
> - **Shared resources should not be duplicated**: Cache shared study resources by server ID without duplicating records locally.

---

## 7. Development Workflow
1. Review mobile feature specifications in `docs/mobile/`.
2. Build UI screens using mobile design tokens and navigation guidelines.
3. Validate offline caching and reconnection sync behavior.
4. Verify deep link resolution and push payload handling.

---

## 8. Expected Output Quality
- Fluid 60 FPS UI performance with zero UI thread blocking.
- Graceful error handling for network offline states.
- Clean pass on app store release checklists.

---

## Cross-References
- [Mobile Overview](file:///D:/CampusGuide/docs/mobile/mobile-overview.md)
- [Offline Strategy](file:///D:/CampusGuide/docs/mobile/offline-strategy.md)
