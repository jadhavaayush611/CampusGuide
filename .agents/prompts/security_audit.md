# Security Audit Prompt Template

```markdown
## Task
Perform a security audit and vulnerability remediation on [Domain / Component].

## Audit Checkpoints
1. Validate Spring Security authorization rules (`@PreAuthorize`, `@Secured`).
2. Verify field ownership: Ensure clients cannot modify administrative or server-calculated fields.
3. Check DTO separation: Confirm database entities are never returned or accepted directly.
4. Audit exception handling: Ensure sensitive stack traces are not leaked to API callers.
5. Verify test coverage for unauthorized (`401`) and forbidden (`403`) scenarios.
```
