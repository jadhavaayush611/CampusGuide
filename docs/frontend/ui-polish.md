# Production UI Polish & Design System Guidelines

This document details the guidelines, specifications, and principles governing the UI and UX consistency across CampusGuide. It serves as the single source of truth for design tokens, page layout margins, components, and interaction states.

---

## 1. Design Principles

1. **Information Hierarchy First**: Content structure must be immediately clear. Users should identify the most critical elements in under 3 seconds.
2. **Surface & Elevation Intent**: Colors, borders, and shadows indicate structural layers. High-elevation surfaces (cards, modals) always rest on low-elevation backgrounds.
3. **Intentional Transitions**: Interactive elements must respond to user input with soft, subtle transitions (`150ms` duration, ease-in-out). 
4. **Adaptive Context**: Components should adapt seamlessly to Light and Dark modes without losing their contrast ratios or branding identities.

---

## 2. Spacing Scale

Our spacing scale uses a strict base-8 layout system (`rem` values) to align columns, padding, and layout wrappers consistently.

| Utility / Value | Equivalent CSS | Purpose |
| :--- | :--- | :--- |
| `space-1` / `0.25rem` | `4px` | Tiny gaps (elements inside badges, subtext margins) |
| `space-2` / `0.5rem` | `8px` | Small gaps (labels to inputs, internal item elements) |
| `space-3` / `0.75rem` | `12px` | Moderate gaps (dropdown list items, inline meta text) |
| `space-4` / `1.0rem` | `16px` | Base layout padding (small mobile padding, list items spacing) |
| `space-6` / `1.5rem` | `24px` | Standard padding (card internal padding, desktop gaps) |
| `space-8` / `2.0rem` | `32px` | Desktop outer padding (main layout margins, large page splits) |
| `space-12` / `3.0rem`| `48px` | Large section separators (hero banners, modal panels spacing) |

- **Page Margins**: Main views must use `p-4 sm:p-6 lg:p-8` padding with a maximum container width of `max-w-[1440px]` (`max-w-7xl` or custom `[1440px]`).
- **Section Spacing**: Vertically align modules using `space-y-8`.
- **Card Padding**: Standard cards must use `p-6` (or `px-6 py-6`).
- **Modal / Dialog Spacing**: Modal content must use a standard padding of `p-6`.

---

## 3. Typography Hierarchy

CampusGuide relies on modern, premium typography scales. Elements inherit the baseline weights and colors automatically:

| Token / Type | Tailwind Class | Font Weight | Line Height | Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Page Title** | `text-3xl font-bold tracking-tight` | `700` (Bold) | `1.2` | Main page heading |
| **Header Banner**| `text-2xl font-bold` | `700` (Bold) | `1.25` | Section-level titles |
| **Section Title**| `text-xl font-semibold` | `600` (Semi-Bold) | `1.3` | Card headers, widget groups |
| **Card Title** | `text-base font-semibold` | `600` (Semi-Bold) | `1.4` | Course titles, activity headers |
| **Standard Body**| `text-sm font-normal` | `400` (Normal) | `1.5` | Main content text |
| **Muted Body** | `text-sm text-muted-foreground` | `400` (Normal) | `1.5` | Descriptions, secondary details |
| **Helper / Small**| `text-xs text-muted-foreground` | `400` (Normal) | `1.4` | Input captions, metadata |
| **Badge / Chip** | `text-[10px] uppercase font-bold`| `700` (Bold) | `1` | Status tags, activity metrics |

---

## 4. Button Standards

All interactive buttons in CampusGuide must utilize the unified `buttonVariants` interface with micro-animations.

### Base Interactive States
- **Hover**: Background changes slightly (e.g. `bg-primary/95` or `bg-accent`), cursor changes to `pointer`.
- **Active / Pressed**: Button scales down slightly to `active:scale-[0.98]` with a `150ms` transition.
- **Focus**: Displays a visible, color-matched outer border ring (`focus-visible:ring-ring focus-visible:ring-[3px]`).
- **Disabled**: Standardized to `disabled:opacity-50 disabled:pointer-events-none`.

### Button Styles
- **Primary**: Brand-colored surface (`bg-primary text-primary-foreground`). Use for confirmation and main CTAs.
- **Secondary**: Dark/light neutral fill (`bg-secondary text-secondary-foreground`). Use for secondary page actions.
- **Outline**: Bordered, empty background (`border border-border bg-background text-foreground`). Use for filters, edit buttons, or secondary triggers.
- **Ghost**: Empty background (`hover:bg-accent hover:text-accent-foreground`). Use for icon-only buttons, row-level menu buttons, or cancels.
- **Destructive**: Alarm-colored surface (`bg-destructive text-white`). Use for deletes, leaves, or cancellations.

---

## 5. Form Standards

Every CRUD input form follows a strict layout checklist to ensure readability and reduce cognitive load:

1. **Labels**: Placed above the input using `text-sm font-medium text-foreground`. Required inputs must display a red asterisk (`*`).
2. **Spacing**: Inputs inside a form container must be spaced with `space-y-4` or grouped inside a grid with `gap-4`.
3. **Helper / Validation Text**: Form descriptions use `text-xs text-muted-foreground`. Validation errors must use `text-xs text-destructive` and update the input border color to `border-destructive`.
4. **Footer Alignment**:
   - Secondary actions (Cancel, Back) go on the left or bottom (in stacked layouts).
   - Primary actions (Save, Submit, Create) go on the right.
   - Button size must match input size (`h-9` or `h-10`).
