---
name: architecture-auditor
description: Audits domain isolation boundaries, cross-domain dependencies, controller-service separation, and invariants across CampusGuide.
---

# Architecture Auditor Skill

## Execution Steps

1. Verify package structure follows the 4 core domains (`platform`, `academic`, `campus`, `personal`).
2. Audit RestControllers: Ensure zero business logic or direct database queries exist inside `@RestController` files.
3. Audit Repositories: Ensure MongoRepositories are scoped strictly to their owning domain.
4. Verify Invariants:
   - Calendar owns no separate database storage.
   - Atlas AI gateway does not execute direct database mutations without domain API calls.
   - Student progress academic metrics (GPA, credits) are derived on the server side.

## Verification Checklist

- [ ] Domain boundaries are clean.
- [ ] Direct entity references across domains are avoided (IDs used instead).
- [ ] Architecture documentation in `docs/architecture/` remains synchronized.
