# Backend Coding & Architectural Guidelines

## Technology Stack
- Java 25 / Spring Boot 4.0.6
- Spring Data MongoDB
- Spring Security + JWT
- JUnit 5 / Spring Security Test

## Coding Rules & EditorConfig Compliance
1. **Formatting (.editorconfig)**: Enforce 4-space indentation, LF line endings, UTF-8 encoding, and insert final newline. Never use hard tabs.
2. **Dependency Injection**: Always use constructor injection (`@RequiredArgsConstructor` or explicit constructors). Avoid `@Autowired` field injection.
3. **DTO Layer**: Create explicit Request and Response DTOs for every API endpoint. Use ModelMapper / Mappers or static mapper methods.
4. **Exception Handling**: Standardize error handling with `@RestControllerAdvice` and domain exceptions. Never swallow exceptions or log and ignore.
5. **Security Annotations**: Protect sensitive endpoints with `@PreAuthorize("hasRole('ADMIN')")` or role checks.
6. **Auditing & Logging**: Use `LoggerFactory` / `@Slf4j` for standard logging. Never use `System.out.println`.
