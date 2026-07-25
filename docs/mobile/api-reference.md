# Mobile API Reference

This document provides mobile developers with the standard response envelope, status code handling, and error contracts used by the backend.

---

## 1. Standard Response Envelope

All backend API responses follow a consistent payload wrapper:

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2026-07-25T17:30:00Z"
}
```

---

## 2. Standard Error Envelope

When HTTP status is `4xx` or `5xx`, the error structure is formatted as:

```json
{
  "success": false,
  "errorCode": "PREREQUISITE_NOT_MET",
  "message": "Cannot enroll in CS301. Missing prerequisite CS201.",
  "timestamp": "2026-07-25T17:30:00Z",
  "details": [
    "Required course CS201 has status: NOT_COMPLETED"
  ]
}
```

---

## 3. Common HTTP Status Codes

| Code | Status | Mobile Client Handling |
|---|---|---|
| `200` | OK | Parse response body `data` payload. |
| `201` | Created | Resource created successfully. Update local store. |
| `400` | Bad Request | Display validation error message in UI form. |
| `401` | Unauthorized | Token expired or invalid. Redirect user to Login Screen. |
| `403` | Forbidden | Insufficient user role permissions. Show permission denied toast. |
| `404` | Not Found | Resource missing. Display empty state screen. |
| `500` | Server Error | Internal failure. Show "Something went wrong" alert. |

---

## Cross-References
- [Mobile Overview](file:///D:/CampusGuide/docs/mobile/mobile-overview.md)
- [Offline Strategy](file:///D:/CampusGuide/docs/mobile/offline-strategy.md)
