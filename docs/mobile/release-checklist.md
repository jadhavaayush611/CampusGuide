# Mobile App Store Release Checklist

This document details pre-release requirements for publishing CampusGuide to the Apple App Store and Google Play Store.

---

## 1. Pre-Release Verification

- [ ] All API endpoint URLs point to production gateway instances (`https://api.campusguide.io`).
- [ ] Debug logging levels disabled (`WARN` / `ERROR` only).
- [ ] Offline caching and database migration scripts tested against previous store builds.
- [ ] Push notification APNs / FCM certificates valid and loaded in production environment.

---

## 2. Store Submission Assets

- [ ] iOS App Store screenshots (6.5", 5.5", and iPad Pro display sizes).
- [ ] Google Play Store feature graphic and screenshot set.
- [ ] Privacy Policy URL and Terms of Service links verified.
- [ ] App Store privacy labels configured for identity data and device identifiers.

---

## Cross-References
- [Mobile Overview](file:///D:/CampusGuide/docs/mobile/mobile-overview.md)
- [Production Deployment](file:///D:/CampusGuide/docs/deployment/production.md)
