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

| Feature            | Student | Faculty | Council Admin | Super Admin |
| ------------------ | ------- | ------- | ------------- | ----------- |
| View Events        | Yes     | Yes     | Yes           | Yes         |
| Register for Event | Yes     | Yes     | Yes           | Yes         |
| Create Event       | No      | No      | Yes           | Yes         |
| Edit Event         | No      | No      | Yes           | Yes         |
| Delete Event       | No      | No      | Yes           | Yes         |
| View Participants  | No      | Yes     | Yes           | Yes         |
| Publish Results    | No      | Yes     | Yes           | Yes         |

---

# Resources

| Feature                 | Student | Faculty | Council Admin | Super Admin |
| ----------------------- | ------- | ------- | ------------- | ----------- |
| View Public Resources   | Yes     | Yes     | Yes           | Yes         |
| Request Resource Access | Yes     | No      | No            | No          |
| Upload Resource         | No      | Yes     | Yes           | Yes         |
| Review Access Requests  | No      | Yes     | Yes           | Yes         |
| Approve Access Requests | No      | Yes     | Yes           | Yes         |
| Delete Resource         | No      | Yes     | Yes           | Yes         |

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
