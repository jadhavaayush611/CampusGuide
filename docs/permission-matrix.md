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

# Notices & Announcements

| Feature              | Student | Faculty | Council Admin | Super Admin |
| -------------------- | ------- | ------- | ------------- | ----------- |
| View Notices         | Yes     | Yes     | Yes           | Yes         |
| Publish Notice       | No      | Yes     | No            | Yes         |
| View Announcements   | Yes     | Yes     | Yes           | Yes         |
| Publish Announcement | No      | No      | Yes           | Yes         |

---

# Vault

| Feature            | Student | Faculty | Council Admin | Super Admin |
| ------------------ | ------- | ------- | ------------- | ----------- |
| Upload Files       | Yes     | Yes     | Yes           | Yes         |
| View Own Files     | Yes     | Yes     | Yes           | Yes         |
| Delete Own Files   | Yes     | Yes     | Yes           | Yes         |
| View Others' Files | No      | No      | No            | Yes         |

---

# Resume Builder

| Feature      | Student | Faculty | Council Admin | Super Admin |
| ------------ | ------- | ------- | ------------- | ----------- |
| Build Resume | Yes     | Yes     | Yes           | Yes         |
| Export PDF   | Yes     | Yes     | Yes           | Yes         |

---

# Roadmaps

| Feature                | Student      | Faculty      | Council Admin | Super Admin |
| ---------------------- | ------------ | ------------ | ------------- | ----------- |
| View Pre-made Roadmaps | Yes          | Yes          | Yes           | Yes         |
| View AI Roadmaps       | Premium Only | Premium Only | Premium Only  | Yes         |

---

# Administration

| Feature            | Student | Faculty | Council Admin | Super Admin |
| ------------------ | ------- | ------- | ------------- | ----------- |
| Manage Users       | No      | No      | No            | Yes         |
| Manage Roles       | No      | No      | No            | Yes         |
| Platform Analytics | No      | No      | No            | Yes         |
