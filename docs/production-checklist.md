# CampusGuide Production Deployment Checklist

This operational checklist is used to verify that the CampusGuide application is fully configured, secured, and ready for deployment to a production environment.

---

## 1. Frontend Audit
- [ ] Production build (`npm run build`) compiles successfully without errors.
- [ ] TS typechecks (`npm run typecheck`) complete with zero errors.
- [ ] Obsolete developer logs and `console.log()` statements are removed or guarded behind debug environment configurations.
- [ ] No hardcoded localhost addresses are present in the source files.
- [ ] Environment config reads variables strictly from a central module ([env.ts](file:///D:/CampusGuide/frontend/src/core/config/env.ts)).

---

## 2. Backend Audit
- [ ] Build and verify checks (`mvn clean verify`) complete with zero test failures.
- [ ] Startup validator runs and performs fail-fast verification on profiles, database links, and secret parameters.
- [ ] No raw DB entities are exposed directly to API controllers.
- [ ] Spring Security method authorization (`@PreAuthorize`) is active on sensitive controllers.
- [ ] No raw print statements (`System.out.println`) are used in the service classes.

---

## 3. Authentication & Security
- [ ] JWT Signature algorithm uses a robust HMAC-SHA256 key matching the minimum size.
- [ ] `JWT_SECRET` key is rotated and set via a secure vault (e.g. AWS Secrets Manager) in production.
- [ ] CORS allowed origins explicitly configured with no wildcards (`*`) allowed when credentials are active.
- [ ] Content Security Policy (CSP) is active on API routes.
- [ ] Custom security headers are active:
  - `X-Frame-Options: DENY`
  - `X-Content-Type-Options: nosniff`
  - `Referrer-Policy: no-referrer`
  - `Permissions-Policy: geolocation=(), microphone=(), camera=()`
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains`

---

## 4. Database (MongoDB)
- [ ] MongoDB Atlas connection string uses connection pooling and SSL/TLS.
- [ ] Write concern is set to `majority` for critical writes.
- [ ] Database credentials (username and password) are injected via env parameters.
- [ ] Appropriate compound indexes are verified and created on collections.

---

## 5. Storage & Vault
- [ ] Storage location (`STORAGE_LOCATION`) points to a persistent directory with correct read/write permissions.
- [ ] Maximum file size limits are verified on the Spring servlet config (default: 20MB).

---

## 6. Atlas AI Subsystem
- [ ] Primary OpenAI or OpenRouter API key is validated and active.
- [ ] Startup validator confirms the context engines and persona generators are fully loaded.
- [ ] Prompt tokens budget matches the target limits of the model (`gpt-4o-mini`).
- [ ] Circuit Breaker, rate limit, and timeout parameters are enabled and tested.

---

## 7. Email & Notifications
- [ ] SMTP server configurations are validated for the environment.
- [ ] SMTP host and ports match secure TLS connection configs (e.g. port 465 or 587).
- [ ] Failed email alerts do not block core transaction workflows.

---

## 8. Logging & Monitoring
- [ ] Logging utilizes SLF4J loggers.
- [ ] Exceptions are logged with stack traces internally at `ERROR` level.
- [ ] API error payloads sanitize stack traces and return clean messages to clients.
- [ ] Actuator endpoints `/health`, `/info`, and `/metrics` are exposed.
- [ ] Actuator liveness/readiness probes are verified.

---

## 9. Performance & SEO
- [ ] favicon and OpenGraph/Twitter Card metadata tags are verified in `index.html`.
- [ ] Manifest file matches PWA installation specs.
- [ ] robots.txt is present and configured.
- [ ] Vite code splitting separates bundle dependencies into individual vendor chunks.
