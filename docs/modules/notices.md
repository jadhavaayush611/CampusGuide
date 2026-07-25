# Notices Module Architecture

## 1. Purpose
The Notices module acts as an official administrative broadcast channel, allowing university management, department heads, and council leaders to communicate urgent announcements, policy updates, and campus alerts.

---

## 2. Responsibilities
- Publish official announcements with target audience scoping (e.g., specific departments, graduation years, or all campus).
- Set urgency priorities (`LOW`, `STANDARD`, `URGENT`, `CRITICAL_ALERT`).
- Enforce publisher authorization rules to ensure broadcast authenticity.

---

## 3. Entities
- `Notice`: Stores notice headline, body content, publisher ID, target audience filters, priority level, and active publication range.

---

## 4. Services
- `NoticeService`: Handles notice creation, authorization verification, target audience filtering, and dispatching notice events to the notification engine.

---

## 5. APIs
- `GET /api/notices`: Fetch active notices applicable to the authenticated student context.
- `POST /api/notices`: Publish a new official notice (Restricted to `COUNCIL_ADMIN`, `FACULTY`, `SUPER_ADMIN`).
- `DELETE /api/notices/{id}`: Archive or retract an active notice.

---

## 6. Future Improvements
- Mandatory read-receipt acknowledgement for critical safety announcements.
- Multi-lingual translation of official notices using AI Gateway services.

---

## Cross-References
- [Notifications Module](file:///D:/CampusGuide/docs/modules/notifications.md)
- [Permission Model](file:///D:/CampusGuide/docs/architecture/permission-model.md)
