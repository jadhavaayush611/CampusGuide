# Architecture Principles

This document defines the architectural guidelines and design principles for CampusGuide. All backend and frontend code contribution must comply with these core principles.

---

## 1. Clean Architecture & Layered Boundaries

CampusGuide strictly separates concerns across explicit domain boundaries and software layers:

- **Domain Boundaries**: Platform, Academic, Campus, Personal domains remain decoupled with strict module visibility.
- **Layer Separation**:
  - `Controller`: Handles HTTP requests, triggers validation, maps DTOs. **No business logic is permitted in controllers.**
  - `Service`: Encapsulates business logic, transactions, authorization rules, and orchestration.
  - `Repository`: Data access abstractions built using Spring Data Mongo.
  - `DTO (Data Transfer Object)`: Explicit request and response payloads. **Entities must NEVER be exposed directly via public APIs.**

---

## 2. SOLID Principles

- **Single Responsibility Principle (SRP)**: Each class, service, or component must have one well-defined reason to change.
- **Open/Closed Principle (OCP)**: Software entities should be open for extension (via interfaces/strategies) but closed for modification.
- **Liskov Substitution Principle (LSP)**: Derived types or interface implementations must be fully substitutable for their base abstractions.
- **Interface Segregation Principle (ISP)**: Keep interfaces small, client-focused, and purpose-driven.
- **Dependency Inversion Principle (DIP)**: Depend upon abstractions (interfaces), not concrete implementations.

---

## 3. Dependency Injection & Immutability

- **Constructor Injection**: Always use explicit constructor injection (via final fields and Lombok `@RequiredArgsConstructor` or standard constructors). Avoid `@Autowired` field injection.
- **Immutability**: Prefer immutable DTOs, records, and unmodifiable collections to prevent accidental state mutation.

---

## 4. Pure Mappers & DTO Isolation

- Use dedicated, pure mapper components or methods to convert between domain entities and DTOs.
- Mappers must be side-effect-free functions that only map data structures without invoking database queries or external RPC calls.

---

## 5. One Architectural Concern Per Batch

- Each development batch or pull request must focus on a single architectural or feature concern.
- Avoid mixing cross-cutting infrastructural refactoring with feature development in the same batch.

---

## 6. Test-First Mindset

- Write unit tests alongside or before implementation logic where practical.
- Every domain service and controller endpoint must have corresponding test coverage verifying positive, negative, and edge cases.

---

## Cross-References
- [Code Style Guide](./code-style.md)
- [Git Workflow](./git-workflow.md)
