# Coding Standards & Guidelines

This document sets the code quality, architectural isolation, and formatting expectations for CampusGuide developers and AI coding agents.

---

## 1. Java / Spring Boot Standards

1. **Layered Isolation**:
   - `Controller`: Handles HTTP requests, triggers validation, and maps DTOs. Zero business logic.
   - `Service`: Encapsulates business rules, transactions, and security checks.
   - `Repository`: Pure Spring Data Mongo interfaces.
   - `DTO`: Explicit request and response models. **Entities must NEVER be exposed directly via APIs.**

2. **Error Handling**:
   - Use custom runtime exceptions extending `CampusGuideException`.
   - All REST controllers leverage `@RestControllerAdvice` for uniform JSON error responses.

3. **Immutability & Annotations**:
   - Prefer constructor injection over `@Autowired` field injection.
   - Use Lombok (`@Getter`, `@Setter`, `@Builder`) cleanly without polluting entity logic.

---

## 2. Frontend Standards (React + Vite)

1. **Vanilla CSS & Design Tokens**:
   - Use root CSS design tokens (`index.css`) for color palettes, typography, and spacing.
   - Micro-animations and glassmorphism styles preferred for high visual quality.

2. **State Management**:
   - Keep component state localized (`useState`, `useReducer`).
   - Use modular API client services (`src/services/`) for all backend REST interactions.

---

## 3. Code Review & Quality Gates

- All pull requests must pass `mvn clean verify` without compilation warnings or test failures.
- Preserve existing comments and docstrings unless explicitly refactoring the code.

---

## Cross-References
- [Commit Conventions](file:///D:/CampusGuide/docs/development/commit-conventions.md)
- [Testing Strategy](file:///D:/CampusGuide/docs/development/testing-strategy.md)
