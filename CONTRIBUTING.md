# Contributing to CampusGuide

Thank you for your interest in contributing to CampusGuide! This document outlines our development workflow, coding standards, and guidelines to ensure high code quality and smooth collaboration.

---

## 1. Branch Strategy

We follow a structured Git branching strategy:

- `main`: Production-ready releases. Direct pushes are restricted.
- `develop`: Integration branch for ongoing development. All feature branches branch off from and merge back into `develop`.
- `feature/<feature-name>`: Dedicated branches for developing new features.
- `fix/<issue-name>`: Dedicated branches for resolving bugs or security issues.
- `docs/<topic>`: Branches reserved for documentation updates.

### Workflow
1. Fork or clone the repository.
2. Checkout the latest `develop` branch: `git checkout develop && git pull origin develop`.
3. Create a feature branch: `git checkout -b feature/academic-roadmap`.
4. Commit changes following our commit conventions.
5. Push your branch and open a Pull Request against `develop`.

---

## 2. Commit Conventions

We strictly follow the **Conventional Commits** specification (`<type>(<scope>): <short summary>`).

### Allowed Types
- `feat`: A new feature for the user or platform.
- `fix`: A bug fix.
- `docs`: Documentation changes only.
- `style`: Changes that do not affect code logic (formatting, missing semi-colons, whitespace).
- `refactor`: Code restructuring without adding features or fixing bugs.
- `test`: Adding missing tests or refactoring existing tests.
- `chore`: Updating build tasks, dependencies, package configurations, or scripts.

### Examples
- `feat(academic): implement semester planner prerequisite validation`
- `fix(auth): resolve JWT expiration token parsing bug`
- `docs(api): update OpenAPI specifications for events endpoint`
- `chore(deps): bump Spring Boot version to 4.0.6`

---

## 3. Pull Request Expectations

Before opening a Pull Request (PR):
1. **Verification**: Verify that backend and frontend builds pass locally:
   - Backend: `cd backend && mvn clean verify`
   - Frontend: `cd frontend && npm run build`
2. **PR Description**: Include a clear description of changes, motivation, and references to resolved issues.
3. **Review Process**: Every PR requires at least one code review and approval before merging into `develop`.
4. **Clean Commits**: Rebase onto `develop` and squash unnecessary commits if needed.

---

## 4. Coding Standards

### Backend (Java / Spring Boot)
- **Java Version**: Java 25.
- **Framework**: Spring Boot 4.0.6.
- **Layered Architecture**:
  - `Controller`: Thin controllers for handling requests, validating DTOs, and mapping HTTP statuses. No business logic in controllers.
  - `Service`: Business logic, authorization rules, and transaction boundaries.
  - `Repository`: Pure database access via Spring Data MongoDB.
  - `DTO`: Data Transfer Objects for API contracts. **Never expose Entities directly via REST endpoints.**
- **Formatting**: Standard 4-space indentation, UTF-8 encoding, LF line endings.
- **Exception Handling**: Use centralized `@RestControllerAdvice` exception handlers. Never swallow exceptions.

### Frontend (React / Vite)
- **Framework**: React with Vite and Tailwind CSS.
- **Component Design**: Modular, reusable components under `src/components/`. Keep page components concise.
- **API Requests**: Route all API communication through service abstractions in `src/services/` using Axios and TanStack Query.
- **Formatting**: Standard 2-space indentation, UTF-8 encoding, LF line endings.

---

## 5. Reporting Issues

If you encounter bugs or have feature requests:
- Check existing GitHub Issues before creating a new one.
- Use our standard issue templates (`bug_report.md`, `feature_request.md`, `documentation.md`).
