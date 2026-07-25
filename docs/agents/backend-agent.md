# Backend Agent Operational Guide

This document defines operational rules, architectural boundaries, and code quality expectations for AI coding agents modifying backend Java / Spring Boot code in CampusGuide.

---

## 1. Project Vision
CampusGuide is a multi-domain modular monolithic platform powered by AI. The backend provides high-performance, secure, and domain-isolated REST APIs for web and mobile clients.

---

## 2. Architecture
- **Framework**: Java 25 / Spring Boot 4.0.6.
- **Data Store**: MongoDB Atlas (Spring Data MongoDB).
- **Domains**: 4-domain monolith (`com.campusguide.platform`, `com.campusguide.academic`, `com.campusguide.campus`, `com.campusguide.personal`).

---

## 3. Responsibilities
- Implement REST API endpoints, DTO mappers, and domain service logic.
- Enforce Spring Security role authorization (`@PreAuthorize`) and JWT filtering.
- Maintain unit and integration tests (`*Test.java` and `*IT.java`).

---

## 4. Coding Standards
- Zero business logic in `@RestController` classes. Business logic belongs exclusively in `@Service` implementations.
- Prefer constructor injection over field injection.
- DTOs are mandatory for all API contracts. **Entities must NEVER be exposed directly in API requests/responses.**

---

## 5. Naming Conventions
- Packages: Lowercase domain-first naming (`com.campusguide.academic.planner.service`).
- Interfaces & Impls: `PlannerService` interface with `PlannerServiceImpl` implementation.
- DTOs: `CourseResponse`, `SemesterPlanRequest`.

---

## 6. What NOT to Do

> [!CAUTION]
> **CRITICAL INVARIANTS**:
> - **Calendar owns no data**: Never create a MongoDB collection for Calendar entities.
> - **Atlas never mutates data directly**: AI assistants provide suggestions; write operations must pass through user-approved domain APIs.
> - **Business logic belongs in services**: Never put validation or calculation logic in controllers or repositories.
> - **Councils and Communities are separate concepts**: Do not merge council administration logic with community social forums.
> - **Shared resources should not be duplicated**: Reference courses and users by ID across domains instead of duplicating entity records.

---

## 7. Development Workflow
1. Read relevant module and API specs in `docs/` before making changes.
2. Implement atomic changes with corresponding unit/integration tests.
3. Run `mvn clean verify` in `backend/` to validate compilation and tests.
4. Update relevant documentation in `docs/` before declaring task completion.

---

## 8. Expected Output Quality
- Production-grade code with complete error handling and logging.
- No unhandled null pointer exceptions or swallow-exception blocks.
- Clean pass on all automated build gates.

---

## Cross-References
- [Domain Architecture](file:///D:/CampusGuide/docs/architecture/domain-architecture.md)
- [Coding Standards](file:///D:/CampusGuide/docs/development/coding-standards.md)
