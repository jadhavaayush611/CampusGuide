# Documentation Synchronization Prompt Template

```markdown
## Task
Synchronize documentation in `docs/` with recent codebase updates.

## Workflow
1. Audit modified API controllers and DTOs against `docs/api/`.
2. Update domain guides in `docs/` for any new business rules or database schema changes.
3. Update Postman API collections in `docs/postman/` if routes or request bodies changed.
4. Verify all file references in documentation use relative paths or `file:///` links.
```
