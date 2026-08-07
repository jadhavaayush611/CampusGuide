# Test Matrix

This document maps the validation test matrix across all functional and technical domains of the CampusGuide platform.

---

| Domain | Feature Area | Test Case ID | Test Scenario | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Platform** | Authentication | TC-ATH-01 | Login with valid credentials | JWT token issued, user state updated, redirected to Dashboard | **PASS** |
| **Platform** | Authentication | TC-ATH-02 | Login with invalid credentials | Friendly validation error shown, login blocked | **PASS** |
| **Platform** | Authentication | TC-ATH-03 | Silent token refresh | Expiring access token refreshed in the background without UI blocking | **PASS** |
| **Platform** | Session | TC-ATH-04 | Session restoration | Page refresh restores JWT from storage and preserves route | **PASS** |
| **Platform** | Session | TC-ATH-05 | Protected Route redirect | Accessing `/dashboard` unauthenticated redirects to `/login` | **PASS** |
| **Platform** | Auth / Authorization | TC-ATH-06 | Role-based permission guard | STUDENT role trying to write notices gets `403 Forbidden` response | **PASS** |
| **Platform** | Security | TC-ATH-07 | Hardened CORS controls | Wildcard origins rejected under production profile | **PASS** |
| **Dashboard** | Orchestrator | TC-DSH-01 | Widget Loading | All widget skeletons load, followed by hydrated counters | **PASS** |
| **Dashboard** | Orchestrator | TC-DSH-02 | Navigation Drawer | Drawer toggles correctly and routes to respective modules | **PASS** |
| **Academic** | Course Catalog | TC-ACA-01 | Course filter & search | Filtering courses by department and title yields matching courses | **PASS** |
| **Academic** | Degree Plan | TC-ACA-02 | Prerequisite verification | Warning displays if student adds course without meeting prerequisite | **PASS** |
| **Academic** | Student Progress | TC-ACA-03 | Graduation checker | Server calculates cumulative GPA/credits and displays status | **PASS** |
| **Campus** | Communities | TC-COM-01 | Join / Leave Community | Joining updates community count and updates post feeds | **PASS** |
| **Campus** | Forums | TC-COM-02 | Post and Nest Comments | Users create posts, like posts, and nest replies under threads | **PASS** |
| **Campus** | Councils | TC-CON-01 | Discovery & Detail view | Renders council leaders, active applications, and documents | **PASS** |
| **Campus** | Councils | TC-CON-02 | Events RSVP | Toggling RSVP updates event count and displays confirmation | **PASS** |
| **Campus** | Resources | TC-RES-01 | File Upload validation | Attempting to upload files over limits triggers validation banner | **PASS** |
| **Campus** | Resources | TC-RES-02 | Preview and Download | Clicking preview displays file viewer; clicking download pulls file | **PASS** |
| **Campus** | Notices | TC-NTC-01 | Priority Pinning | High-priority pinned notices display at top of noticeboard feed | **PASS** |
| **Campus** | Notices | TC-NTC-02 | Read / Unread marking | Reading a notice marks it read; count in header decrements | **PASS** |
| **Personal** | Planner | TC-PLN-01 | Task Creation & Completion | Adding task sets it to TODO; checking it updates state to COMPLETED | **PASS** |
| **Personal** | Planner | TC-PLN-02 | Study Goal integration | Creating study goal updates degree plan progress bar | **PASS** |
| **Personal** | Calendar | TC-CAL-01 | Views Switch | Switching Day/Week/Month/Agenda updates event cards layout | **PASS** |
| **Personal** | Calendar | TC-CAL-02 | Planner Sync | Dragging event updates task due date in database | **PASS** |
| **Personal** | Notifications | TC-NTF-01 | Mark all as read | Clicking mark all read clears unread state badge immediately | **PASS** |
| **Personal** | Atlas AI | TC-ATL-01 | Context Assembly | Assistant incorporates current enrolled courses and grades | **PASS** |
| **Personal** | Atlas AI | TC-ATL-02 | Response Streaming | Responses stream character-by-character; thinking layout visible | **PASS** |
| **Personal** | Atlas AI | TC-ATL-03 | Workflow Orchestration | Atlas triggers tools (e.g., search catalog) instead of chat | **PASS** |
| **Resilience** | Offline | TC-RES-01 | Network offline mode | Toggling offline displays notification banner, relies on cache | **PASS** |
| **Resilience** | Errors | TC-ERR-01 | Localized Error boundaries | Server 500 error on widget renders a localized error card | **PASS** |
| **Resilience** | Errors | TC-ERR-02 | Session expiration | Expired token triggers user log out and session teardown | **PASS** |
