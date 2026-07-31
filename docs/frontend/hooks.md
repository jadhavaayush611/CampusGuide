# Shared Custom Hooks Conventions

## Overview

All component-level data fetching and mutations in CampusGuide are exposed through custom React hooks located in `src/hooks/`. These hooks wrap SDK operations inside React Query primitives (`useQuery`, `useMutation`), ensuring consistent caching, optimistic UI updates, and error handling.

---

## Directory Organization

```
src/hooks/
├── common/
│   └── useOptimisticMutation.ts # Reusable mutation helper with cache rollback & toast alerts
├── auth/
│   ├── useCurrentUser.ts
│   ├── useLogin.ts
│   ├── useRegister.ts
│   ├── useLogout.ts
│   └── useUpdateProfile.ts
├── campus/
│   ├── useBuildings.ts
│   ├── useBuildingDetails.ts
│   ├── useLocations.ts
│   ├── useCampusEvents.ts
│   ├── useEventDetails.ts
│   ├── useCreateEvent.ts
│   ├── useRegisterForEvent.ts
│   ├── useCouncils.ts
│   └── useResources.ts
├── planner/
│   ├── useSchedules.ts
│   ├── useScheduleDetails.ts
│   ├── useCreateSchedule.ts
│   ├── useUpdateSchedule.ts
│   ├── useCourses.ts
│   ├── useTimetable.ts
│   ├── useStudyGoals.ts
│   └── useCreateStudyGoal.ts
├── atlas/
│   ├── useAtlasSearch.ts
│   ├── useRouteCalculation.ts
│   ├── useLandmarkDetails.ts
│   └── useMapLayers.ts
└── index.ts                     # Central hooks export
```

---

## Hook Writing Conventions

1. **Query Hooks**:
   - Must use centralized query keys from `queryKeys` (`src/sdk/queryKeys.ts`).
   - Must invoke domain SDK singletons (`campusSdk`, `authSdk`, `plannerSdk`, `atlasSdk`).
   - Must return strongly typed `UseQueryResult<TData, Error>`.

2. **Mutation Hooks**:
   - Must use `useOptimisticMutation` for write operations.
   - Must declare `invalidateQueryKeys` to keep server state in sync.
   - Optionally declare `targetQueryKey` and `updateCacheOptimistically` for instant UI responsiveness.
   - Supply `successMessage` and `errorMessage` for standardized toast feedback.

---

## Reusable Mutation Pattern (`useOptimisticMutation`)

`useOptimisticMutation` standardizes state mutation behavior across the application:

```typescript
export function useRegisterForEvent(): UseMutationResult<CampusEvent, Error, RegisterEventVariables> {
  return useOptimisticMutation<CampusEvent, RegisterEventVariables>({
    mutationFn: ({ eventId, register }) =>
      register ? campusSdk.registerForEvent(eventId) : campusSdk.cancelEventRegistration(eventId),
    invalidateQueryKeys: [queryKeys.campus.events()],
    targetQueryKey: queryKeys.campus.events(),
    updateCacheOptimistically: (oldEvents: CampusEvent[], { eventId, register }) => {
      if (!Array.isArray(oldEvents)) return oldEvents;
      return oldEvents.map((evt) => {
        if (evt.id === eventId) {
          return {
            ...evt,
            isRegistered: register,
            attendeeCount: register ? evt.attendeeCount + 1 : Math.max(0, evt.attendeeCount - 1),
          };
        }
        return evt;
      });
    },
    successMessage: (_, { register }) =>
      register ? 'Registered for event successfully!' : 'Registration cancelled.',
    errorMessage: 'Failed to update event registration.',
  });
}
```

### Key Workflow of `useOptimisticMutation`:
1. `onMutate`: Cancels pending queries for `targetQueryKey`, snapshots `previousData`, and applies `updateCacheOptimistically`.
2. `onError`: Restores `previousData` automatically if the server responds with an error, and displays a toast error alert.
3. `onSuccess`: Invalidates query keys listed in `invalidateQueryKeys` and displays a toast success alert.
4. `onSettled`: Re-validates `targetQueryKey` to guarantee client-server consistency.
