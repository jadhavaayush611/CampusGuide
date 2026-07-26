---
name: backend-verification
description: Verifies backend Java / Spring Boot code compilation, test suite execution, security authorization, and DTO contracts.
---

# Backend Verification Skill

## Execution Steps

1. Navigate to the `backend/` directory.
2. Run `mvn clean verify` to execute compilation, unit tests, and integration security tests.
3. Inspect output log for any build or test failures.
4. Verify that no raw entities are exposed in controllers and that all service methods enforce DTO contracts.
5. Ensure `@PreAuthorize` security constraints are maintained on updated endpoints.

## Verification Checklist

- [ ] `mvn clean verify` passes with 0 failures and 0 errors.
- [ ] New endpoints have corresponding Integration Tests (`*IT.java`).
- [ ] New service methods have corresponding Unit Tests (`*Test.java`).
