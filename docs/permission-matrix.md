# CampusGuide Permission Matrix (MVP)

## Roles

* STUDENT
* FACULTY
* COUNCIL_ADMIN
* SUPER_ADMIN

---

# Authentication

| Feature      | Student | Faculty | Council Admin | Super Admin |
| ------------ | ------- | ------- | ------------- | ----------- |
| Register     | Yes     | Yes     | Yes           | Yes         |
| Login        | Yes     | Yes     | Yes           | Yes         |
| Edit Profile | Yes     | Yes     | Yes           | Yes         |

---

# Councils

| Feature              | Student | Faculty | Council Admin | Super Admin |
| -------------------- | ------- | ------- | ------------- | ----------- |
| View Councils        | Yes     | Yes     | Yes           | Yes         |
| Apply for Membership | Yes     | No      | No            | No          |
| Review Applications  | No      | No      | Yes           | Yes         |
| Approve Applications | No      | No      | Yes           | Yes         |
| Manage Council       | No      | No      | Yes           | Yes         |

---

# Communities

| Feature            | Student | Faculty | Council Admin | Super Admin |
| ------------------ | ------- | ------- | ------------- | ----------- |
| Join Community     | Yes     | Yes     | Yes           | Yes         |
| Create Post        | Yes     | Yes     | Yes           | Yes         |
| Comment            | Yes     | Yes     | Yes           | Yes         |
| Delete Own Post    | Yes     | Yes     | Yes           | Yes         |
| Moderate Community | No      | No      | Yes           | Yes         |

---

# Events

| Feature | Student | Faculty | Council Admin | Super Admin | Event Organizer (Owner) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| View Events | Yes | Yes | Yes | Yes | Yes |
| Create Event | Yes | Yes | Yes | Yes | N/A (Creator becomes organizer) |
| Update Event | No | No | No | Yes | Yes (Own events only) |
| Delete Event | No | No | No | Yes | Yes (Own events only) |
| Register for Event | Yes | Yes | Yes | Yes | Yes |
| Cancel Registration | Yes | Yes | Yes | Yes | Yes |
| View Registration Status | Yes | Yes | Yes | Yes | Yes |
| View Registrations List | Yes | Yes | Yes | Yes | Yes |

> [!NOTE]
> * **Event Organizer**: Any authenticated user who creates an event becomes its organizer. They are authorized to update and delete their own events.
> * **Super Admin**: Has global override access and can update or delete any event in the system.
> * **Register/Cancel**: Any authenticated user can register or cancel their registration for any active, non-cancelled, and upcoming event.

---

# Resources

| Feature | Student | Faculty | Council Admin | Super Admin | Resource Owner (Uploader) | Unauthenticated |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Browse Resources (Get All/Recent/By Council/By Community/By Tag/By Uploader) | Yes | Yes | Yes | Yes | Yes | No |
| Search Resources | Yes | Yes | Yes | Yes | Yes | No |
| Download Resources | Yes | Yes | Yes | Yes | Yes | No |
| Upload Resource | Yes | Yes | Yes | Yes | N/A (Creator becomes owner) | No |
| Update Own Resource | No | No | No | Yes | Yes | No |
| Update Any Resource | No | No | No | Yes | N/A | No |
| Delete Own Resource | No | No | No | Yes | Yes | No |
| Delete Any Resource | No | No | No | Yes | N/A | No |

> [!NOTE]
> * **Resource Owner (Uploader)**: Any authenticated user who uploads a resource metadata record becomes its owner. They are authorized to update and delete their own resources.
> * **Super Admin**: Has global override access and can update or delete any resource in the system.
> * **Unauthenticated**: Has no access to any resource endpoint. All Resource API routes require authentication (`@PreAuthorize("isAuthenticated()")`).

---

# Academic Planning (Roadmaps, Courses, Student Progress & Semester Plans)

