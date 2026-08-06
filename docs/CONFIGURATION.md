# CampusGuide Configuration Specification

This document details the configuration profiles, security headers, Actuator health endpoints, CORS policies, and logging structures of the CampusGuide application.

---

## 1. Profile Configurations

CampusGuide separates configuration properties into environment-specific profiles located in the backend resources directory:

1. **`dev` Profile**: [application-dev.properties](file:///D:/CampusGuide/backend/src/main/resources/application-dev.properties)
   - Tailored for local developer setups.
   - Root logging level: `INFO`.
   - Uses default mock JWT secret.
   - Connects to local MongoDB (`localhost:27017`).
2. **`test` Profile**: [application-test.properties](file:///D:/CampusGuide/backend/src/main/resources/application-test.properties)
   - Exclusively used during unit and integration test executions.
   - Logging level restricted to `WARN` to minimize noise, with project-specific logs on `INFO`.
   - Configured with `target/uploads-test` storage to prevent test files from polluting workspace.
   - Starts a programmatic in-memory MongoDB server instance.
3. **`staging` Profile**: [application-staging.properties](file:///D:/CampusGuide/backend/src/main/resources/application-staging.properties)
   - Connects to staging MongoDB cluster.
   - Enforces SMTP config validation and JWT strength validation.
   - Enables debug diagnostics.
4. **`prod` Profile**: [application-prod.properties](file:///D:/CampusGuide/backend/src/main/resources/application-prod.properties)
   - Strict production environment.
   - All defaults for critical services (MongoDB, JWT secret, SMTP, AI API keys) are disabled; they MUST be injected via environment variables or the application will fail-fast and crash on startup.

---

## 2. Security Headers

Spring Security is configured to automatically inject security headers to protect users from modern browser-based vulnerabilities:

- **Content Security Policy (CSP)**: REST endpoints restrict scripts, styles, and data calls to trusted sources (`default-src 'self'`).
- **X-Frame-Options**: Enforced to `DENY` to prevent clickjacking attacks by blocking nested framing.
- **X-Content-Type-Options**: Explicitly configured to `nosniff` to protect against MIME-type sniffing exploits.
- **Referrer-Policy**: Configured to `no-referrer` to prevent leakage of address parameters.
- **Permissions-Policy**: Restricts geolocation, microphone, and camera access to block device enumeration.
- **HTTP Strict Transport Security (HSTS)**: Active with `includeSubDomains` and a max-age of 31,536,000 seconds (1 year) to force HTTPS.

---

## 3. CORS Policy

Cross-Origin Resource Sharing (CORS) rules are managed globally in [CorsConfig.java](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/common/config/CorsConfig.java):

- **Allowed Methods**: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- **Allowed Headers**: `*` (All request headers allowed).
- **Credentials**: Enabled (`allowCredentials = true`) to support cookie-based sessions or Authorization headers.
- **Allowed Origins**: 
  - **Dev/Staging**: Allows `http://localhost:3000` and `http://localhost:5173`.
  - **Production**: Configured via the `DATABASE_URL` / `APP_CORS_ALLOWED_ORIGINS` environment variables. **Wildcards (`*`) are strictly forbidden in production and staging profiles** and will trigger a startup check failure.

---

## 4. Health & Monitoring (Actuator)

The Spring Boot Actuator is exposed to facilitate integration with load balancers, orchestrators (e.g., Kubernetes), and telemetry agents:

### Exposed Endpoints
- `/actuator/health`: Central health status report.
- `/actuator/info`: Application name and active metadata.
- `/actuator/metrics`: JVM performance, memory utilization, and thread counts.

### Health Probes
Liveness and readiness probes are enabled under `/actuator/health/liveness` and `/actuator/health/readiness`.
- **Liveness Probe**: Confirms the JVM container is active.
- **Readiness Probe**: Checks that the MongoDB database and Atlas AI providers are responsive.

---

## 5. Logging Standard

1. **Structured Logging**: Logback/SLF4J is utilized globally. No `System.out.println` or `printStackTrace` is permitted.
2. **Exception Sanitization**: General exceptions caught by [GlobalExceptionHandler](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/common/config/GlobalExceptionHandler.java) are logged with full stack traces at `ERROR` level internally. However, clients receive a generic `An unexpected error occurred` message with status `500` to prevent leaking system information.