5. **Loading States**: When submitting, disable buttons, replace label/icon with a `Loader2` rotating spinner, and display an active indicator.

---

## 6. Loading Guidelines

Avoid flashing blank states. Cohesive visual transitions prevent layouts from jumping.

1. **Page Fallback**: Full page routing must load using `PageLoadingFallback` with a centralized, smooth spinner.
2. **Component Skeletons**: Use the pulse component (`Skeleton`) matching the dimensions of the final loaded cards/lists:
   - Must use `animate-pulse motion-reduce:animate-none`.
   - Maintain the original outline margins, borders, and border-radii (`rounded-2xl` or `rounded-xl`).
3. **Indicators**: Realtime/streaming components (like Atlas timeline inputs) must display subtle animate-pulse indicators.

---

## 7. Empty State Guidelines

Empty states must reassure the user and offer a path forward rather than feeling like a dead end.

- **Layout**: Center-aligned layout with `space-y-4` and maximum width `max-w-md` inside a card shell (`bg-card rounded-2xl border border-border p-12`).
- **Icon**: Central circular badge in `bg-muted/10` featuring a thematic icon matching the empty content.
- **Copy**:
   - **Title**: Expressive yet concise (`No matching notifications found`, `No Resources Found`).
   - **Description**: Conversational and descriptive instructions (`Try adjusting your search query, priority filters, or category tabs to discover notifications.`).
- **CTA Actions**: Offer a primary action (e.g. `Reset Filters`, `Upload New Resource`, `Create Task`) to guide users directly.

---

## 8. Error State Guidelines

All error boundaries and inline alerts must be visually consistent to represent system issues.

- **Theme Compliance**: Error cards must adapt to dark mode using `bg-destructive/5 dark:bg-destructive/10 border border-destructive/20 text-destructive rounded-2xl p-6 text-center space-y-4 shadow-xs`.
- **Layout**:
  1. Centered layout with a circular destructive warning icon (e.g., `AlertCircle` or `AlertTriangle`).
  2. Large title highlighting what failed.
  3. Actionable description of next steps.
  4. Explicit brand-colored **Retry Button** with a `RefreshCw` spin icon.
- **Privacy**: Hides internal error traces, JavaScript Reference/Type errors, or Database details, presenting friendly messages via `ErrorHandler.getUserMessage`.

---

## 9. Toast Guidelines

Toasts are delivered via Sonner and styled using theme variables (`--normal-bg: var(--popover)`, `--normal-border: var(--border)`). Toast messages must follow these rules:

1. **Active Phrasing**: Use title case or capitalized active sentences. Avoid passive descriptions.
2. **Punctuation**: Success toasts must end with a period (`.`) for formal records (e.g., `Notice marked as read.`, `Event rescheduled successfully.`).
3. **Templates**:
   - **Success**: `[Object] [action] successfully.` or `[Action] completed.` (e.g., `Personal event deleted successfully.`).
   - **Error**: `Failed to [action]: [reason]` (e.g., `Rescheduling failed: Server offline.`).
   - **Loading / Progress**: `Downloading [item]...` or `Updating progress...`.

---

## 10. Animation & Motion Guidelines

Animations must be subtle, fast, and respectful of system preferences:

- **Durations**: Standard durations are `150ms` (hover, fade) to `200ms` (modal slide, drawer slide).
- **Scale**: Interactive taps use a scale of `active:scale-[0.98]`.
- **Reduced Motion**: If a user has `prefers-reduced-motion: reduce` configured in their browser, all transitions, scales, rotations, and animations are immediately bypassed (`transition-duration: 0s`, `animation-duration: 1ms`).

---

## 11. Responsive Design Guidelines

Every view must support standard breakpoints: Mobile (`xs`/`sm`), Tablet (`md`), Laptop (`lg`), Desktop (`xl`).

- **Layout Wrappers**: Standard outer layout columns must wrap into single columns on small tablet and mobile layouts (`grid-cols-1 lg:grid-cols-3` or `grid-cols-1 md:grid-cols-2`).
- **Scroll Areas**: Containers must handle long labels or lists using native scroll wrappers (`overflow-x-auto` or `scroll-area`). Do NOT allow page-level horizontal overflow under any circumstances.
- **Dialogs / Drawers**: Larger dialogs on desktops automatically render as Vaul drawers (`Vaul.Root`) on mobile devices to preserve natural tap targets and screen space.

---

## 12. Visual Consistency Checklist

Before deploying frontend changes, verify that the following items are fully checked:

- [ ] Page wrappers use `bg-gray-50 dark:bg-background min-h-screen text-foreground transition-colors duration-150`.
- [ ] Borders use `border-border` (`var(--border)`) to automatically adjust in dark mode instead of hardcoded `border-gray-200`.
- [ ] Card surfaces use `bg-card text-card-foreground border rounded-xl`.
- [ ] Headings (`h1` - `h4`) utilize standard theme sizing classes via Tailwind `@apply` to preserve hierarchy.
- [ ] Input borders, hover styles, and focus states match active outlines and theme rings (`ring-ring`, `bg-input-background`, `border-input`).
- [ ] All interactive buttons feel alive with `hover:bg-...`, `active:scale-[0.98]`, and standard cursor pointers.
- [ ] Text styling avoids plain dark mode overrides (always check layout in both modes).
- [ ] Empty state containers and error states follow standard centered patterns.
- [ ] Toasts are capitalized and use standardized success/error styling.
