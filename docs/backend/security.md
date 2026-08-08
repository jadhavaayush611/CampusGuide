# CampusGuide Backend Security Architecture & Hardening Guide

This document provides a comprehensive overview of the security architecture, controls, audit findings, and production-hardening measures implemented across the CampusGuide backend.

---

## 1. Authentication Flow

CampusGuide uses a stateless, token-based authentication mechanism powered by **Spring Security** and **JSON Web Tokens (JWT)**.

1. **Credentials Submission**: Clients submit their credentials via the `POST /api/v1/auth/login` or `POST /api/v1/auth/register` endpoints.
2. **Verification & Issuance**: 
   - Usernames/emails and passwords are verified using `BCrypt` password hashing.
   - Upon successful verification, the backend generates a signed JWT.
3. **Token Propagation**: The client must store this token securely and send it in the `Authorization` header of all subsequent API requests:
   ```http
   Authorization: Bearer <token>
   ```
4. **Token Authentication Filter**: The `JwtAuthenticationFilter` intercepts each request, extracts the JWT, verifies its signature and expiration, retrieves the user profile, and populates the `SecurityContextHolder`.

---

## 2. Authorization Model

Authorization in CampusGuide is established at two layers:

### A. Endpoint Filter Rules (`SecurityConfig`)
HTTP requests are filtered early by Spring Security's filter chain:
- **Public Endpoints** (Permitted unconditionally):
  - `/api/v1/auth/register`
  - `/api/v1/auth/login`
  - Swagger UI / OpenAPI docs (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`)
  - Platform/Atlas health and lifecycle probes (`/error`, `/api/v1/atlas/health`, `/api/v1/atlas/ready`, `/api/v1/atlas/live`)
- **Protected Endpoints** (Require authentication):
  - Any request not matching the public endpoints must provide a valid JWT (`.anyRequest().authenticated()`).

### B. Method-Level Security (`@PreAuthorize`)
Fine-grained access control is enforced on individual service methods and controller endpoints using `@PreAuthorize("isAuthenticated()")` or role-based checks.

**User Roles**:
- `STUDENT`: General student access to campus resources, roadmaps, and personal planner.
- `FACULTY`: Faculty access for publishing notices or managing course info.
- `COUNCIL_ADMIN`: Admin access for student council posts, notice publications, and event management.
- `SUPER_ADMIN`: Full system administrative capability.

---

## 3. JWT Lifecycle & Security

The JWT lifecycle is audited and hardened against standard token attacks:

- **Signing Algorithm**: Tokens are signed using the HMAC-SHA algorithm via the `jjwt` library.
- **Secret Handling**: JWT secrets are never hardcoded. They are loaded dynamically using the `jwt.secret` configuration property. If in production, this environment variable (`JWT_SECRET`) must be injected. A startup validator (`StartupValidator`) ensures the application halts if a weak or default key is used in production.
- **Expiration Validation**: The JWT payload contains an expiration date (`exp` claim). Expired tokens are rejected automatically by the parser, throwing `ExpiredJwtException`.
- **Clock Skew Tolerance**: The parser is configured with a **60-second clock skew tolerance** (`.clockSkewSeconds(60)`) to accommodate minor clock drift across distributed servers without triggering false validation failures.
- **Malformed & Invalid Token Rejection**: Any exception thrown due to validation failures (e.g. `MalformedJwtException`, `SignatureException`) is caught in the filter chain, which logs a sanitized warning and drops the request security context, forcing a `401 Unauthorized` response.

---

## 4. Password Security

Passwords represent critical credentials and are protected through defense-in-depth:

- **BCrypt Hashing**: All passwords are encrypted using `BCryptPasswordEncoder` with a secure cost factor of **12** (`new BCryptPasswordEncoder(12)`). This provides high resistance to offline brute-force and hardware acceleration attacks while maintaining acceptable server-side validation latency.
- **Strength Validation**: The `@PasswordStrength` custom validator enforces password requirements:
  - Minimum length of **8 characters**.
  - At least **one uppercase letter**, **one lowercase letter**, **one digit**, and **one special character**.
- **No Password Exposure**: 
  - Password fields are omitted from standard logs.
  - The request DTOs (`RegisterRequest` and `LoginRequest`) do not declare Lombok `@ToString` or `@Data` annotations containing password fields.
  - Hashed passwords are never mapped or returned in any DTO response.

---

## 5. File Upload Security

File uploads via `POST /api/v1/resources` are audited and restricted using physical storage isolation and rigorous path/content validation:

- **Upload Limit**: Restricted to a maximum of **20MB** via servlet multipart request configurations.
- **Strict Content & Extension Mapping**: The application maintains a strict map of allowed MIME types to extensions to prevent header spoofing:
  ```java
  "application/pdf" -> ".pdf"
  "application/msword" -> ".doc"
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx"
  "application/vnd.ms-excel" -> ".xls"
  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> ".xlsx"
  "application/vnd.ms-powerpoint" -> ".ppt"
  "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> ".pptx"
  "image/jpeg" -> ".jpg", ".jpeg"
  "image/png" -> ".png"
  ```
  Any file matching an unmapped MIME type or possessing an extension that deviates from its claimed content type is immediately rejected.
- **Filename Sanitization & Randomization**: Clients cannot choose the stored file's name. Physical file storage writes files using a random UUID (`UUID.randomUUID().toString() + extension`), preventing filename injection or executable overwrites.
- **Directory Traversal Prevention**: File lookup and storage paths resolve relative to the root storage directory and enforce that the parent path equals the root storage directory path:
  ```java
  if (!destinationFile.getParent().equals(this.rootLocation)) {
      throw new SecurityException("Cannot store file outside current directory.");
  }
  ```
- **Executable Upload Prevention**: The application explicitly blocks files lacking an extension, containing relative path components (`..`), or matching executable extensions (like `.jsp`, `.exe`, `.sh`).

---

## 6. CORS Policy

The Cross-Origin Resource Sharing (CORS) policy is audited and locked down:

- **Allowed Origins**: Allowed origins are explicitly defined via `app.cors.allowed-origins`. 
- **Production Restrictions**: If the application runs with the `prod` or `production` profile, **wildcard origins (`*`) are strictly forbidden**. The application will throw an `IllegalStateException` on startup/configuration if a wildcard origin is loaded in production.
- **Credentials Support**: `allowCredentials` is enabled, permitting secure transport of authentication headers and cookies only between explicit, trusted domains.

---

## 7. Rate Limiting

Rate limiting is implemented at two distinct levels:

### A. Authentication Endpoints (IP-Based)
A lightweight servlet filter `AuthRateLimitingFilter` applies IP-based rate limiting to the public endpoints `/api/v1/auth/login` and `/api/v1/auth/register`.
- Leverages a custom Token Bucket rate limiter (`AuthRateLimiter`) with concurrent maps.
- **Default limits**: 10 requests per minute with a bucket capacity of 15 tokens.
- Fully configurable in `application.properties`.
- Rate-limited requests are blocked immediately and return a `429 Too Many Requests` status code.

### B. Atlas AI Endpoints (User/Session-Based)
The Atlas AI subsystem leverages an `InMemoryRateLimitPolicy` component to restrict API usage.
- Limits concurrent and per-minute executions on a per-user basis.
- **Default limits**: 60 requests per minute.
- Returns a standardized `429 Too Many Requests` error response on limit exhaustion.

---

## 8. HTTP Security Headers

Spring Security headers are configured to prevent standard client-side attacks:

- **Content Security Policy (CSP)**: Locks down resources to the origin, disallowing external code loading:
  ```http
  Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'
  ```
- **HTTP Strict Transport Security (HSTS)**: Enforces SSL transport for 1 year (31,536,000 seconds) including all subdomains:
  ```http
  Strict-Transport-Security: max-age=31536000 ; includeSubDomains
  ```
- **Clickjacking Protection**: Disallows framing the application:
  ```http
  X-Frame-Options: DENY
  ```
- **MIME Sniffing Prevention**: Enforces browser adherence to content type headers:
  ```http
  X-Content-Type-Options: nosniff
  ```
- **Referrer Policy**: Prevents leaking sensitive URI components in request referrers:
  ```http
  Referrer-Policy: no-referrer
  ```
- **Permissions Policy**: Disables browser device permissions (camera, microphone, location) unless explicitly needed:
  ```http
  Permissions-Policy: geolocation=(), microphone=(), camera=()
  ```

---

## 9. Secure Coding & Error Handling

- **NoSQL Injection Prevention**: All queries in Spring Data repositories are parameterized. Dynamic user-supplied string manipulation inside Mongo queries is forbidden.
- **Query Length Limitation**: Search queries submitted to the global search engine (`SearchServiceImpl`) are strictly validated to not exceed **255 characters**, preventing regex denial-of-service (ReDoS) or thread starvation.
- **Sanitized Exception Handling**:
  - Global error responses handle database (`org.springframework.dao.DataAccessException`), Mongo, JSON parsing (`HttpMessageNotReadableException`), and type mismatches (`MethodArgumentTypeMismatchException`).
  - Stack traces, class names, file system paths, and internal exceptions are logged on the server but are **never** exposed in API responses. Responses return sanitized, developer-friendly messages (e.g. `"An unexpected error occurred"`, `"Malformed JSON request or invalid field format"`, `"Invalid parameter value or format"`).
