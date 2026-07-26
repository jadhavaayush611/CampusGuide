# CampusGuide AI Agent Development Assets

This directory contains shared AI agent skills, prompt libraries, coding workflows, and project instructions for AI coding assistants working on the CampusGuide codebase.

---

## Directory Overview

```
.agents/
├── instructions/             # Project instructions and domain invariants
│   ├── project-instructions.md
│   ├── backend-guidelines.md
│   ├── frontend-guidelines.md
│   └── security-rules.md
├── skills/                   # Specialized agent skills
│   ├── backend-verification/
│   ├── frontend-verification/
│   ├── architecture-auditor/
│   ├── api-contract-checker/
│   └── repository-hygiene/
├── prompts/                  # Reusable prompt templates
│   ├── feature_implementation.md
│   ├── security_audit.md
│   ├── bug_remediation.md
│   ├── refactoring_guide.md
│   └── documentation_sync.md
└── workflows/                # Standardized AI execution workflows
    ├── verification-workflow.md
    ├── feature-development-workflow.md
    ├── security-hardening-workflow.md
    └── repository-recovery-workflow.md
```

---

## Core Principles for AI Agents

1. **Non-destructive & Portable**: Never commit absolute machine paths, tokens, or environment secrets.
2. **Domain Isolation**: Respect domain boundaries (`platform`, `academic`, `campus`, `personal`).
3. **Verification First**: Never claim completion without executing local verification scripts (`mvn clean verify`, `npm run build`, `scripts/build.bat`).
4. **DTO Contract Enforcer**: Entities must never be exposed directly in REST controllers.

---

## Agent Documentation Cross-References

- [Atlas Agent Operational Guide](file:///D:/CampusGuide/docs/agents/atlas-agent.md)
- [Backend Agent Operational Guide](file:///D:/CampusGuide/docs/agents/backend-agent.md)
- [Frontend Agent Operational Guide](file:///D:/CampusGuide/docs/agents/frontend-agent.md)
- [Documentation Agent Operational Guide](file:///D:/CampusGuide/docs/agents/documentation-agent.md)
- [Mobile Agent Operational Guide](file:///D:/CampusGuide/docs/agents/mobile-agent.md)
