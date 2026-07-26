# Security Hardening Workflow

Procedure for auditing and securing endpoints against unauthorized access or privilege escalation.

1. **Endpoint Ownership Review**: Audit request DTOs to ensure user-controlled requests cannot modify server-derived fields.
2. **Role Authorization Checks**: Verify `@PreAuthorize("hasRole('...')")` on every write endpoint.
3. **Integration Security Testing**: Add tests using `@WithMockUser` to verify `401 Unauthorized` and `403 Forbidden` statuses.
4. **Secret Audit**: Run `git grep` to ensure no passwords or JWT keys are committed.
