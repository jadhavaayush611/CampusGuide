# Documentation Agent Operational Guide

This document defines governance rules, formatting standards, and structural guidelines for AI agents maintaining project documentation in CampusGuide.

---

## 1. Project Vision
Maintain a single source of truth documentation framework across backend, frontend, mobile, and AI agent domains, ensuring zero drift between implementation and architectural documentation.

---

## 2. Architecture
Documentation is organized under `docs/` into 9 core directories: `architecture/`, `api/`, `modules/`, `development/`, `deployment/`, `mobile/`, `ai/`, `agents/`, and `docs/README.md`.

---

## 3. Responsibilities
- Keep API contracts, module specifications, and domain architecture diagrams updated.
- Enforce cross-referencing between related documents to eliminate content duplication.
- Ensure every directory contains active, meaningful markdown documentation.

---

## 4. Coding & Markdown Standards
- Use standard GitHub Flavored Markdown (GFM).
- Use Mermaid diagrams for sequence flows, class models, and system topologies.
- Use clickable file links with `file://` URIs for file references.

---

## 5. Naming Conventions
- Directories: Lowercase single-word or hyphenated (`architecture`, `development`, `ai`).
- Markdown Files: Lowercase kebab-case (`domain-architecture.md`, `system-overview.md`).

---

## 6. What NOT to Do

> [!CAUTION]
> **CRITICAL INVARIANTS**:
> - **Mandatory Lock Rule**: **No implementation batch is complete until the relevant documentation has been updated.**
> - **Calendar owns no data**: Document Calendar consistently as a read-only aggregation view across all module and mobile guides.
> - **Atlas never mutates data directly**: Ensure all AI specs document Atlas as a non-mutating advisory engine.
> - **Business logic belongs in services**: Ensure module documentation places business logic boundaries squarely in service layers.
> - **Councils and Communities are separate concepts**: Maintain explicit separation in module and API documentation.
> - **Shared resources should not be duplicated**: Avoid repeating data schemas across multiple documents; use relative cross-reference links.

---

## 7. Development Workflow
1. Identify code changes in the active feature batch or pull request.
2. Locate the corresponding markdown files in `docs/`.
3. Update API endpoints, DTO placeholders, or module diagrams to reflect code reality.
4. Verify all relative cross-reference links.

---

## 8. Expected Output Quality
- Concise, professional, and future-proof markdown files.
- Accurate Mermaid diagram syntax.
- Zero broken relative links or stale documentation.

---

## Cross-References
- [Documentation Standards](file:///D:/CampusGuide/docs/README.md)
- [System Overview Architecture](file:///D:/CampusGuide/docs/architecture/system-overview.md)
