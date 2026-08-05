# CampusGuide Frontend Accessibility Guidelines & Implementations

This document outlines the accessibility (a11y) strategies, keyboard interaction patterns, ARIA semantics, and component refactoring implemented across CampusGuide's interactive components.

---

## 1. Overview & Core Principles

All accessibility refactors are guided by the following principles:
- **Zero Visual Changes**: Keep styling and design integrity identical to the original layout while inserting off-screen accessibility states and keyboard focus systems.
- **Focus Indicators**: Every interactive card, button, and input must show clear outline states (`focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2`).
- **Landmarks & Semantics**: Avoid generic markup (like raw `div` tags) for interactive or landmark components. Map them to semantic HTML5 elements or explicit ARIA roles (e.g. `role="button"`, `role="tablist"`, `role="search"`).
- **Decorative Media Hiding**: Screen readers shouldn't stutter on decorative visual assets. Set `aria-hidden="true"` on all Lucide and visual icons.

---

## 2. Interactive Cards & Lists

### Impacted Components
- `CommunityCard.tsx`
- `CouncilCard.tsx`
- `ResourceCard.tsx`
- `NoticeCard.tsx`
- `NotificationItemCard.tsx`
- `TaskCard.tsx`
- `StudyGoalCard.tsx`
- `NotificationsWidget.tsx` (Dashboard)

### Implementation Pattern
For cards that acts as grid/list item buttons to trigger details panels or navigation:
1. **Interactive Attributes**: Added `role="button"` and `tabIndex={0}` to the container.
2. **Keyboard Operability**:
   ```typescript
   const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
     if (e.key === 'Enter' || e.key === ' ') {
       if (e.target === e.currentTarget) {
         e.preventDefault();
         handleCardClick();
       }
     }
   }, [handleCardClick]);
   ```
3. **Screen Reader Labels**: Added detailed context labels so visually impaired users get complete details upon tab focus:
   `aria-label="Task: [Title], Category: [Category], Priority: [Priority], Progress: [X]%"`
4. **Action Bubbling Prevention**: Prevent double-focus triggers and event bubbling on action buttons (like delete or edit) inside cards using `onClick={(e) => e.stopPropagation()}` and proper focus rings on inner actions.

---

## 3. Custom Modal Dialogs & Focus Trapping

### Impacted Components
- `EventDetailsModal.tsx`
- `CourseDetailsModal.tsx`
- `ResourceDetailsModal.tsx`
- `NoticeDetailsModal.tsx`
- `TaskDetailsModal.tsx`

### Implementation Pattern
Custom modals constructed using manual absolute/fixed divs suffer from focus leakage, lack of focus restoration, and manual Escape/Overlay close handlers. To fix this with zero visual change, we wrapped these overlays using Radix UI Dialog primitives:
- `<DialogPrimitive.Root open={isOpen} onOpenChange={...}>`
- `<DialogPrimitive.Portal>`
- `<DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs ...">`
- `<DialogPrimitive.Content className="fixed inset-0 z-50 flex items-center justify-center p-4">` (centring the card layout without visual disruption)
- `<DialogPrimitive.Title>` associated with the modal header title for instant land-marking.
- `<DialogPrimitive.Close asChild>` surrounding both close close-icon buttons and footer cancel buttons.

---

## 4. Custom Tabs & WAI-ARIA Semantics

### Impacted Components
- `NotificationCategoryTabs.tsx`
- `PlannerTabs.tsx`

### Accessibility Standards
Custom tab strips must announce themselves as tab structures and allow keyboard arrow navigation:
- **Roles & States**:
  - Container wraps in `role="tablist"` with an `aria-label` detailing the tab list group.
  - Buttons wrap in `role="tab"`, specifying `aria-selected={isActive}`, and `tabIndex={isActive ? 0 : -1}` to ensure only one tab stop exists in normal document flow.
- **Keyboard Arrow Patterns**: Pressing left/right arrow keys automatically shifts focus and activates adjacent tabs:
  ```typescript
  const handleKeyDown = (e: React.KeyboardEvent<HTMLButtonElement>, index: number) => {
    let nextIndex = index;
    if (e.key === 'ArrowRight') {
      nextIndex = (index + 1) % tabs.length;
    } else if (e.key === 'ArrowLeft') {
      nextIndex = (index - 1 + tabs.length) % tabs.length;
    } else if (e.key === 'Home') {
      nextIndex = 0;
    } else if (e.key === 'End') {
      nextIndex = tabs.length - 1;
    } else {
      return;
    }
    e.preventDefault();
    const children = Array.from(e.currentTarget.parentElement?.children || []) as HTMLButtonElement[];
    children[nextIndex]?.focus();
    onTabChange(tabs[nextIndex].id);
  };
  ```

---

## 5. Search Landmarks & Live Announcement Regions

### Impacted Components
- `CourseCatalogSection.tsx` (Search bar and result counter)
- Filter bars in discovery dashboards (`CouncilDiscovery`, `CommunityDiscovery`)

### Implementation
1. **Search Region Landmark**: Wrapped the input and icon in a landmark tag:
   `<div role="search" aria-label="Course Search">`
2. **Polite Live Regions**: Added `aria-live="polite"` directly to search result statuses. For example, as a user types a query in the Course Catalog:
   `<div className="..." aria-live="polite">Showing {filteredCourses.length} of {courses.length} courses</div>`
   Screen readers automatically voice the updated result counts to the user dynamically without blocking input focus.

---

## 6. Dropdowns, Menus & Pagination

- **Radix Dropdowns**: Converted custom toggle menus (e.g. three-dot menus on NoticeCard or TaskCard) to standard Radix UI Dropdown wrappers. Escape key automatically closes the popup and restores focus to the trigger button.
- **Accessible Pagination**: Wrapped pagination elements in `<nav role="navigation" aria-label="Pagination">`.
  - Back/forward buttons declare descriptive labels (`aria-label="Go to previous page"`).
  - Page buttons designate their active state clearly (`aria-current={isActive ? "page" : undefined}`).
