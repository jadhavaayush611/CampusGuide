# Resources Module

The **Resources** module enables students and faculty to upload and share study materials, lecture slides, assignments, and notes on the CampusGuide platform.

---

## 1. Domain & Storage Architecture

Resources are managed via database metadata combined with local disk storage:
* **Resources Metadata (`resources` collection)**: Holds title, tags, uploader ID, associated council/community ID, MIME type, file size, download path, and soft delete flags.
* **Physical File Storage**: Files are saved to the local directory `uploads/resources/` (via a UUID-based filename to prevent clashes) during this MVP phase.
* **Size/Type Restrictions**: Supports files up to 20MB. Allowed file formats include PDF, Word Documents, Excel Sheets, PowerPoint Slides, and images (JPEG/PNG).

---

## 2. Authorization Rules

* **Any Authenticated User**:
  - Can browse and search all active resources.
  - Can download files.
  - Can upload new resources (becoming the resource owner).
* **Resource Owner (Uploader)**:
  - Can update resource metadata or delete/remove their own resource.
* **Super Admin**:
  - Full override access to manage and delete any resource metadata and file.

---

## 3. Implemented REST Endpoints

* **POST `/api/resources`**: Upload a new file (accepts `multipart/form-data` with metadata parameters).
* **PUT `/api/resources/{resourceId}`**: Update resource metadata like title or tags (Uploader or `SUPER_ADMIN`).
* **DELETE `/api/resources/{resourceId}`**: Soft-delete resource (Uploader or `SUPER_ADMIN`).
* **GET `/api/resources`**: List all active resources.
* **GET `/api/resources/{resourceId}`**: Get specific resource metadata.
* **GET `/api/resources/search`**: Search resources by title/description query.
* **GET `/api/resources/recent`**: Get latest uploaded resources.
* **GET `/api/resources/tag/{tag}`**: Filter resources by tag.
* **GET `/api/resources/uploader/{uploaderId}`**: List resources uploaded by a specific user.
* **GET `/api/resources/council/{councilId}`**: List resources linked to a specific council.
* **GET `/api/resources/community/{communityId}`**: List resources linked to a specific community.
* **GET `/api/resources/download/{resourceId}`**: Download the physical file (returns a binary stream with proper MIME headers).
