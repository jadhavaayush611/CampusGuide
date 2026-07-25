# CampusGuide Product Roadmap

This document outlines the locked development roadmap for CampusGuide. Development progresses sequentially through phases and batches.

---

## Phase 0: Repository Infrastructure & Engineering Standards (Current Phase)

- [x] **Batch 0.1**: Domain Architecture & Engineering Audits (Refactored 4-Domain Backend Package Structure)
- [x] **Batch 0.2**: Repository Engineering Standards (Standardized Root Files, GitHub Templates, Configs)
- [ ] **Batch 0.3**: Comprehensive Repository Documentation (API Contracts, Database Schemas, System Guides)

---

## Phase 1: Core Platform & Authentication Domain

- [ ] **User Registration & Profile Management**: Student, Faculty, Council Admin, and Super Admin accounts.
- [ ] **Spring Security & JWT Authentication**: Token issuance, refresh mechanisms, and role-based access control (RBAC).
- [ ] **Faculty & Administrative Verification**: Verification workflows for elevated platform roles.

---

## Phase 2: Campus Ecosystem & Community Domain

- [ ] **Council Directory & Profiles**: Centralized directory of student councils, clubs, and societies.
- [ ] **Community Forums & Channels**: Discussion boards for academic departments, councils, and student interests.
- [ ] **Posts & Comments Engine**: Threaded discussion feeds, rich text content, and interaction metrics.

---

## Phase 3: Events & Resource Center Domain

- [ ] **Campus Event Management**: Hackathons, workshops, and seminar scheduling with registration limits.
- [ ] **Event Registration & Check-in**: RSVP workflows, ticket generation, and result publishing.
- [ ] **Academic Resource Center**: Notes, past exam papers, assignments, and study material repository.
- [ ] **Resource Approval Workflow**: Faculty and admin review workflows for uploaded materials.

---

## Phase 4: Academic Engine & Degree Planning Domain

- [ ] **Course Catalog & Search**: Mandatory and elective course listing with credit tracking and prerequisites.
- [ ] **Degree Roadmaps**: Degree pathway structures mapped by department and target semester levels.
- [ ] **Student Progress Engine**: Completed course tracking, credit accumulation, and graduation eligibility checks.
- [ ] **Semester Planner**: Dynamic semester-by-semester course scheduling with automated prerequisite validation.

---

## Phase 5: Personalization & AI Domain (Atlas AI)

- [ ] **AI Assistant Gateway**: Integration with Atlas AI gateway for student advisement and general chat.
- [ ] **Personalized Recommendation Engine**: Multi-domain strategy pattern recommending courses, events, communities, and resources.
- [ ] **Personal Document Vault**: Encrypted cloud storage for personal academic documents and certificates.
- [ ] **Automated Resume Builder**: Exportable resume generator populated from student profile and achievements.

---

## Phase 6: Production Readiness & Administrative Analytics

- [ ] **Global Platform Search**: Unified search service spanning courses, roadmaps, events, communities, and resources.
- [ ] **Admin Operational Analytics**: Aggregated dashboard metrics for Super Admins.
- [ ] **Modular Notification Engine**: Event-driven in-app notifications and Firebase Cloud Messaging (FCM) push alerts.
- [ ] **Deployment & CI/CD Pipeline**: Docker containerization, AWS infrastructure deployment, and monitoring.
