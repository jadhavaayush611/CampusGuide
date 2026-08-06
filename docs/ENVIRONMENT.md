# CampusGuide Environment Configuration Specification

This document lists all environment variables required or supported by the CampusGuide frontend and backend applications, including their profiles, scopes, default values, and security constraints.

---

## Architecture Overview

CampusGuide operates on a 4-domain monolith architecture with a decoupled React/Vite frontend and a Spring Boot 4.x backend. Configurations are loaded dynamically:
1. **Frontend**: Reads variables prefixed with `VITE_` via `import.meta.env` strictly in [env.ts](file:///D:/CampusGuide/frontend/src/core/config/env.ts).
2. **Backend**: Reads standard OS environment variables mapped inside active Spring profile files:
   - `dev` (default): [application-dev.properties](file:///D:/CampusGuide/backend/src/main/resources/application-dev.properties)
   - `test`: [application-test.properties](file:///D:/CampusGuide/backend/src/main/resources/application-test.properties)
   - `staging`: [application-staging.properties](file:///D:/CampusGuide/backend/src/main/resources/application-staging.properties)
   - `prod`: [application-prod.properties](file:///D:/CampusGuide/backend/src/main/resources/application-prod.properties)

---

## Frontend Environment Variables

All variables used by the frontend are defined below. Example file can be found in [.env.example](file:///D:/CampusGuide/frontend/.env.example).

| Variable Name | Type | Profile / Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `VITE_API_BASE_URL` | String | All | `http://localhost:8080/api/v1` | Base URL for backend REST API endpoints. |
| `VITE_APP_NAME` | String | All | `CampusGuide` | Brand name of the application displayed in UI titles. |
| `VITE_APP_VERSION` | String | All | `1.0.0` | Semantic version string displayed in diagnostics. |
| `VITE_ENABLE_DEBUG` | Boolean | Dev / Local | `false` (true in dev) | Enables verbose debug logging and client-side diagnostics. |
| `VITE_ENABLE_ANALYTICS` | Boolean | Prod / Staging | `false` | Enables telemetry and analytics tracking handlers. |

---

## Backend Environment Variables

All variables used by the backend are defined below. Example file can be found in [.env.example](file:///D:/CampusGuide/backend/.env.example).

### 1. General & Logging Settings

| Variable Name | Type | Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `PORT` | Integer | All | `8080` | Port on which the Spring Boot application listens. |
| `LOG_LEVEL` | String | All | `INFO` | Root logging level. Supported: `INFO`, `DEBUG`, `WARN`, `ERROR`. |

### 2. Security & Token Configuration

| Variable Name | Type | Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `JWT_SECRET` | String | All | *(None in prod)* | Cryptographically secure secret key for generating HMAC-SHA256 tokens. Must be >= 32 chars in production. |
| `JWT_EXPIRATION` | Long | All | `86400000` (24h) | JWT Token expiration time in milliseconds. |
| `REFRESH_TOKEN_EXPIRATION` | Long | All | `604800000` (7d) | Refresh Token validity window in milliseconds. |

### 3. Database Configuration

| Variable Name | Type | Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `DATABASE_URL` | String | All | *(None in prod)* | Connection URI for the MongoDB Atlas database instance. |
| `DATABASE_USERNAME` | String | Optional | *(None)* | Database username (if not embedded directly in the connection URI). |
| `DATABASE_PASSWORD` | String | Optional | *(None)* | Database password (if not embedded directly in the connection URI). |

### 4. Notification & SMTP Config

| Variable Name | Type | Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `MAIL_HOST` | String | Prod / Staging | *(None in prod)* | SMTP Mail Server Hostname (e.g., `smtp.mailtrap.io`). |
| `MAIL_PORT` | Integer | Prod / Staging | `2525` | SMTP Mail Server Port. |
| `MAIL_USERNAME` | String | Prod / Staging | *(None)* | SMTP Credentials Username. |
| `MAIL_PASSWORD` | String | Prod / Staging | *(None)* | SMTP Credentials Password. |

### 5. AI Gateway & OpenAI (Atlas) Config

| Variable Name | Type | Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `OPENAI_API_KEY` | String | All | *(None)* | Primary OpenAI API secret key for the Atlas subsystem. |
| `OPENROUTER_API_KEY` | String | All | *(None)* | Alternative key utilized if deploying Atlas via OpenRouter gateway. |
| `OPENAI_BASE_URL` | String | All | `https://api.openai.com/v1` | Target endpoint for OpenAI API routing. |
| `AI_GATEWAY_BASE_URL` | String | Dev / Staging | `http://localhost:8000` | Base URL of the secondary local recommendation gateway. |

### 6. Storage Configuration

| Variable Name | Type | Scope | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `STORAGE_LOCATION` | String | All | `uploads/resources` | File directory on disk for local resource attachments. |

---

## Startup Validation Guards

The application implements a strict fail-fast validation layer [StartupValidator](file:///D:/CampusGuide/backend/src/main/java/com/campusguide/common/config/StartupValidator.java) that executes checks immediately during the `@PostConstruct` phase. 

In `prod` and `staging` profiles:
1. It **terminates startup** if `DATABASE_URL` is empty.
2. It **terminates startup** if `JWT_SECRET` is missing, set to the default dev secret, or is under 32 characters in length.
3. It **terminates startup** if `MAIL_HOST` is missing or `MAIL_PORT` is <= 0.
4. It **terminates startup** if `AI_GATEWAY_BASE_URL` is configured with an invalid URL pattern.
