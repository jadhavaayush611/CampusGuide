# Frontend Agent Operational Guide

This document defines operational rules, visual aesthetics, and coding expectations for AI coding agents working on the CampusGuide React + Vite frontend application.

---

## 1. Project Vision
Deliver a responsive, visually stunning web application for students, faculty, and administrators, providing instant access to academic planning, campus communities, and Atlas AI guidance.

---

## 2. Architecture
- **Framework**: React + Vite + TypeScript / JavaScript.
- **Styling**: Vanilla CSS with CSS design tokens (`index.css`), glassmorphism, and micro-animations. Avoid Tailwind unless requested.
- **API Client**: Modular REST client modules in `src/services/`.

---

## 3. Responsibilities
- Construct modular React UI components using centralized design tokens.
- Handle state management locally (`useState`, `useReducer`) and connect to backend APIs.
- Provide smooth UI transitions, micro-animations, and dynamic visual feedback.

---

## 4. Coding Standards
- Keep components focused and reusable.
- Use explicit API service abstractions instead of ad-hoc `fetch()` calls inside components.
- Implement robust loading states, error boundaries, and empty state representations.

---

## 5. Naming Conventions
- Components: PascalCase (`PlannerCard.jsx`, `AtlasChatDrawer.jsx`).
- CSS Variables: Kebab-case (`var(--color-primary-indigo)`).
- Services: CamelCase (`academicService.js`, `atlasService.js`).

---

## 6. What NOT to Do

> [!CAUTION]
> **CRITICAL INVARIANTS**:
> - **Calendar owns no data**: Do not attempt to POST primary calendar data; aggregate events from academic, campus, and personal endpoints.
> - **Atlas never mutates data directly**: Atlas UI actions must present confirmation cards that execute standard API calls upon user click.
> - **Business logic belongs in services**: Do not implement prerequisite validation or GPA calculation rules in frontend components.
> - **Councils and Communities are separate concepts**: Keep council administrative dashboards distinct from community interest feeds.
> - **Shared resources should not be duplicated**: Do not store redundant client-side copies of course catalogs; query API services.

---

## 7. Development Workflow
1. Inspect UI specifications and CSS token definitions in `index.css`.
2. Build reusable UI components with interactive hover and focus states.
3. Validate API interactions against backend REST contracts.
4. Verify visual excellence and mobile responsiveness.

---

## 8. Expected Output Quality
- Premium, state-of-the-art UI design with high aesthetic appeal.
- Zero console errors or unhandled promise rejections.
- Fully responsive across mobile, tablet, and desktop viewports.

---

## Cross-References
- [System Overview](file:///D:/CampusGuide/docs/architecture/system-overview.md)
- [Coding Standards](file:///D:/CampusGuide/docs/development/coding-standards.md)
