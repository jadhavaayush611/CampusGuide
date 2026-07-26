# Local Development Setup Guide

## Prerequisites

Ensure your workstation has the following tools installed:
- **Java Development Kit (JDK)**: Java 25 or compatible LTS (21+)
- **Build Tool**: Apache Maven 3.9+
- **Node.js Environment**: Node.js v20+ and `npm` / `pnpm`
- **Database**: Local MongoDB 7.0+ or a MongoDB Atlas connection string
- **Git**: 2.40+

---

## 1. Environment Configuration

1. Clone the repository:
   ```bash
   git clone https://github.com/jadhavaayush611/CampusGuide.git
   cd CampusGuide
   ```

2. Configure backend environment properties:
   Copy default properties in `backend/src/main/resources/application.properties` or set environment variables:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/campusguide
   jwt.secret=YourSuperSecretKeyWithMinimum256BitsLengthHere
   jwt.expiration-ms=86400000
   ai.gateway.base-url=http://localhost:8000
   ```

---

## 2. Building & Running the Backend

1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```

2. Compile and run unit tests:
   ```bash
   mvn clean verify
   ```

3. Launch the Spring Boot development server:
   ```bash
   mvn spring-boot:run
   ```
   The backend API runs at `http://localhost:8080`.

---

## 3. Running the AI Gateway (FastAPI)

1. Navigate to the `ai-gateway/` directory (if configured locally):
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   uvicorn main:app --reload --port 8000
   ```

---

## 4. Running the Frontend Client

1. Navigate to `frontend/`:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The Vite dev server launches at `http://localhost:5173`.

---

## Cross-References
- [Coding Standards](./code-style.md)
- [Testing Strategy](./testing-strategy.md)
