---
name: api-contract-checker
description: Verifies REST API contracts, request/response DTO schemas, and documentation consistency in docs/api/.
---

# API Contract Checker Skill

## Execution Steps

1. Compare controller mapping routes with documentation in `docs/api/` and `docs/api-contracts.md`.
2. Inspect DTO fields: Verify field naming conventions (camelCase), type matching, and validation annotations.
3. Validate Postman collections in `docs/postman/` against updated endpoints.
4. Verify HTTP status codes:
   - `200 OK` for successful fetches and updates.
   - `201 Created` for new resource creations.
   - `400 Bad Request` for validation failures.
   - `401 Unauthorized` for missing/invalid JWT tokens.
   - `403 Forbidden` for insufficient role permissions.
   - `404 Not Found` for non-existent entities.
