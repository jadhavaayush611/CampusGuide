# Local Deployment Setup

This document covers running CampusGuide locally using standalone services or Docker containers.

---

## 1. Standalone Execution

Refer to [Development Setup](file:///D:/CampusGuide/docs/development/development-setup.md) for compiling backend Java code and running local Vite dev servers.

---

## 2. Containerized Local Execution (Docker Compose)

<!-- PLACEHOLDER: Docker -->
*Docker Compose configuration for orchestrating MongoDB, Spring Boot Backend, and FastAPI AI Gateway locally.*

```yaml
# Example Local Docker Compose Structure (Placeholder)
version: '3.8'
services:
  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
  backend:
    build: ./backend
    ports:
      - "8080:8080"
  ai-gateway:
    build: ./ai-gateway
    ports:
      - "8000:8000"
```

---

## 3. Database Configuration

<!-- PLACEHOLDER: Database Configuration -->
*Local MongoDB connection pool configuration, seed script execution, and indexing initialization.*

---

## Cross-References
- [Environment Catalog](file:///D:/CampusGuide/docs/deployment/environment.md)
- [Production Deployment](file:///D:/CampusGuide/docs/deployment/production.md)
