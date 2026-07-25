# Councils Module Architecture

## 1. Purpose
The Councils module manages student governance organizations, student body leadership, council executive positions, member rosters, and official council events.

---

## 2. Responsibilities
- Provide a directory of active student councils and executive committees.
- Manage council membership applications and recruitment workflows.
- Enable council administrators to host and manage official campus events.
- Maintain council branding, mission statements, and administrative contacts.

---

## 3. Entities
- `Council`: Represents a student council body (e.g., Student Governing Council, Engineering Council).
- `CouncilExecutive`: Maps students to specific council leadership roles (e.g., President, Vice-President, Treasurer).
- `CouncilApplication`: Represents student applications for council membership or leadership positions.

---

## 4. Services
- `CouncilService`: Manages council registration, profile updates, and executive appointments.
- `CouncilApplicationService`: Processes incoming student application submissions and approval workflows.

---

## 5. APIs
- `GET /api/councils`: List all active councils.
- `GET /api/councils/{id}`: Fetch detailed council profile and executive board.
- `POST /api/councils/{id}/apply`: Submit a membership or role application.
- `POST /api/councils`: Create a new council body (Restricted to `SUPER_ADMIN`).

---

## 6. Future Improvements
- Multi-tier sub-committee organization trees.
- Automated annual executive election and voting module.
- Council budget tracking and financial request management.

---

## Cross-References
- [Campus Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Campus API Framework](file:///D:/CampusGuide/docs/api/campus.md)
