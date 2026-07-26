# Code Style Guide

This document summarizes the coding standards, style guidelines, and design conventions for the CampusGuide codebase. For editor-level formatting rules (indentation, line endings, character set), refer to [.editorconfig](../../.editorconfig).

---

## 1. Naming Conventions

- **Packages / Directories**: Lowercase, dot-separated for Java (`com.campusguide.academic.planner`), lowercase kebab-case or camelCase for frontend files.
- **Classes / Interfaces**: PascalCase (`SemesterPlannerService`, `CouncilRepository`).
- **Methods / Variables**: camelCase (`calculateCredits`, `userEmail`).
- **Constants**: UPPER_SNAKE_CASE (`MAX_CREDITS_PER_SEMESTER`).
- **DTOs & Exceptions**: Must carry explicit suffixes (`RegisterUserRequest`, `CourseNotFoundException`).

---

## 2. Package Organization

Organize backend code cleanly by domain module:
```text
com.campusguide.<domain>.<feature>/
  ├── controller/
  ├── dto/
  ├── entity/
  ├── repository/
  └── service/
```

Frontend files should follow standard React component folder organization:
```text
frontend/src/
  ├── components/
  ├── services/
  └── styles/
```

---

## 3. Dependency Injection & Immutability

- Use constructor injection exclusively for spring components.
- Declare dependencies as `private final` fields.
- Prefer immutable data structures (Java `record` types or final fields) for DTOs and value objects.

---

## 4. Exception Handling

- Throw domain-specific runtime exceptions extending `CampusGuideException`.
- Never catch generic `Exception` or swallow exceptions with empty `catch` blocks.
- All REST controllers leverage centralized `@RestControllerAdvice` exception handlers to produce standardized JSON error responses.

---

## 5. Validation Strategy

- Use Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`) on DTO fields.
- Annotate controller payload parameters with `@Valid`.
- Perform business rule validations explicitly inside service layers.

---

## 6. Logging Guidelines

- Use `SLF4J` via Lombok's `@Slf4j` annotation.
- Log at appropriate severity levels:
  - `DEBUG`: Detailed troubleshooting data.
  - `INFO`: Significant lifecycle events and workflow milestones.
  - `WARN`: Recoverable errors or non-blocking issues.
  - `ERROR`: System failures, unexpected exceptions, or security alerts.
- Never log sensitive information (passwords, JWT secrets, PII).

---

## Cross-References
- [.editorconfig](../../.editorconfig)
- [Architecture Principles](./architecture-principles.md)
- [Git Workflow](./git-workflow.md)
