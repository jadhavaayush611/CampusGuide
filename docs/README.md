# CampusGuide Documentation Framework

Welcome to the central documentation repository for **CampusGuide** — a multi-domain campus management and student guidance platform powered by AI.

---

## 1. Structure Overview

The documentation is organized by target audience and functional domain across nine core directories:

```
docs/
├── README.md                  # Documentation standards, governance, and indexing
├── architecture/              # High-level architecture, domain boundaries, data flow, SaaS vision
│   ├── system-overview.md
│   ├── domain-architecture.md
│   ├── database-design.md
│   ├── permission-model.md
│   └── saas-roadmap.md
├── api/                       # API contracts & frameworks by domain
│   ├── authentication.md
│   ├── academic.md
│   ├── campus.md
│   ├── personal.md
│   └── atlas.md
├── modules/                   # Architectural overviews of core modules
│   ├── councils.md
│   ├── communities.md
│   ├── planner.md
│   ├── calendar.md
│   ├── resources.md
│   ├── notices.md
│   ├── notifications.md
│   └── achievements.md
├── development/               # Developer setup, conventions, branching, and testing
│   ├── git-workflow.md
│   ├── commit-convention.md
│   ├── code-style.md
│   ├── architecture-principles.md
│   ├── development-setup.md
│   └── testing-strategy.md
├── deployment/                # Environment, infrastructure, and deployment standards
│   ├── local-setup.md
│   ├── environment.md
│   └── production.md
├── ai/                        # Atlas AI assistant system & gateway architecture
│   ├── atlas.md
│   ├── memory-engine.md
│   ├── rag.md
│   ├── intent-engine.md
│   └── action-planner.md
├── mobile/                    # Mobile client specifications & interface reference
│   ├── mobile-overview.md
│   ├── authentication.md
│   ├── api-reference.md
│   ├── navigation.md
│   ├── planner.md
│   ├── calendar.md
│   ├── councils.md
│   ├── notifications.md
│   ├── atlas.md
│   ├── offline-strategy.md
│   ├── ui-guidelines.md
│   └── release-checklist.md
└── agents/                    # Rules & operational guides for AI coding agents
    ├── backend-agent.md
    ├── frontend-agent.md
    ├── mobile-agent.md
    ├── atlas-agent.md
    └── documentation-agent.md
```

---

## 2. Documentation Governance & Ownership

Each section of the documentation framework is assigned explicit ownership:

| Directory | Primary Owner | Target Audience | Focus |
|---|---|---|---|
| `architecture/` | Lead Architect | All Engineers / Tech Leads | System design, cross-domain rules, database single source of truth |
| `api/` | Backend Engineers | Frontend / Mobile / Integration Leads | Endpoint contracts, DTO definitions, security boundaries |
| `modules/` | Feature Owners | Domain Developers | Domain responsibilities, service scopes, entity models |
| `development/` | DevOps & Engineering Leads | All Contributors | Setup, git workflows, quality gates, test requirements |
| `deployment/` | DevOps / Infra Engineers | Site Reliability Engineers | Infrastructure, Docker, production readiness, secrets |
| `ai/` | AI Engineers | AI / Backend Developers | Atlas gateway, prompt engineering, RAG, memory engine |
| `mobile/` | Mobile Engineers | iOS / Android / React Native Teams | Mobile architecture, API consumption, offline sync, UI/UX |
| `agents/` | AI Agent Governance | AI Coding Assistants | Context boundaries, coding guidelines, operational constraints |

---

## 3. How & When to Update Documentation

Documentation must evolve alongside code. To prevent documentation drift:

1. **Feature Additions**: When adding or altering features, APIs, or database models, update the respective domain document in `docs/api/`, `docs/modules/`, or `docs/architecture/` in the same pull request.
2. **Architecture Decisions**: Architectural shifts require updating `docs/architecture/` and adding cross-references in affected module guides.
3. **Agent Rules**: Any new system invariant or "What NOT to Do" constraint must be added immediately to the corresponding agent guide in `docs/agents/`.

---

## 4. Documentation Standards

- **Concise & Direct**: Focus on facts, contracts, and design intent. Avoid unnecessary fluff.
- **Cross-Referenced**: Use relative links to point to canonical documents instead of duplicating schemas or contract definitions.
- **Diagram First**: Use standard Mermaid diagrams for sequence flows, state machines, and class structures.

---

## 5. Mandatory Lock Rule

> [!IMPORTANT]
> **LOCK RULE**: No implementation batch or pull request is complete until all relevant documentation under `docs/` has been updated and verified against the implementation changes.
