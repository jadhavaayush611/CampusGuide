# Feature Development Workflow

Standardized procedure for introducing new features across backend and frontend domains.

1. **Architecture Planning**: Read relevant specs in `docs/architecture/` and `docs/api/`.
2. **DTO & Domain Modeling**: Create immutable request/response DTOs and Mongo entity models.
3. **Repository & Service Implementation**: Implement service interfaces and business logic.
4. **Security & Controller Wiring**: Expose REST endpoints secured with `@PreAuthorize`.
5. **Unit & IT Testing**: Create test classes in `src/test/java/com/campusguide/...`.
6. **Frontend Component Creation**: Wire React state and components to backend REST endpoints.
7. **Verification**: Execute `scripts/build.bat` or `scripts/build.sh`.
