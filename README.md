# CampusGuide

CampusGuide is a centralized campus management platform designed to streamline communication, event management, academic resource sharing, and student engagement within educational institutions.

The platform provides a single ecosystem where students, faculty members, councils, and administrators can interact, share resources, manage events, and stay informed through a unified interface.

---

# Problem Statement

Most colleges currently rely on fragmented systems such as:

* WhatsApp Groups
* Email Chains
* Google Forms
* Shared Drives
* Separate Event Portals

This often results in:

* Missed announcements
* Low event participation
* Scattered academic resources
* Poor communication
* Administrative overhead

CampusGuide solves these issues by centralizing campus activities into a single platform.

---

# Project Goals

* Reduce missed announcements by 70%
* Increase event participation by 40%
* Centralize academic resources
* Improve communication efficiency
* Simplify event management
* Improve student engagement

---

# Core Features

## Authentication & User Management

* User Registration
* User Login
* JWT Authentication
* Role-Based Access Control
* Faculty Verification
* Premium Membership Support

---

## Councils & Communities

* Council Profiles
* Council Membership Applications
* Community Discussions
* Posts & Comments
* Activity Feeds

---

## Events Management

* Event Creation
* Event Registration
* Event Tracking
* Event Results Publishing
* Participation Management
* Membership Drive Support

---

## Resource Center

* Notes Repository
* Assignments Repository
* Study Material Sharing

---

## Academic Planning & Progress

* Academic Roadmaps: Degree pathway structures defined by degree program and department.
* Course Catalog: Fully searchable catalog of mandatory and elective courses with credit tracking, prerequisite mapping, and target semester levels.
* Student Progress: Track completed courses, credits earned, current GPA, and graduation eligibility.
* Semester Planning: Build, update, modify, and finalize semester-by-semester planned courses with automatic prerequisite verification.
* Academic Dashboard: Aggregated progress visualizer detailing graduation eligibility, completion percentage, remaining credits, and plans.
* Course Recommendations: Automated semester recommendations suggesting courses based on department, target semester, and completed prerequisites with validation warnings.

---

## Notifications

* Event Notifications
* Resource Updates
* Membership Updates
* Reminder Notifications

---

## Premium Features

* Personal Vault Storage
* Resume Builder
* Custom AI Roadmaps (Planned / Future Phase)
* Additional Storage Capacity

---

# User Roles

## Student

Can:

* Join communities
* Register for events
* Upload and download academic resources
* Apply for council memberships
* Build resumes
* Store personal documents
* View academic roadmaps and browse courses
* Manage personal student progress and completed courses
* Build, modify, and finalize semester plans
* Access academic dashboard and receive recommended courses

---

## Faculty

Can:

* Upload resources
* Download academic resources
* Manage academic content
* Track event participation

---

## Council Admin

Can:

* Create and manage events
* Review membership applications
* Publish event results
* Manage council content

---

## Super Admin

Can:

* Manage platform settings
* Manage users
* Manage councils
* Moderate content
* Access administrative tools
* Manage full course catalog (create, update, delete courses)
* Manage roadmaps (create, update, delete roadmaps)
* View/manage all student progress records and semester plans

---

# Technology Stack

## Backend

* Java 25
* Spring Boot 4.0.6
* Spring Security
* JWT Authentication
* MongoDB Atlas
* Maven

---

## Frontend

* React
* Vite
* JavaScript (JSX)
* Tailwind CSS
* React Router
* Axios
* TanStack Query

---

## Cloud Services

* AWS S3 (File Storage)
* Firebase Cloud Messaging (Notifications)

---

## Development Tools

* Git
* GitHub
* Postman
* IntelliJ IDEA
* VS Code

---

# Project Architecture

Backend follows a layered architecture:

Controller
→ Service
→ Repository
→ Database

Frontend follows a component-based architecture:

Pages
→ Layouts
→ Components
→ Services
→ APIs

---

# Repository Structure

```text
CampusGuide

├── backend
│   └── Spring Boot Application
│
├── frontend
│   └── React Application
│
├── docs
│   ├── api-contracts
│   ├── db-schema
│   ├── permission-matrix
│   ├── frontend-routes
│   ├── deployment
│   └── testing
│
├── INSTRUCTIONS.md
├── PROJECT_VISION.md
├── BACKEND_ARCHITECTURE.md
├── FRONTEND_ARCHITECTURE.md
├── TESTING_STRATEGY.md
│
└── README.md
```

---

# Development Workflow

Branches:

```text
main
develop
```

Rules:

* All development occurs on develop
* main contains stable milestone releases
* Every feature must compile before commit
* Documentation must remain updated

---

# Running Backend

Navigate to backend:

```bash
cd backend
```

Compile:

```bash
mvn clean compile
```

Run:

```bash
mvn spring-boot:run
```

Default:

```text
http://localhost:8080
```

---

# Running Frontend

Navigate to frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start development server:

```bash
npm run dev
```

Default:

```text
http://localhost:5173
```

---

# Environment Variables

Required variables:

```text
JWT_SECRET

MONGODB_URI

AWS_ACCESS_KEY

AWS_SECRET_KEY

FIREBASE_PROJECT_ID
```

These should never be committed to source control.

---

# Testing

Backend:

```bash
mvn clean compile
```

Frontend:

```bash
npm run build
```

Authentication Testing:

* Register
* Login
* JWT Validation
* Protected Endpoints

---

# MVP Roadmap

Week 1

* Authentication

Week 2

* Councils & Communities

Week 3

* Events System

Week 4

* Resource Center

Week 5

* Integrations & Notifications

Week 6

* Premium Features, Academic Planning (Roadmaps, Courses, Student Progress, Semester Plans, Dashboard, Recommendations) & Deployment

---

# Future Scope

* Native Mobile Application
* AI Advisor & AI Recommendation Engine (Planned / Future Phase)
* Advanced Curriculum Planning (Planned / Future Phase)
* Placement Tracking
* Internship Portal
* Academic Analytics
* Campus Marketplace

---

# License

This project is currently developed as an academic and portfolio project by the CampusGuide development team.

All rights reserved.