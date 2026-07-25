# Environment Variables & Configuration Matrix

This document defines the environment variable catalog across Development, Staging, and Production environments.

---

## 1. Environment Variable Catalog

<!-- PLACEHOLDER: Environment Variables -->
*Catalog of mandatory and optional application environment variables.*

| Variable Name | Environment | Description | Default Value |
|---|---|---|---|
| `SPRING_DATA_MONGODB_URI` | All | MongoDB connection string | `mongodb://localhost:27017/campusguide` |
| `JWT_SECRET` | Staging / Prod | Secret key for signing JWTs | *Must be set via secrets manager* |
| `JWT_EXPIRATION_MS` | All | JWT lifetime in milliseconds | `86400000` (24 hours) |
| `AI_GATEWAY_BASE_URL` | All | FastAPI AI Gateway endpoint | `http://localhost:8000` |
| `AI_GATEWAY_TIMEOUT` | All | Gateway HTTP timeout | `10s` |

---

## 2. Environment Profile Matrix

| Profile | Target | Database Mode | Logging Level | AI Gateway |
|---|---|---|---|---|
| `dev` | Local Workstation | Standalone / Local Mongo | `DEBUG` | Local / Mock |
| `staging` | Staging Cluster | MongoDB Atlas Staging | `INFO` | Staging Gateway |
| `prod` | Production Cloud | Dedicated MongoDB Atlas Cluster | `WARN` / `ERROR` | High-Availability Gateway |

---

## Cross-References
- [Local Setup](file:///D:/CampusGuide/docs/deployment/local-setup.md)
- [Production Deployment](file:///D:/CampusGuide/docs/deployment/production.md)
