# CampusGuide

CampusGuide is a centralized campus management platform designed to streamline communication, event management, academic resource sharing, and student engagement within educational institutions.

---

## Project Overview

Most higher education ecosystems currently rely on fragmented tools such as WhatsApp groups, email chains, Google Forms, and separate event portals. This results in missed announcements, low event participation, scattered academic resources, and high administrative overhead. 

CampusGuide unifies these activities into a single platform structured around four core domains: Platform, Academic, Campus, and Personal.

---

## Features

### Platform Domain
- **Authentication & User Management**: User registration, login, JWT token issuance, and Spring Security authorization.
- **Role-Based Access Control**: Granular permissions across `STUDENT`, `FACULTY`, `COUNCIL_ADMIN`, and `SUPER_ADMIN` roles.
- **Unified Global Search**: Multi-module search engine spanning courses, roadmaps, events, communities, and resources.
- **Admin Analytics**: Centralized administrative dashboard reporting platform metrics and entity activity counts.

### Academic Domain
- **Course Catalog**: Searchable repository of mandatory and elective courses with credit values and prerequisite chains.
- **Degree Roadmaps**: Structured degree pathway requirements mapped by department and target semester levels.
- **Student Progress**: Academic record tracking completed courses, accumulated credits, GPA, and graduation eligibility.
- **Semester Planner**: Interactive semester scheduling tool with automated prerequisite validation.

### Campus Domain
- **Councils Directory**: Council profiles, membership drives, and application tracking.
- **Community Forums**: Discussion channels, post feeds, threaded comments, and engagement tracking.
- **Events Center**: Campus event creation, RSVP registration, participant management, and competition results.
- **Resource Center**: Academic study material repository with notes, assignment guides, and approval workflows.

### Personal Domain
- **Notification Engine**: Event-driven in-app notifications and Firebase Cloud Messaging (FCM) alerts.
- **Personal Vault**: Encrypted file storage for personal academic documents.
- **Resume Builder**: Exportable resume generator populated from student profile data and achievements.
- **Atlas AI & Recommendations**: Strategy-based recommendation engine and AI student advisor.

---

## Tech Stack

### Backend
- **Java**: Version 25
- **Framework**: Spring Boot 4.0.6
- **Security**: Spring Security & JWT (JJWT)
- **Database**: MongoDB Atlas
- **Build Tool**: Apache Maven

### Frontend
- **Framework**: React (JSX) with Vite
- **Styling**: Tailwind CSS
- **Routing**: React Router
- **State Management**: TanStack Query & React Context API
- **HTTP Client**: Axios

### Cloud & Third-Party
- **Storage**: AWS S3
- **Push Notifications**: Firebase Cloud Messaging (FCM)

---

## Architecture

CampusGuide follows a clean 4-domain monolith architecture. For full architectural details, see [`ARCHITECTURE.md`](file:///D:/CampusGuide/ARCHITECTURE.md).

```
CampusGuide
├── backend/                  # Spring Boot 4.0.6 Application
├── frontend/                 # React + Vite Client Application
├── docs/                     # Detailed Module Documentation & Specifications
├── design/                   # UI/UX Wireframes & Design Assets
├── scripts/                  # Build & Maintenance Helper Scripts
└── .github/                  # GitHub Governance Templates & Workflows
```

---

## Screenshots

> [!NOTE]
> *UI screenshots and visual assets will be added during Batch 0.3 documentation phase.*

---

## Getting Started

### Prerequisites
- Java 25 JDK
- Apache Maven 3.9+
- Node.js 20+ & npm 10+
- MongoDB Atlas instance or local MongoDB server

### Quick Start

1. **Clone the repository**:
   ```bash
   git clone https://github.com/jadhavaayush611/CampusGuide.git
   cd CampusGuide
   ```

2. **Run Backend**:
   ```bash
   cd backend
   mvn clean spring-boot:run
   ```
   *Backend runs on `http://localhost:8080`.*

3. **Run Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *Frontend dev server runs on `http://localhost:5173`.*

---

## API Documentation

> [!NOTE]
> *Full OpenAPI specifications and Postman collections are maintained in [`docs/api-contracts.md`](file:///D:/CampusGuide/docs/api-contracts.md).*

---

## Atlas AI

> [!NOTE]
> *Atlas AI integration details and recommendation strategy configurations are documented in [`docs/ai-module.md`](file:///D:/CampusGuide/docs/ai-module.md).*

---

## Roadmap

Check our locked product roadmap in [`ROADMAP.md`](file:///D:/CampusGuide/ROADMAP.md) to view upcoming phases and features.

---

## Contributing

We welcome community contributions! Please read [`CONTRIBUTING.md`](file:///D:/CampusGuide/CONTRIBUTING.md) for guidelines on branching strategies, commit conventions, and pull request expectations. All contributors must adhere to our [`CODE_OF_CONDUCT.md`](file:///D:/CampusGuide/CODE_OF_CONDUCT.md).

---

## License

This project is licensed under the MIT License - see the [`LICENSE`](file:///D:/CampusGuide/LICENSE) file for details.