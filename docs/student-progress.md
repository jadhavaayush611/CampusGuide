# Student Progress Module Documentation

The **Student Progress** module tracks students' academic achievements, milestones, roadmaps, current active semester, GPA, completed courses, and eligibility for graduation.

---

## 1. Domain Model Overview

The student progress record contains both user-controlled planning information and faculty-controlled academic record information.

* **User/Student-Controlled Fields**:
  - `roadmapId`: The curriculum roadmap code selected by the student.
  - `currentSemester`: The semester number currently being completed.
  - `completedCourseIds`: The list of courses the student has completed.

* **Server-Controlled / Academic Record Fields**:
  - `currentGpa`: Grade Point Average, representing overall academic standing.
  - `totalCreditsEarned`: Total number of academic credits accumulated.
  - `graduationEligible`: Flag indicating if the student has met all credits and requirements to graduate.

---

## 2. Field Ownership & Security Model

To prevent privilege escalation and coordinate data integrity, access control is enforced at the service and endpoint level.

### Permitted User Operations (Role-Based Access Control)

1. **STUDENT (Owner)**:
   - Can initialize their own progress record.
   - Can update their `roadmapId` or `currentSemester` via `PUT /api/progress`.
   - Can mark courses complete or remove them via `PATCH /api/progress/complete/{courseId}` and `PATCH /api/progress/remove/{courseId}`.
   - **Strictly Prohibited**: Cannot directly modify their `currentGpa`, `totalCreditsEarned`, or `graduationEligible`.

2. **SUPER_ADMIN (Faculty Placeholder)**:
   - Can view and manage all student progress records.
   - Can modify `currentGpa` and other record-level metrics via `PUT /api/progress/admin`.
   - Recalculates total credits and graduation status.

---

## 3. Server-Derived Calculations

Core academic record fields are never updated directly from client-submitted values. Instead, they are calculated server-side.

### Total Credits Earned
Whenever a course is completed, removed, or a progress record is saved:
1. The server retrieves the student's completed courses list (`completedCourseIds`).
2. It fetches each course (including inactive ones) via an internal database lookup.
3. The total credits are calculated as the sum of all completed course credits.

### Graduation Eligibility
The server automatically recalculates graduation eligibility:
1. Compares the derived `totalCreditsEarned` with the total credits required by the active `Roadmap`.
2. Sets `graduationEligible` to `true` if `totalCreditsEarned >= roadmap.totalCredits`, and `false` otherwise.

---

## 4. Temporary Implementation Details & Future Migration Path

### SUPER_ADMIN as Faculty Placeholder
In the current MVP phase, a dedicated **Faculty** role has not yet been fully implemented. Therefore, the `SUPER_ADMIN` role acts as the temporary academic authority for administrative overrides (such as GPA updates).
* **Future Migration**: Once a Faculty module is developed, the administrative endpoints (`PUT /api/progress/admin`, etc.) will be adjusted to allow the `FACULTY` role instead of or in addition to `SUPER_ADMIN`.

### Manually Set GPA vs. Fully Derived GPA
Currently, GPA is updated manually by administrators through the admin endpoint since a grading/transcript system does not yet exist.
* **Future Migration**: When grades are implemented, the `currentGpa` field will be calculated directly by the backend based on letter grades or numerical scores recorded for each completed course. Manual overrides will be phased out.
