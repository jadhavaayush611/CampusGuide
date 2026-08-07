# CampusGuide Production Deployment Specification

This guide describes the release architecture, deployment procedure, dependencies, startup sequence, rollback strategy, and limitations for the CampusGuide MVP platform.

---

## 1. Required Infrastructure Services

To run CampusGuide in production, the following baseline resources must be provisioned:

1. **Docker Runtime**: Docker Engine v24.0+ and Docker Compose v2.20+.
2. **MongoDB Database**: MongoDB Atlas cluster (v6.0 or v7.0 recommended) or self-hosted secure MongoDB service.
3. **AI Gateway Provider**: Access credentials for OpenAI API (`gpt-4o-mini` model capability) or OpenRouter API.
4. **SMTP Gateway**: Standard SMTP mail relay (e.g., SendGrid, Mailgun, Amazon SES).

---

## 2. Docker & Containerized Deployment (Recommended)

CampusGuide is fully containerized for production deployment.

### A. Directory Structure of Docker Assets
* [frontend/Dockerfile](file:///D:/CampusGuide/frontend/Dockerfile): Multi-stage build (Node 20 build stage -> Nginx 1.25 runtime stage).
* [backend/Dockerfile](file:///D:/CampusGuide/backend/Dockerfile): Multi-stage build (Temurin Java 25 builder -> Temurin 17 JRE runtime stage running as non-root user).
* [docker-compose.yml](file:///D:/CampusGuide/docker-compose.yml): Base container configuration for development/testing.
* [docker-compose.prod.yml](file:///D:/CampusGuide/docker-compose.prod.yml): Production overrides ensuring non-exposed ports for internal services, volume persistence, and logging configurations.

### B. Deployment via Docker Compose (Staging/Production)
1. Copy [backend/.env.example](file:///D:/CampusGuide/backend/.env.example) to `.env` in the root workspace directory.
2. Update the environment variables in the newly created `.env` file with production secrets.
3. Launch the container stack:
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
   ```
4. Verify container execution status:
   ```bash
   docker-compose ps
   ```

---

## 3. Virtual Private Server (VPS) Deployment (Manual)

If deploying directly to a Virtual Private Server (VPS) without Docker containers:

### Step A: Build & Package Backend
1. Clone the master repository and navigate to the backend folder:
   ```bash
   cd backend
   ```
2. Build the production jar using the wrapper:
   ```bash
   ./mvnw.cmd clean package -DskipTests
   ```
3. The packaged JAR will be located at `target/campusguide-1.0.0-MVP.jar`.

### Step B: Build & Bundle Frontend
1. Navigate to the frontend folder:
   ```bash
   cd ../frontend
   ```
2. Install production dependencies:
   ```bash
   npm ci
   ```
3. Compile the production assets using Vite:
   ```bash
   npm run build
   ```
4. The generated assets will be located in the `dist/` directory and are ready to be served by Nginx.

---

## 4. Nginx Reverse Proxy Setup

Nginx is used to host the static frontend assets and reverse proxy `/api` requests to the backend Spring Boot instance.

### Configurations
* **Main Configuration**: [deployment/nginx/nginx.conf](file:///D:/CampusGuide/deployment/nginx/nginx.conf)
* **Site Configuration**: [deployment/nginx/campusguide.conf](file:///D:/CampusGuide/deployment/nginx/campusguide.conf)

### Core Features Implemented:
1. **SPA Fallback Routing**: All non-file URI requests resolve to `index.html` to support client-side React Routing.
2. **API Proxying**: Requests to `/api/` are forwarded to the backend container (`http://backend:8080`).
3. **Atlas SSE Compatibility**: Server-Sent Events `/api/v1/atlas/chat/stream` are explicitly exempted from proxy buffering (`proxy_buffering off`) and caching, allowing chunked token streaming.
4. **WebSocket Compatibility**: Upgrades connections matching `$http_upgrade` seamlessly.
5. **Caching Policies**:
   * HTML index: Cache-Control: `no-store, no-cache, must-revalidate` (forces fresh client reads).
   * Static Assets (`/assets/*`): Cache-Control: `public, max-age=31536000, immutable` (leverages browser caching for hashed bundles).
6. **Security Headers**: Injects protection headers including `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, and custom Content-Security-Policy (CSP) settings.
7. **Body Size Limits**: Extended to `client_max_body_size 25M` to align with the backend's 20MB file upload limit.

---

## 5. HTTPS & SSL Certificate Guidance

In a production environment, all HTTP traffic must be redirected to HTTPS. 

### A. SSL Termination Options
* **Option 1 (Cloudflare/CDN)**: Terminate SSL at the DNS CDN level (Flexible/Full mode). Nginx container continues listening on Port 80.
* **Option 2 (Let's Encrypt / Certbot)**: Install Certbot on the host machine to obtain a free Let's Encrypt certificate.
  ```bash
  sudo apt-get install certbot python3-certbot-nginx
  sudo certbot --nginx -d campusguide.example.com
  ```

### B. Updating Nginx Site Config for SSL (Port 443)
Configure Nginx server block to use certificates:
```nginx
server {
    listen 443 ssl http2;
    server_name campusguide.example.com;

    ssl_certificate /etc/letsencrypt/live/campusguide.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/campusguide.example.com/privkey.pem;
    
    # Modern TLS configurations...
}
```

---

## 6. Database Migration & Seed Order

CampusGuide uses MongoDB. While schema changes are handled dynamically by Spring Data document models, indexes and structural collections must be loaded in the following order during cold-starts:

1. **Academic Collections**:
   * Pre-seed standard course offerings in `courses` collection.
   * Run prerequisite link validations to ensure roadmaps match course credits.
2. **Platform & Auth Roles**:
   * Create unique indexes on user `email` and `username`.
3. **AI Knowledge Store**:
   * Seed the initial campus knowledge graphs (`knowledge_catalog` collection).
   * Initialize Vector Indexes in MongoDB Atlas for semantic vector search in the Atlas advisor module.

---

## 7. Backup Recommendations

To prevent data loss, implement these regular backup policies:

1. **Database Backups (MongoDB)**:
   * **Staged / Local**: Run a daily cron job utilizing `mongodump`:
     ```bash
     mongodump --uri="mongodb://localhost:27017/campusguide" --out=/backups/mongo/$(date +%F)
     ```
   * **Production**: Enable MongoDB Atlas cloud backup policies (hourly snapshot retention).
2. **File Storage Backups**:
   * Back up the persistent uploaded resources directory (`STORAGE_LOCATION` target) daily using `tar` or synchronizing to a secure cloud bucket (e.g. AWS S3 Glacier):
     ```bash
     tar -czf /backups/uploads/resources-$(date +%F).tar.gz /app/uploads
     ```

---

## 8. Rolling Deployments & Rollback Strategy

### A. Rolling Deployment Process
To minimize downtime during production upgrades:
1. Pre-build the new Docker images on a CI build machine.
2. Run docker-compose using the new image tag:
   ```bash
   # Pull the new images
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml pull
   # Re-create containers one by one with zero downtime
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-deps backend
   # After backend is healthy, update Nginx/frontend
   docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-deps frontend
   ```

### B. Rollback Strategy
If an issue is detected during post-release verifications:
1. **Frontend Rollback**:
   * Revert the container image tag to the last stable release version and run `docker-compose up -d frontend`.
2. **Backend Rollback**:
   * Stop the failing container immediately.
   * Pull the last stable jar version or container image.
   * Restart the service container.
3. **Database Rollback**:
   * If a release included structural schema changes that broke compatibility, restore database state from the last snapshot backup taken immediately before the release window.

---

## 9. MVP Limitations

* **Local Storage Fallback**: Attached resources are uploaded to a local disk folder. In multi-instance deployments, switch to a cloud storage provider (AWS S3) by injecting S3 variables.
* **Actuator Endpoint Exposure**: Monitoring metrics are visible. Block external access to `/actuator` path in cloud firewall or private VPC settings.
* **FCM Simulated Alerts**: Browser notification relays default to local UI alerts because simulated FCM parameters are used in local configs.

