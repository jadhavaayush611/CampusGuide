# Git Commit Conventions

CampusGuide strictly adheres to the **Conventional Commits 1.0.0** specification.

---

## 1. Commit Message Structure

```text
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

---

## 2. Commit Types

| Type | Description | Example |
|---|---|---|
| `feat` | A new user-facing feature or API endpoint | `feat(academic): add prerequisite validation to semester planner` |
| `fix` | A bug fix | `fix(auth): resolve JWT expiration parsing issue` |
| `docs` | Documentation changes only | `docs(architecture): add SaaS expansion roadmap` |
| `refactor` | Code changes that neither fix a bug nor add a feature | `refactor(campus): extract CouncilApplication validation logic` |
| `test` | Adding or updating unit/integration tests | `test(personal): add integration test for recommendation strategies` |
| `chore` | Maintenance, build config, or dependency updates | `chore(deps): upgrade Spring Boot to 4.0.6` |
| `perf` | Code changes that improve performance | `perf(search): add compound index to mongo audit logs` |

---

## 3. Allowed Scopes

- `auth`, `academic`, `campus`, `personal`, `atlas`, `planner`, `calendar`, `resources`, `councils`, `communities`, `notices`, `notifications`, `achievements`, `infra`, `deps`

---

## 4. Breaking Changes

Breaking changes must include `BREAKING CHANGE:` in the footer or an exclamation mark after the type/scope (e.g., `feat(auth)!: replace legacy session cookie with JWT Bearer`).

---

## Cross-References
- [Branching Strategy](file:///D:/CampusGuide/docs/development/branching-strategy.md)
- [Coding Standards](file:///D:/CampusGuide/docs/development/coding-standards.md)
