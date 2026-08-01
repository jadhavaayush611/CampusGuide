# Frontend Rendering & React Performance Optimization

## Overview

This document outlines the rendering performance optimization strategy for the CampusGuide frontend application. The goal of Batch 5.2 was to minimize unnecessary React component re-renders across all core modules, dashboard widgets, and shared card components while strictly preserving existing UI layout, styling, application state contracts, and SDK integrations.

---

## 1. Optimization Strategy

The performance strategy follows a surgical, targeted approach rather than wholesale memoization:

1. **Root & Context Boundary Isolation**: Prevent global state providers (such as `AuthProvider` and `LoadingProvider`) from broadcasting unnecessary re-renders down the component tree by memoizing context value references with `useMemo`.
2. **Dashboard & Widget Scoping**: Wrap self-contained dashboard widgets in `React.memo` and stabilize navigation/modal handlers with `useCallback` to isolate user interactions to individual widgets.
3. **Card & Item Memoization**: Apply `React.memo` to high-frequency list items (`ResourceCard`, `NoticeCard`, `NotificationItemCard`, `TaskCard`, `CommunityCard`, `CouncilCard`, `StudyGoalCard`, `AtlasConversationItem`) to prevent non-target items from re-rendering during parent filter or sort updates.
4. **Calculated Value Caching**: Use `useMemo` for non-trivial array derivations, date formatting, badge class computations, and timetable calculations inside render loops.
5. **Streaming UI Boundary**: Ensure high-frequency streaming events in Atlas re-render only active streaming bubbles (`MessageBubble`, `ThinkingTimeline`, `ToolExecutionPanel`) without invalidating historical conversation list items.

---

## 2. Component Memoization (`React.memo`) Decisions

`React.memo` was applied to components with stable props or those rendered inside dynamic list maps:

| Component | Rationale | Memoization Mechanism |
| :--- | :--- | :--- |
| `AuthProvider` / `LoadingProvider` | Root context value reference stabilization | `useMemo` wrapped context value objects |
| `Sidebar` / `SidebarNavItem` | App Shell navigation items re-rendering on page change | `React.memo` on items and sidebar shell |
| `Header` | Global shell top bar header | `React.memo` |
| `Dashboard` | Orchestrator page for 6 standalone widgets | `React.memo` |
| `UserOverviewWidget` | Account & profile overview | `React.memo` + `useMemo` for progress |
| `AcademicSummaryWidget` | Class timetable & course summary | `React.memo` + `useMemo` for class filter |
| `NotificationsWidget` | Header / dashboard notification preview | `React.memo` + `useMemo` for notification slice |
| `PlannerWidget` | Today tasks & degree progress | `React.memo` + `useMemo` for task filter |
| `CampusActivityWidget` | Top events, councils, resources preview | `React.memo` + `useMemo` for top slices |
| `AtlasWidget` | Quick AI assistant & building route preview | `React.memo` + `useMemo` for building slice |
| `ResourceCard` | Library resource item in grid/list views | `React.memo` + `useMemo` for file badge |
| `NoticeCard` | Campus notice bulletin card | `React.memo` + `useMemo` for priority/category badge |
| `NotificationItemCard` | Activity & alert list item | `React.memo` + `useCallback` for item action handlers |
| `TaskCard` | Planner task card (grid & list mode) | `React.memo` + `useMemo` for overdue calculation |
| `CommunityCard` | Student community card | `React.memo` + `useMemo` for category styling |
| `CouncilCard` | Student council card | `React.memo` + `useCallback` for join toggle |
| `StudyGoalCard` | Study goal progress card | `React.memo` + `useMemo` for percentage computation |
| `CampusResultCard` | Atlas deep-link result payload card | `React.memo` + `useMemo` for type badge & icon |
| `MessageBubble` | Chat conversation bubble | `React.memo` |
| `ThinkingTimeline` / `TimelineNode` | Real-time AI pipeline execution steps | `React.memo` on root & timeline nodes |
| `ToolExecutionPanel` | Capability & tool execution breakdown | `React.memo` |
| `AtlasSidebar` / `AtlasConversationItem` | Chat history conversation drawer item | `React.memo` extracted `AtlasConversationItem` |
| `MonthView` / `WeekView` / `DayView` / `AgendaView` | Calendar view mode layouts | `React.memo` + `useMemo` for date grid & day event maps |
| `ConflictIndicator` | Schedule overlap indicator badge | `React.memo` |

---

## 3. Callback Stabilization (`useCallback`)

Function identities were stabilized using `useCallback` when passed as props to memoized child components:

- **Navigation Handlers**: `handleNavigateCalendar`, `handleNavigateResources`, `handleNavigateProfile`, `handleNavigateAcademic`, `handleNavigateAll`.
- **Card Action Callbacks**: `onSelect`, `onEdit`, `onMarkComplete`, `onUpdateProgress`, `onArchive`, `onRestore`, `onDelete`, `onToggleRead`, `onToggleArchive`.
- **Modal & Form Triggers**: `handleCreateGoal`, `handleOpenModal`, `handleCloseModal`, `handleQuickRoute`.

---

## 4. React Query Selectors & Derived State

- **Server Query Caching**: React Query queryFn hooks use staleTime (1 to 5 minutes) to avoid refetching static server data on tab focus.
- **Derived Query Mappings**: Hooks like `useUpcomingDeadlines` aggregate tasks, calendar items, and study goals, sorting and categorizing them into `upcoming`, `overdue`, and `recentlyCompleted` data views.
- **Selective Consumption**: Components select only necessary slices or utilize `useMemo` on returned data arrays to avoid re-running expensive filter and sort operations during render cycles.

---

## 5. Rendering Boundaries & Context Isolation

- **Context Refactoring**: `AuthProvider` and `LoadingProvider` previously passed inline literal objects as context values (e.g. `value={{ state, login, logout }}`). This caused every consumer in the application tree to re-render whenever the provider re-rendered. Wrapping `value` in `useMemo` ensured consumer re-renders only fire when state values actually change.
- **Widget Boundaries**: Each widget on the Dashboard is wrapped in an independent `WidgetErrorBoundary` and memoized with `React.memo`. State changes or interactions within one widget (such as toggling task progress in `PlannerWidget`) do not cause sibling widgets to re-render.

---

## 6. Intentionally Avoided Optimizations

In alignment with explicit project boundaries, the following optimizations were intentionally avoided:

- **No Virtualization**: Virtual list libraries (`react-window`, `react-virtualized`) were **not** introduced to keep DOM structure clean, predictable, and fully searchable without complex layout offsets.
- **No Redesign or UI Alterations**: All components retain their exact existing markup, CSS classes, Lucide icons, responsive Tailwind breakpoints, and visual styling.
- **No Over-Memoization**: Small, inline primitive components without child elements or complex calculations were intentionally left un-memoized to avoid unnecessary React comparison overhead.
