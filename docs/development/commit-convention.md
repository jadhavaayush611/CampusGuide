# Commit Message Conventions

CampusGuide adheres strictly to standard Conventional Commits formatting across all sub-systems and modules.

---

## 1. Commit Message Structure

```text
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

- **Type**: Indicates the nature of the change.
- **Scope**: (Optional) The specific domain, module, or component affected.
- **Summary**: Concise description in imperative, present tense (e.g., "implement council membership workflow").

---

## 2. Allowed Commit Types

| Type | Description |
|---|---|
| `feat` | A new feature or endpoint implementation |
| `fix` | A bug fix |
| `refactor` | Code changes that neither fix a bug nor add a feature |
| `docs` | Documentation changes only |
| `test` | Adding or updating tests |
| `style` | Formatting, missing semi-colons, whitespace changes |
| `build` | Changes to build configuration or dependencies (Maven, Vite) |
| `ci` | Changes to CI/CD pipelines and scripts |
| `perf` | Code changes that improve performance |
| `chore` | Maintenance tasks, repository housekeeping |

---

## 3. Commit Examples

```text
feat(councils): implement council membership workflow

refactor(auth): simplify registration service

docs(planner): update API documentation

fix(academic): resolve semester credit limit calculation bug

test(personal): add integration test for recommendation service

chore(deps): update Spring Boot framework dependency
```

---

## 4. Guidelines

1. **Imperative Mood**: Use "add" instead of "added" or "adds", "implement" instead of "implemented".
2. **Capitalization**: Keep summary lowercase after the colon.
3. **No Trailing Period**: Do not end the commit summary line with a period.
4. **Breaking Changes**: Indicate breaking changes by placing `!` after the type/scope or including `BREAKING CHANGE:` in the footer.

---

## Cross-References
- [Git Workflow](./git-workflow.md)
- [Code Style Guide](./code-style.md)
