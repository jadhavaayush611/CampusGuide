---
name: frontend-verification
description: Verifies frontend React / Vite code compilation, ESLint rules, dependencies, and production bundle generation.
---

# Frontend Verification Skill

## Execution Steps

1. Navigate to the `frontend/` directory.
2. Check if `node_modules` exists. If missing, run `npm install`.
3. Run `npm run build` to verify Vite bundle creation.
4. Run `npx eslint src` (if ESLint is configured) to check code quality.
5. Verify that visual design tokens and CSS modules align with modern UI principles.

## Verification Checklist

- [ ] `npm run build` completes without errors.
- [ ] No unhandled promise rejections or missing import errors.
- [ ] UI components handle loading and error states cleanly.
