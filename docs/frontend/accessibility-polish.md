# Accessibility Polish, Motion Preferences & UX Consistency

This document outlines the final accessibility polish pass, motion preference configurations, keyboard focus strategies, and UX consistency enhancements implemented across CampusGuide.

---

## 1. Reduced Motion (prefers-reduced-motion)

To protect users with vestibular motion disorders and respect their OS-level animation settings, we implemented a robust CSS-based global reduced motion override. 

### Implementation
We added a media query reset at the bottom of the base theme configuration [`theme.css`](file:///D:/CampusGuide/frontend/src/styles/theme.css):

```css
@media (prefers-reduced-motion: reduce) {
  *, ::before, ::after {
    animation-delay: -1ms !important;
    animation-duration: 1ms !important;
    animation-iteration-count: 1 !important;
    background-attachment: initial !important;
    scroll-behavior: auto !important;
    transition-duration: 0s !important;
    transition-delay: 0s !important;
  }
}
```

### Audited Animation Domains
This reset instantly covers and dampens the following animation elements:
* **Loading Animations**: The Compass spin in `PageLoadingFallback.tsx` and Lucide loaders in buttons.
* **Skeletons**: All `animate-pulse` skeleton blocks in dashboards, calendars, and notice feeds become static.
* **Atlas Streaming Indicators**: Sparkle spin (`MessageBubble.tsx`) and processing dot ping (`ThinkingTimeline.tsx`) states freeze.
* **Timeline Animations**: Progress line expansions and nodes transition instantly without intermediate movement.
* **Dashboard & Calendar Transitions**: Page/view transitions and modal zoom effects occur immediately without motion.

---

## 2. Focus Visibility

Consistent, high-contrast focus rings are crucial for keyboard navigation. We refactored both the focus colors and focus-visible outlines.

### Reconfiguring Ring Contrast
The default focus ring color (`--ring`) was updated to have high contrast against all background states in [`theme.css`](file:///D:/CampusGuide/frontend/src/styles/theme.css):
* **Light Mode**: `--ring: #2563EB;` (High-contrast primary blue, >4.5:1 contrast against white backgrounds).
* **Dark Mode**: `--ring: oklch(0.7 0.15 250);` (Bright light blue, >3:1 contrast against dark background colors).

### Default Outline Behavior
We configured a global outline pattern for elements with focus-visible state to ensure keyboard users always see their cursor position:

```css
*:focus-visible {
  outline: 2px solid var(--ring);
  outline-offset: 2px;
}
```
* **Native Elements & Standard Tabs**: Use the global 2px outline.
* **Shadcn & Radix Components**: Buttons and input controls with custom focus styles use `focus-visible:outline-none focus-visible:ring-ring/50 focus-visible:ring-[3px]`, avoiding double focus borders while adopting the new high-contrast focus ring color.

---

## 3. Hover & Focus Reachability

We audited the application to ensure that all interactive hover states can be accessed via keyboard navigation.

### Keyboard Accessible Cards
Custom interactive grid/list items, such as course details, were updated with key event listeners and roles:
* **Course Catalog Card** ([`CourseCatalogSection.tsx`](file:///D:/CampusGuide/frontend/src/app/components/academic/CourseCatalogSection.tsx)):
  * Added `tabIndex={0}` and `role="button"` to the grid items.
  * Added an `onKeyDown` listener supporting `Enter` and `Space` keys to select the course.
  * Configured focus rings (`focus-visible:ring-2 focus-visible:ring-[#2563EB]`).
* **Agenda Event Item** ([`AgendaView.tsx`](file:///D:/CampusGuide/frontend/src/app/components/calendar/AgendaView.tsx)):
  * Added `tabIndex={0}`, `role="button"`, and `onKeyDown` activation handlers.
* **Calendar Grid Cells** ([`MonthView.tsx`](file:///D:/CampusGuide/frontend/src/app/components/calendar/MonthView.tsx), [`WeekView.tsx`](file:///D:/CampusGuide/frontend/src/app/components/calendar/WeekView.tsx), [`DayView.tsx`](file:///D:/CampusGuide/frontend/src/app/components/calendar/DayView.tsx)):
  * Added `tabIndex={0}`, `role="button"`, and Space/Enter support to date slots, event chips, and calendar grid hour cells to allow event scheduling via keyboard.

### Hidden Hover Actions Visibility
In [`AtlasSidebar.tsx`](file:///D:/CampusGuide/frontend/src/app/components/atlas/AtlasSidebar.tsx), conversation action buttons (Rename, Archive, Delete) were previously hidden until hovered. We updated the container class to show buttons when the container gains focus anywhere inside:
```diff
- <div className="absolute right-2 top-2 hidden group-hover:flex items-center gap-1 ...">
+ <div className="absolute right-2 top-2 hidden group-hover:flex group-focus-within:flex items-center gap-1 ...">
```
Additionally, all inner buttons were equipped with focus-visible ring styles.

---

## 4. Live Regions & Streaming Content

Screen reader speech synthesizers will stutter or announce excessively if dynamic content updates too quickly. We polished live regions across several views.

* **Atlas Streaming Token Dampening** ([`MessageBubble.tsx`](file:///D:/CampusGuide/frontend/src/app/components/atlas/MessageBubble.tsx)):
  * In the AI conversation response container, we suppress `aria-live` announcements during active streaming to prevent screen readers from calling out every single word token.
  * The container announces the fully completed message content once streaming finishes:
    ```tsx
    aria-live={(!isUser && !isStreaming) ? "polite" : "off"}
    ```
* **Status Panels & Toasts**:
  * Centralized error alerts use `role="alert"` and `aria-live="assertive"` for critical failures (network connection loss, session timeout).
  * Informational updates (e.g. number of filtered results in [`CourseCatalogSection.tsx`](file:///D:/CampusGuide/frontend/src/app/components/academic/CourseCatalogSection.tsx)) utilize a single, polite region `aria-live="polite"`.

---

## 5. Icon Accessibility

We audited standard icon components (`lucide-react`) across the codebase to ensure screen readers do not read decorative glyphs:
* **Decorative Icons**: Set `aria-hidden="true"` on icons nested in status buttons, tags, chips, and badge components (e.g., `BookOpen`, `Clock`, `CheckCircle2`, `Sparkles`, `User`, `ExternalLink` in [`CourseCatalogSection.tsx`](file:///D:/CampusGuide/frontend/src/app/components/academic/CourseCatalogSection.tsx)).
* **Meaningful Actions**: Interactive icon buttons (such as settings, delete buttons, close dialog controls) are equipped with explicit descriptive `aria-label` or `title` properties.

---

## 6. Color Contrast & WCAG AA Guidance

We reviewed background/foreground contrast pairings across badges, chips, and labels.

* **Contrast Correction on Amber Badges**:
  * The light amber badge (`bg-amber-50`) paired with `text-amber-700` had a contrast ratio of ~3.5:1, which falls short of the WCAG AA 4.5:1 target for normal text.
  * Corrected instances in [`NoticeCard.tsx`](file:///D:/CampusGuide/frontend/src/app/components/notices/NoticeCard.tsx) and [`AcademicCalendarSection.tsx`](file:///D:/CampusGuide/frontend/src/app/components/academic/AcademicCalendarSection.tsx) to use `text-amber-800` (which has a contrast ratio of >4.5:1 against the pale yellow background), satisfying WCAG compliance without affecting visual branding.

---

## 7. Disabled & Loading States

We validated disabled state styling and duplicate action prevention:
* **Form Submission Blocks**: Submission buttons are disabled during active mutation queries (`disabled={isPending}`), preventing double action execution and visual state mismatch.
* **Loading Semantics**: Dialog forms, upload panels, and login/registration flows toggle `aria-busy="true"` on their submit buttons and containers during backend requests.

---

## 8. Keyboard Consistency Summary

Below is the verified support matrix for standard keyboard interactions:

| Key / Control | Expected Action | Handled By |
| :--- | :--- | :--- |
| **Escape** | Closes modals, dropdowns, popovers, select boxes | Radix Primitives & native hooks |
| **Enter / Space** | Triggers click actions on focused buttons, links, cards, calendar cells | Native elements / Custom event handlers |
| **Arrow keys** | Navigates calendar view headers, dropdown menu items, select options | Radix Primitives / Custom listbox logic |
| **Tab / Shift-Tab** | Cycles focus through all active controls in correct document order | Native DOM layout & Radix Dialog focus traps |
