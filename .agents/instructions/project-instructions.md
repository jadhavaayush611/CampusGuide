# CampusGuide AI Project Instructions

## Context
CampusGuide is a multi-domain modular monolithic application for campus management built with Spring Boot (Java 25) and React (Vite/JavaScript).

## Architecture Boundaries
1. **Four Core Domains**:
   - `com.campusguide.platform`: User authentication, Spring Security, global search, admin analytics.
   - `com.campusguide.academic`: Course catalog, degree roadmaps, semester planner, student progress.
   - `com.campusguide.campus`: Councils directory, community forums, event management, resource center.
   - `com.campusguide.personal`: Personal AI Assistant (Atlas), recommendation engine, document vault, resume builder.

2. **Critical Invariants**:
   - **No cross-domain direct entity calls**: Domain services communicate via documented DTOs and interfaces.
   - **No business logic in controllers**: RestControllers handle request validation and delegated service calls only.
   - **Decoupled Security DTOs**: Never expose internal entities in controller requests/responses.

## Code Style & Formatting Enforcement (.editorconfig)
AI agents working on feature implementations, refactoring tasks, or bug fixes MUST strictly enforce [.editorconfig](../../.editorconfig) formatting standards across all generated code:

- **Never introduce formatting inconsistent with `.editorconfig`**:
  - 4 spaces for Java, Kotlin, XML, Properties, YAML, SQL (`indent_size = 4`).
  - 2 spaces for JavaScript, TypeScript, JSX, TSX, JSON, CSS, SCSS, HTML, Markdown (`indent_size = 2`).
- **Always preserve LF (`\n`) line endings** for all source code, markdown, scripts (except Windows `.bat`/`.cmd`), and configs.
- **Always preserve the final newline** at the end of every file (`insert_final_newline = true`).
- **Never introduce hard tabs** where spaces are required (`indent_style = space`).
- **Respect language-specific whitespace rules**, including preserving trailing whitespace in Markdown (`trim_trailing_whitespace = false` for `*.md`).
- **Treat formatting violations as implementation defects**: Any pull request or code change with non-compliant indentation, line endings, or tab usage is considered defective and must be fixed prior to submission.

## Testing & Verification
- Backend tests live in `backend/src/test/java/com/campusguide/...`.
- Run `mvn clean verify` inside `backend/` to run all unit and integration tests.
- Run `scripts/build.bat` or `scripts/build.sh` for full repository build checks.
