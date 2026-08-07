# CampusGuide Production Deployment Specification

This guide describes the release architecture, deployment procedure, dependencies, startup sequence, rollback strategy, and limitations for the CampusGuide MVP platform.

---

## 1. Required Infrastructure Services

To run CampusGuide in production, the following baseline resources must be provisioned:

1. **Java Runtime**: JDK 25.
2. **Node.js**: Version 20+ (with npm 10+).
3. **MongoDB Cluster**: MongoDB Atlas (v6.0 or v7.0 recommended).
4. **AI Gateway Provider**: Access keys for OpenAI API (`gpt-4o-mini` model capability) or OpenRouter API.
5. **SMTP Gateway**: Standard SMTP mail relay (e.g., SendGrid, Mailgun, Amazon SES).

---

## 2. Deployment Procedure (Step-by-Step)

Follow this process for a standard clean production deployment:

### Step A: Build & Package Backend
1. Clone the master repository and navigate to the backend folder:
   ```bash
   cd backend
   ```
2. Build the production jar using the wrapper:
   ```bash
   ./mvnw clean package -DskipTests
   ```
   *(Note: Skip tests during packaging only if they have already been verified locally or via CI/CD pipelines to speed up build cycles).*
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
4. The generated assets will be located in the `dist/` directory and are ready to be served by a CDN, Nginx webserver, or Spring Boot static resources mapping.

### Step C: Configure Environment Variables
Prepare the container orchestration configurations (e.g. Docker Compose, Kubernetes manifests, or systemd services) with the appropriate environment variables. Review the [Environment Specification](file:///D:/CampusGuide/docs/ENVIRONMENT.md) for a complete list of required values.

---

## 3. Database Migration Order

CampusGuide uses MongoDB, a document database. While schema migrations are flexible (handled dynamically by spring-data-mongodb document mapping), the indexes and structural collections must be loaded in the following order:

1. **Academic Collections**:
   - Save the default courses list to the `courses` collection.
   - Run prerequisite link validations to ensure roadmaps match course credits.
2. **Platform & Auth Roles**:
   - Pre-populate user roles index on the `users` collection to prevent duplicates.
   - Create compound index on `email` and `username`.
3. **AI Knowledge Store**:
   - Seed the initial campus knowledge graphs (`knowledge_catalog` collection).
   - Ensure the vector embeddings indexes are initialized for semantic retrieval.

---

## 4. Startup Sequence

Services must be launched in this order to prevent connection failures:

1. **Launch MongoDB**: Verify that the MongoDB instance is online and reachable from the application host.
2. **Launch AI Gateway (Optional)**: If running a secondary Python recommendations server, launch it first.
3. **Launch Backend Service**: Run the Spring Boot application jar:
   ```bash
   java -jar campusguide-1.0.0-MVP.jar --spring.profiles.active=prod
   ```
   Ensure the application console outputs `Startup Configuration Validation complete` with no exceptions.
4. **Deploy Frontend Web Server**: Spin up the static web hosting for the React `dist/` bundle pointing to the Backend URL.

---

## 5. Rollback Procedure

If a failure is detected in production (e.g., crash loops, API breaks), execute these recovery steps:

1. **Frontend Rollback**:
   - Re-deploy the last verified build directory (`dist/` bundle) to the CDN or static host.
2. **Backend Rollback**:
   - Terminate the active failing application process.
   - Pull the previous stable JAR package release.
   - Restart the process using the previous JAR package.
3. **Database Restore**:
   - If migrations made breaking database schema adjustments, restore the database from the last automated snapshot (e.g., MongoDB Atlas point-in-time recovery) taken immediately prior to deployment.

---

## 6. MVP Limitations

The MVP release of CampusGuide contains several structural constraints that should be resolved in subsequent iterations:

- **Local Storage Limitations**: Attached resources are uploaded to a local disk folder. In a scaled or multi-instance deployment, this must be switched to a cloud storage provider (like Amazon S3).
- **Embedded Database for Tests**: The test suite spins up an ephemeral database that takes around 30 seconds to extract during first compilation; subsequent test runs utilize caching.
- **Actuator Endpoint Exposure**: Probes are exposed openly. They must be secured behind admin roles or restricted to internal VPC subnets in network configurations.
- **FCM Simulated Alerts**: In-app notifications are stored inside the database, but Firebase Cloud Messaging client integrations rely on simulated parameters.
