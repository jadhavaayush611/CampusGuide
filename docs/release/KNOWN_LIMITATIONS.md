# Known Limitations

This document lists the non-blocking limitations for the CampusGuide `v1.0.0-MVP` Release Candidate. These issues do not block release certification but are slated for resolution in subsequent versions (e.g. `v1.1.0`).

---

## 1. File Uploads (Local Fallback)
* **Limitation**: The system currently defaults to local directory file uploads in `backend/uploads/` instead of an AWS S3 bucket.
* **Impact**: In a multi-instance containerized deployment, file updates will not be synchronized unless a shared volume or custom storage provider is configured.
* **Workaround**: Configure directory mapping or set up a shared network mount on the server. True AWS S3 driver config is already scaffolded and can be turned on by injecting S3 environment properties.

## 2. Push Notifications
* **Limitation**: Firebase Cloud Messaging (FCM) is fully integrated into the backend notification engine but lacks a mock web-push service worker in development environment profiles.
* **Impact**: Real-time push alerts default to local in-app drawer notifications in the frontend interface.
* **Workaround**: Real-time notification updates remain fully readable in the top header drawer widget.

## 3. Calendar Drag & Drop Touch Support
* **Limitation**: The calendar drag-and-drop mechanism uses standard `react-dnd` desktop cursor event listeners.
* **Impact**: Mobile touch gestures on responsive touchscreens are not fully optimized for calendar drag operations.
* **Workaround**: Users can manually open the task modal to change dates on mobile devices, or tap on days to trigger updates.

## 4. Atlas AI Response Token Limits
* **Limitation**: Extremely long chats might truncate context details sent to the AI gateway.
* **Impact**: The context window is optimized for 8,000 tokens. Excessively long conversations may omit older context items.
* **Workaround**: Cleared threads reset the history buffer to guarantee clean academic context retrieval.

## 5. Offline Write Queue
* **Limitation**: The offline state enables viewing cached data through TanStack Query, but writes (creating tasks, posting comments) are blocked.
* **Impact**: Attempting a write action offline displays a validation banner advising the user to check their connection.
* **Workaround**: Safe write actions are blocked when offline to prevent local sync merge conflicts.
