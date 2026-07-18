# Engineering Audit Report: Student Progress Domain Hardening

## Overview

During the Phase 8 engineering security audit, a vulnerability was identified in the Student Progress module: students were able to directly modify their own academic record fields—specifically `currentGpa`, `totalCreditsEarned`, and `graduationEligible`—via the public client-facing progress update API.

This report documents the remediation applied to harden the Student Progress domain, enforce server-side calculations, establish proper field ownership, and define the future migration path for academic records.

---

## 1. Vulnerability & Risk Analysis

### Vulnerability Description
The endpoint `PUT /api/progress` accepted the `UpdateStudentProgressRequest` DTO, which combined student-editable progress fields (like `currentSemester` and `roadmapId`) with academic record fields (`currentGpa`, `totalCreditsEarned`, and `graduationEligible`). Since the ownership check only validated that a student was the owner of the progress record, any student could supply modified GPA, credits, or graduation status in their request, and the server would save those values without verification.

### Risk Level
* **Critical**: Allows unauthorized modification of core academic metrics, compromising database integrity and bypasses graduation checks.

---

## 2. Hardened Domain Model & Field Ownership

We have decoupled the student-editable fields from administrative academic records.

### Field Restrictions

| Field Name | Student Modifiable | Admin Modifiable | Backend Calculated | Description |
| :--- | :--- | :--- | :--- | :--- |
| `roadmapId` | Yes | Yes | No | The curriculum roadmap the student follows. |
| `currentSemester` | Yes | Yes | No | The student's current active semester. |
| `currentGpa` | **No** | **Yes** (Temporary) | Future Derived | Student's grade point average. |
| `totalCreditsEarned` | **No** | **No** | **Yes** | Calculated from completed courses only. |
| `graduationEligible` | **No** | **No** | **Yes** | Derived from earned credits vs roadmap requirement. |

### Technical Decoupling (DTO Separation)
1. **`UpdateStudentProgressRequest`**: Stripped of `currentGpa`, `totalCreditsEarned`, and `graduationEligible`. This is used for student-initiated updates.
2. **`AdminUpdateStudentProgressRequest`**: Introduced as a separate DTO containing administrative fields (such as `currentGpa` and other record-level fields).

---

## 3. Server-Side Calculations (Source of Truth)

To ensure the backend is the absolute source of truth, all client-supplied data regarding credits and graduation eligibility is ignored. 

### Total Credits Earned
* **Calculation Rule**: The sum of all credits for active and inactive courses listed in the student's completed courses list (`completedCourseIds`).
* **Implementation**: Done via a server-side helper:
  ```java
  private int calculateTotalCredits(StudentProgress progress) {
      if (progress.getCompletedCourseIds() == null || progress.getCompletedCourseIds().isEmpty()) {
          return 0;
      }
      int totalCredits = 0;
      for (String courseId : progress.getCompletedCourseIds()) {
          try {
              CourseResponse course = courseService.getCourseByIdInternal(courseId);
              if (course != null && course.getCredits() != null) {
                  totalCredits += course.getCredits();
              }
          } catch (Exception e) {
              // Ignore if course not found
          }
      }
      return totalCredits;
  }
  ```

### Graduation Eligibility
* **Calculation Rule**: Re-evaluated automatically whenever progress is updated, courses are completed, or roadmaps are changed.
* **Implementation**: Compares `totalCreditsEarned` with the roadmap's required `totalCredits`:
  ```java
  private void recalculateGraduationEligibility(StudentProgress progress) {
      if (progress.getRoadmapId() != null) {
          try {
              RoadmapResponse roadmap = roadmapService.getRoadmapById(progress.getRoadmapId());
              if (roadmap != null && roadmap.getTotalCredits() != null) {
                  progress.setGraduationEligible(progress.getTotalCreditsEarned() >= roadmap.getTotalCredits());
              }
          } catch (Exception e) {
              // Ignore if roadmap not found
          }
      }
  }
  ```

---

## 4. API & Controller Hardening

Endpoint behavior has been secured at the controller level:
* `PUT /api/progress`: Accepts `UpdateStudentProgressRequest`. Accessible to authenticated owners.
* `PUT /api/progress/admin`: Accepts `AdminUpdateStudentProgressRequest`. Restricted to `SUPER_ADMIN` only using Spring Security annotations:
  ```java
  @PutMapping("/admin")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<StudentProgressResponse> adminUpdateProgress(...)
  ```
  Any attempt by a student to invoke this endpoint results in a `403 Forbidden` response.

---

## 5. Testing & Validation

A comprehensive test suite was written to validate the business contract and authorization boundaries.

### Unit Tests (`StudentProgressServiceTest`)
* **Student Updates**: Validates that students can update allowed fields, and academic fields remain unmodified.
* **Admin Updates**: Validates that a SUPER_ADMIN can successfully update GPA and progress details.
* **Role Check**: Validates that student requests to perform administrative updates fail with `AccessDeniedException`.
* **Value Derivation**: Validates that `totalCreditsEarned` and `graduationEligible` are automatically calculated and not trusted from input.

### Integration Tests (`StudentProgressControllerSecurityIT`)
* `PUT /api/progress` with student credentials (allowed fields) $\rightarrow$ `200 OK`.
* `PUT /api/progress/admin` with student credentials (updating GPA/credits/eligibility) $\rightarrow$ `403 Forbidden`.
* `PUT /api/progress/admin` with SUPER_ADMIN credentials (updating academic record) $\rightarrow$ `200 OK`.

---

## 6. Future Migration Roadmap

### 1. Dedicated Faculty Module
* **Current Status**: `SUPER_ADMIN` acts as a temporary placeholder for faculty responsibilities since no dedicated faculty workflows exist in the current MVP.
* **Migration Plan**: When the Faculty module is introduced, the `@PreAuthorize("hasRole('SUPER_ADMIN')")` constraint on the `/api/progress/admin` endpoint will be updated to `@PreAuthorize("hasAnyRole('FACULTY', 'SUPER_ADMIN')")`.

### 2. Fully Derived GPA
* **Current Status**: GPA is manually inputted by `SUPER_ADMIN` since there is no course enrollment/grading module.
* **Migration Plan**: Once a Grading/Enrollment module is implemented, the manually entered `currentGpa` field will be removed from `AdminUpdateStudentProgressRequest`. GPA will then be calculated dynamically on the server by retrieving all student grades, computing the weighted average, and setting it automatically.
