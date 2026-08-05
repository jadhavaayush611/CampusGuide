# Frontend Network, Caching & Asset Delivery Performance Strategy

## 1. Overview & Architecture

CampusGuide frontend performance is engineered for high responsiveness, zero redundant network requests, predictable caching lifecycles, and fast route navigation.

---

## 2. React Query Strategy

All server state is managed through TanStack React Query (`@tanstack/react-query`).

### Global Configuration Defaults
- `staleTime`: 5 minutes (`300,000 ms`) by default across data queries to prevent unnecessary refetches while keeping data fresh.
- `gcTime`: 10 minutes (`600,000 ms`) for garbage collection of unused query caches.
- `refetchOnWindowFocus`: `false` (disabled to prevent disruptive background refetches on tab switching).
- `refetchOnReconnect`: `true` (automatically synchronizes state after network recovery).
- `refetchOnMount`: `true` (only triggers background updates on mount if the data is stale, avoiding requests if data is fresh).
- `retry`: Custom `queryRetryPolicy` — retries up to 3 times for transient 5xx server errors / timeouts, and does **not** retry 4xx client errors (400, 401, 403, 404).

---

## 3. Caching Lifecycle & Entity Cache Reuse

To eliminate duplicate requests and waterfall spinners when navigating from list views to detail views, the application leverages React Query's `placeholderData` lookup mechanism:

- **Community List → Community Detail**: `useCommunityDetails` inspects `queryKeys.communities.all` queries (list, featured, joined) to extract the entity immediately without waiting for a detail query to resolve.
- **Council List → Council Detail**: `useCouncilDetails` reuses entities cached from `queryKeys.councils.all`.
- **Resource List → Resource Detail**: `useResourceDetails` reuses items cached from `queryKeys.resources.all`.
- **Notice List → Notice Detail**: `useNoticeDetails` reuses items cached from `queryKeys.notices.all`.
- **Task List → Task Detail**: `useTask` reuses items cached from `queryKeys.planner.all`.
- **Event List → Event Detail**: `useEventDetails` reuses items cached from `queryKeys.campus.events()`.

### Pagination & Parameter Changes
All paginated and filtered queries (`useCommunities`, `useCouncils`, `useResources`, `useNotices`, `useTasks`, `useNotifications`, `useAtlasSearch`) utilize `placeholderData: keepPreviousData`. This keeps the existing dataset visible while fetching updated results, preventing UI flicker and layout shifts.

---

## 4. Request Flow & Waterfall Elimination

1. **Parallel Execution**: Independent hooks across pages (e.g. Dashboard widgets, Academic page modules) execute query functions in parallel using standard `useQuery` declarations or `Promise.all` inside SDK layer handlers.
2. **SDK Deduplication**: SDK modules (e.g. `NoticeSdk`, `PlannerSdk`) maintain in-memory memoization for repeated operations (such as notice read status sets and study goal local storage parsing), eliminating redundant parsing overhead.
3. **Zero Duplicated Requests**: React Query's central query key hierarchy (`queryKeys`) guarantees that identical queries share in-flight promises and cached state.

---

## 5. Route & Data Prefetching Strategy

Route prefetching is integrated via `prefetchRoute(path)` in `frontend/src/core/routing/routePrefetch.ts`:

- **Trigger Points**: `Sidebar` navigation links, Dashboard quick-action buttons, and Atlas result links trigger prefetching on `onMouseEnter` and `onFocus`.
- **Prefetched Assets**:
  1. **Lazy Route Bundles**: Invokes dynamic `import()` to load page component JavaScript chunks prior to click.
  2. **Lightweight React Query Data**: Warms up lightweight initial queries (e.g., recent notices, featured communities, user enrolled courses, upcoming events) into query cache with explicit `staleTime`.

---

## 6. Static Asset Loading & Image Optimization

- **Native Lazy Loading**: All images across cards, headers, avatars, attachments, and previews specify `loading="lazy"`.
- **Asynchronous Decoding**: Images specify `decoding="async"` to prevent main-thread decoding bottlenecks during page renders.
- **Graceful Fallbacks**: Component templates render custom initial badges or icon fallbacks when image URLs fail or are omitted.

---

## 7. Font Optimization

- **Font Family**: Primary font stack uses `'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif`.
- **Display Strategy**: Configured with `font-display: swap` to eliminate Flash of Unseen Text (FOIT).
- **Preconnect & Preload**: `index.html` includes `<link rel="preconnect" href="https://fonts.googleapis.com">` and preloads the font stylesheet to accelerate font rendering and minimize Cumulative Layout Shift (CLS).

---

## 8. Production Asset Delivery & Hosting Recommendations

When deploying the production build (`dist/`), reverse proxies (Nginx / Cloudflare / AWS CloudFront) should enforce the following caching and delivery header policies:

### Immutable Hashed Assets (`dist/assets/*.js`, `dist/assets/*.css`)
Vite generates content-hashed filenames for JavaScript and CSS bundles.
```http
Cache-Control: public, max-age=31536000, immutable
```

### HTML Document (`dist/index.html`)
HTML documents must not be cached aggressively so users immediately receive updated bundle references.
```http
Cache-Control: no-cache, no-store, must-revalidate
```

### Compression
- **Brotli (`br`)**: Enabled for text assets (`.html`, `.js`, `.css`, `.svg`, `.json`).
- **Gzip (`gzip`)**: Fallback for clients without Brotli support.

### CDN & Edge Caching
- Edge servers should serve cached static assets directly from edge POPs without origin revalidation for hashed static files.
