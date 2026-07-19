# Academic Module (Roadmaps, Courses & Semester Plans)

The **Academic** module handles curriculum catalogs, degree roadmaps, semester course planning, and personalized progress dashboard metrics.

---

## 1. Module Overview

The Academic package is divided into three key entities and a dynamic aggregator service:
1. **Academic Roadmaps (`roadmaps` collection)**: Defines the structured degree programs, departments, and total credit limits for graduation.
2. **Course Catalog (`courses` collection)**: Catalog of mandatory and elective courses, prerequisites, and semesters they are offered.
3. **Semester Planner (`semester_plans` collection)**: Student-specific plans tracking which courses are scheduled/taken in a specific semester.
4. **Academic Dashboard / Service**: A dynamic aggregate layer that compiles roadmap requirements, current progress, and recommended courses.

---

## 2. Security & Constraints

* **Student Restrictions**:
  - Students can view/search roadmaps and the course catalog.
  - Students can initialize, update, and manage courses/finalization on their own semester plans.
  - Students cannot edit the Course Catalog or modify other students' semester plans.
* **Super Admin / Faculty Override**:
  - `SUPER_ADMIN` acts as the catalog manager. Only admins can create/update/delete course offerings in the catalog.
  - Admins can view semester plans for any student.

---

## 3. Implemented REST Endpoints

### 3.1 Course Catalog Endpoints
* **GET `/api/courses`**: List active courses.
* **GET `/api/courses/{courseId}`**: Get specific course.
* **GET `/api/courses/department/{department}`**: Filter courses by department.
* **GET `/api/courses/semester/{semester}`**: Filter courses by semester.
* **POST `/api/courses`**, **PUT `/api/courses/{courseId}`**, **DELETE `/api/courses/{courseId}`**: Catalog management (`SUPER_ADMIN` only).

### 3.2 Roadmap Endpoints
* **GET `/api/roadmaps`**: List active roadmaps.
* **GET `/api/roadmaps/{roadmapId}`**: Get specific roadmap.
* **GET `/api/roadmaps/creator/{userId}`**: List roadmaps created by a user.
* **POST `/api/roadmaps`**: Create a roadmap (Authenticated).
* **PUT `/api/roadmaps/{roadmapId}`**, **DELETE `/api/roadmaps/{roadmapId}`**: Manage roadmap (Creator or `SUPER_ADMIN` only).

### 3.3 Semester Planner Endpoints
* **POST `/api/semester-plans`**: Initialize a semester plan.
* **GET `/api/semester-plans`**: List own semester plans.
* **GET `/api/semester-plans/{planId}`**: Get specific plan details.
* **PATCH `/api/semester-plans/{planId}/add/{courseId}`**: Add course to plan.
* **PATCH `/api/semester-plans/{planId}/remove/{courseId}`**: Remove course from plan.
* **PATCH `/api/semester-plans/{planId}/finalize`**: Finalize plan (moves to read-only state).
* **GET `/api/semester-plans/student/{studentId}`**: View student plan (`SUPER_ADMIN` only).

### 3.4 Academic Dashboard Endpoints
* **GET `/api/academic/dashboard`**: Aggregates roadmap progress, credits, completed courses, and planned courses.
* **GET `/api/academic/progress`**: Retrieves simplified GPA, total credits earned, and graduation status.
* **GET `/api/academic/recommended-semester`**: Resolves which courses are recommended to take next based on roadmap and completed prerequisites.
