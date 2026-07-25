# Mobile Academic Planner Feature Specification

This document details the mobile user interface, offline draft handling, and API integration for the Academic Planner.

---

## 1. Feature Overview
The Mobile Academic Planner provides a touch-optimized view of a student's degree roadmap, completed courses, and multi-semester course schedule.

---

## 2. Key Screen Workflows

- **Roadmap Overview Screen**: Visual progress indicators displaying completed credits, active term workload, and cumulative GPA.
- **Semester Editor View**: Drag-and-drop or list-based semester course editor allowing students to add or remove courses.
- **Prerequisite Inspector**: Interactive modal showing prerequisite dependency trees for target courses.

---

## 3. Offline Draft & Sync Logic

1. When a student modifies a semester plan offline, modifications are saved to local device cache.
2. A pending sync flag is set. Upon network reconnection, local changes are sent to `POST /api/academic/planner/semester`.
3. If prerequisite validation fails on backend sync, a validation error modal prompts the user to resolve conflict.

---

## Cross-References
- [Planner Module Architecture](file:///D:/CampusGuide/docs/modules/planner.md)
- [Offline Strategy](file:///D:/CampusGuide/docs/mobile/offline-strategy.md)
