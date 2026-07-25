# Security Policy

## Supported Versions

The CampusGuide project actively maintains and releases security patches for the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 0.2.x   | :white_check_mark: |
| 0.1.x   | :x:                |
| < 0.1.0 | :x:                |

---

## Reporting a Vulnerability

The CampusGuide team takes security seriously. If you discover a vulnerability or potential security flaw in CampusGuide, please report it responsibly:

### How to Report
- **Email**: Send details of the security vulnerability to `security@campusguide.org`.
- **Do NOT create a public issue** on GitHub for security vulnerabilities.
- Provide as much information as possible, including:
  - Type of vulnerability (e.g., SQLi, XSS, broken authentication, IDOR).
  - Step-by-step instructions or proof-of-concept (PoC) script to reproduce the issue.
  - Affected components or endpoints (backend APIs, frontend routes, dependencies).
  - Potential impact of the vulnerability.

---

## Response & SLA Timeline

1. **Acknowledgment**: Our security team will acknowledge receipt of your vulnerability report within **48 hours**.
2. **Triage & Verification**: We will triage and assess the report within **5 business days**.
3. **Fix & Patch**: If confirmed, we will work on a fix and release a security patch as soon as feasible.
4. **Public Disclosure**: We will coordinate with the reporter regarding public disclosure after a patch has been published.

---

## Security Guidelines for Developers

- **Authentication**: JWT tokens must be signed using strong HMAC SHA-256 / SHA-512 algorithms.
- **Passwords**: Hashed exclusively using BCrypt with a cost factor of at least 10.
- **Input Validation**: All request DTOs must undergo server-side JSR-380 validation (`@Valid`, `@NotNull`, `@NotBlank`, etc.).
- **Secrets Management**: Credentials, database URIs, and JWT keys must never be committed to Git. Store secrets in environment variables or configuration vaults.