| Feature | Student | Faculty | Council Admin | Super Admin | Owner (Creator/Student) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| View/Search Roadmaps | Yes | Yes | Yes | Yes | Yes |
| Create Roadmap | Yes | Yes | Yes | Yes | N/A (Creator becomes owner) |
| Update Roadmap | No | No | No | Yes | Yes (Own roadmaps only) |
| Delete Roadmap | No | No | No | Yes | Yes (Own roadmaps only) |
| Browse/Search Courses | Yes | Yes | Yes | Yes | Yes |
| Create Course | No | No | No | Yes | No |
| Update Course | No | No | No | Yes | No |
| Delete Course | No | No | No | Yes | No |
| Initialize Progress | Yes | Yes | Yes | Yes | N/A (Self progress only) |
| Update Progress (Permitted fields only) | No | No | No | Yes | Yes (Own progress only) |
| Update Progress (Academic records: GPA, credits, graduation eligibility) | No | No | No | Yes | No |
| View Progress (Own) | Yes | Yes | Yes | Yes | Yes |
| View Progress (Others)| No | No | No | Yes | No |
| Complete/Remove Course| No | No | No | Yes | Yes (Own progress only) |
| Create Semester Plan | Yes | Yes | Yes | Yes | N/A (Self plans only) |
| Update Semester Plan | No | No | No | Yes | Yes (Own plans only) |
| View Semester Plans (Own)| Yes | Yes | Yes | Yes | Yes |
| View Semester Plans (Others)| No | No | No | Yes | No |
| Add/Remove Plan Course| No | No | No | Yes | Yes (Own plans only) |
| Finalize Semester Plan| No | No | No | Yes | Yes (Own plans only) |
| Access Academic Dashboard| Yes | Yes | Yes | Yes | Yes (Own metrics only) |
| Get Course Recommendations| Yes | Yes | Yes | Yes | Yes (Own recommendations) |

> [!NOTE]
> * **Student Progress Permissions**: Students can modify only `roadmapId`, `currentSemester`, and complete/remove courses in their progress records. Restricted fields (`currentGpa`, `totalCreditsEarned`, `graduationEligible`) are computed server-side. `SUPER_ADMIN` manages GPA manually as a Faculty placeholder.

---

# Global Search

| Feature | Student | Faculty | Council Admin | Super Admin |
| :--- | :--- | :--- | :--- | :--- |
| Execute Global Search (`POST /api/search`) | Yes | Yes | Yes | Yes |

---

# AI Conversations

| Feature | Student | Faculty | Council Admin | Super Admin | Owner (Creator) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Create Conversation | Yes | Yes | Yes | Yes | N/A |
| List Own Conversations | Yes | Yes | Yes | Yes | Yes |
| Get Conversation History | No | No | No | No | Yes |
| Rename Conversation | No | No | No | No | Yes |
| Delete Conversation | No | No | No | No | Yes |
| Send Message / Chat | No | No | No | No | Yes |

---

# Personalized Recommendations

| Feature | Student | Faculty | Council Admin | Super Admin |
| :--- | :--- | :--- | :--- | :--- |
| Get All Recommendations | Yes | Yes | Yes | Yes |
| Get Recommendations by Type | Yes | Yes | Yes | Yes |

---

# Notifications

| Feature | Student | Faculty | Council Admin | Super Admin | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| List Notifications (All/Unread) | Yes | Yes | Yes | Yes | Yes (Own only) |
| Get Unread Count | Yes | Yes | Yes | Yes | Yes (Own only) |
| Mark Notification as Read | No | No | No | No | Yes (Own only) |
| Mark All Notifications as Read | Yes | Yes | Yes | Yes | Yes (Own only) |
| Delete Notification | No | No | No | No | Yes (Own only) |

---

# Platform Analytics

| Feature | Student | Faculty | Council Admin | Super Admin |
| :--- | :--- | :--- | :--- | :--- |
| View Consolidated Dashboard | No | No | No | Yes |
| View User Statistics | No | No | No | Yes |
| View Event Statistics | No | No | No | Yes |
| View Community Statistics | No | No | No | Yes |

---

# Administration

| Feature            | Student | Faculty | Council Admin | Super Admin |
| ------------------ | ------- | ------- | ------------- | ----------- |
| Manage Users       | No      | No      | No            | Yes         |
| Manage Roles       | No      | No      | No            | Yes         |
| Platform Analytics | No      | No      | No            | Yes         |

---

# Planned / Future Phase (Deferred)

The following features were specified in initial mock plans but are **deferred** to future phases and are **not** present in the current backend implementation:

### 1. Notices & Announcements
* **Stale Reference**: `Publish Notice` (Faculty/Admin), `Publish Announcement` (Council/Admin), and corresponding view endpoints.
* **Status**: Not implemented.

### 2. Vault
* **Stale Reference**: `Upload Files`, `View/Delete Own Files`, `View Others' Files`.
* **Status**: Not implemented.

### 3. Resume Builder
* **Stale Reference**: `Build Resume`, `Export PDF`.
* **Status**: Not implemented.

### 4. Council Membership Applications & Resource Requests
* **Stale Reference**: `Apply for Membership`, `Approve/Reject Applications`, `Request Resource Access`.
* **Status**: Not implemented.
