# Accessibility Foundations

## Scope

This document defines shared accessibility conventions for CampusGuide frontend foundations:

- Root application layout and route shells
- Shared navigation surfaces (sidebar/header/global menus)
- Reusable UI primitives in `frontend/src/app/components/ui`
- Global loading, toast, and error boundary behavior

Feature-page audits are tracked separately.

## Semantic structure

- Use semantic landmarks in shared layout:
  - `aside` for the persistent app sidebar
  - `nav` for primary navigation (`aria-label="Primary"`)
  - `header` for page-level top bars
  - `main` should be provided by route pages (single main per rendered view)
- Prefer semantic containers (`section`, `article`, list elements) over generic wrappers when content has structure.

## Navigation landmarks and active state

- Primary navigation links use router-aware links so `aria-current="page"` is set automatically on the active route.
- Icon-only controls (notification trigger, settings, user avatar menu trigger, close controls) must include an accessible name (`aria-label` or visually hidden text).
- All interactive controls must expose visible keyboard focus via `focus-visible` ring styles.

## ARIA conventions

- Prefer native semantics first; add ARIA only when native elements are insufficient.
- Shared conventions:
  - Loading surfaces: `role="status"`, `aria-live="polite"`, `aria-busy="true"`
  - Error fallbacks: `role="alert"`, `aria-live="assertive"`
  - Decorative icons/containers should be `aria-hidden` where appropriate
- Avoid redundant ARIA that duplicates native behavior.

## Keyboard standards

- Shared menus/popovers/dialogs use Radix primitives so keyboard interactions are preserved:
  - Enter/Space activation
  - Escape to close overlays
  - Roving/focus management in menu-like composites
- Navigation order must follow DOM order.
- No keyboard traps outside modal contexts.

## Focus management policy

- Overlays (Dialog, Sheet, Alert Dialog, Dropdown Menu, Popover) rely on Radix focus behavior:
  - focus moves into opened surface
  - focus returns to trigger when closed
  - modal surfaces trap focus while open
- Use Radix defaults unless a concrete requirement requires override.

## Reduced motion policy

- Respect `prefers-reduced-motion` on shared decorative animations.
- For shared primitives with open/close animation, include motion-reduce fallbacks (`motion-reduce:animate-none`, reduced transition duration).
- Preserve functional feedback while removing non-essential motion.

## Shared helpers and components

- Reuse existing shared primitives (`ui/*`) instead of custom overlay/menu implementations.
- Keep public component APIs stable; accessibility enhancements should be internal and non-breaking.

## Known limitations

- This foundations pass does not cover every feature page interaction surface yet.
- Some domain-specific components may still need per-page semantic and form-level accessibility refinements in subsequent batches.
