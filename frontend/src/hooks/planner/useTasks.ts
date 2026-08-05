import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { TaskQueryParams, TaskPaginatedResponse } from '../../models/planner.model';

export function useTasks(params?: TaskQueryParams) {
  return useQuery<TaskPaginatedResponse>({
    queryKey: queryKeys.planner.tasks(params),
    queryFn: () => plannerSdk.getTasks(params),
    staleTime: 1000 * 60 * 5,
    placeholderData: keepPreviousData,
  });
}
