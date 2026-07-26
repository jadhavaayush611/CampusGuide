# Feature Implementation Prompt Template

```markdown
## Task
Implement [Feature Name] within the [Domain Name] domain.

## Requirements
1. Review domain documentation in `docs/[domain]-module.md` and API contracts in `docs/api/`.
2. Implement backend DTOs, Repository interfaces, Service logic, and RestController endpoints.
3. Write comprehensive unit tests (`*Test.java`) and Spring Security integration tests (`*IT.java`).
4. Implement corresponding frontend UI components in `frontend/src/` if applicable.
5. Verify build integrity using `mvn clean verify` and `npm run build`.
6. Update documentation to reflect any new endpoints or schema updates.
```
