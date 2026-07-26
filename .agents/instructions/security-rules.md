# Security & Compliance Invariants

## Invariants
1. **Zero Hardcoded Secrets**: Never check in API keys, database passwords, JWT secrets, or tokens. Use environment variables (`application.properties` with `${ENV_VAR}` defaults).
2. **Server-Side Authorization**: Never trust client inputs for permissions or calculated user privileges (e.g. GPA, total credits earned, role status).
3. **Role Checks**: All modifying endpoints (POST/PUT/DELETE) must enforce authentication and role authorization via `@PreAuthorize`.
4. **Data Sanitization**: Validate request payloads using standard Jakarta validation annotations (`@NotBlank`, `@NotNull`, `@Size`).
