# Notifications Module Architecture

## 1. Purpose
The Notifications module delivers real-time and asynchronous alert messages to users regarding system events, academic reminders, event RSVPs, council updates, and Atlas recommendations.

---

## 2. Responsibilities
- Receive application event triggers from other domain modules.
- Persist in-app notification records in MongoDB Atlas.
- Manage user notification preference channels (in-app, push, email).
- Provide mark-as-read and bulk management endpoints.

---

## 3. Entities
- `Notification`: Stores recipient userId, title, message body, category (`ACADEMIC`, `CAMPUS`, `SYSTEM`, `ATLAS`), read status, and action deep link payload.
- `NotificationPreference`: Per-user channel configuration and muted category settings.

---

## 4. Services
- `NotificationService`: Dispatches notifications, handles read receipts, and enforces user preference suppressions.

---

## 5. APIs
- `GET /api/personal/notifications`: Fetch paginated user notifications.
- `PATCH /api/personal/notifications/{id}/read`: Mark notification as read.
- `GET /api/personal/notifications/preferences`: Retrieve notification channel settings.

---

## 6. Future Improvements
- Web Push & Firebase Cloud Messaging (FCM) integration for mobile push delivery.
- Intelligent batching of low-priority notifications to avoid notification fatigue.

---

## Cross-References
- [Personal Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Personal API Framework](file:///D:/CampusGuide/docs/api/personal.md)
