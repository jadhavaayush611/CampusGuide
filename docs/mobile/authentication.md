# Mobile Authentication & Token Lifecycle

This document specifies the mobile client authentication flow, token storage, refresh token handling, and biometric integration.

---

## 1. Authentication Sequence Flow

```mermaid
sequenceDiagram
    autonumber
    actor MobileUser as Mobile App User
    participant App as Mobile App Client
    participant Storage as Secure Storage (Keychain / Keystore)
    participant Backend as CampusGuide Backend API

    MobileUser->>App: Enter Credentials (Email & Password)
    App->>Backend: POST /api/auth/login
    Backend-->>App: Return JWT Token Payload & User Profile
    App->>Storage: Encrypt & Store Token in Secure Storage
    App-->>MobileUser: Transition to Home Dashboard
```

---

## 2. Secure Token Storage

- **iOS**: Save JWT token in iOS **Keychain Services** via `SecItemAdd`.
- **Android**: Save JWT token in Android **EncryptedSharedPreferences** / Keystore.
- **Cross-Platform**: Never store authentication tokens in unencrypted AsyncStorage or plain SQLite tables.

---

## 3. Biometric Authentication Integration

Mobile clients may wrap token retrieval with local biometric authentication (FaceID / TouchID / Fingerprint) to unlock local app sessions without requiring re-entry of password credentials.

---

## Cross-References
- [Mobile API Reference](file:///D:/CampusGuide/docs/mobile/api-reference.md)
- [Backend Authentication API](file:///D:/CampusGuide/docs/api/authentication.md)
