import { useQuery } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { PlannerTask, TaskPaginatedResponse } from '../../models/planner.model';
import { queryClient } from '../../core/query/queryClient';

export function useTask(id: string) {
  return useQuery<PlannerTask>({
    queryKey: queryKeys.planner.task(id),
    queryFn: () => plannerSdk.getTaskById(id),
    enabled: Boolean(id),
    staleTime: 5 * 60 * 1000,
    placeholderData: () => {
      if (!id) return undefined;
      const queries = queryClient.getQueriesData<TaskPaginatedResponse | PlannerTask[]>({
        queryKey: queryKeys.planner.all,
      });
      for (const [, data] of queries) {
        if (!data) continue;
        if (Array.isArray(data)) {
          const match = data.find((t) => t && t.id === id);
          if (match) return match;
        } else if ('tasks' in data && Array.isArray(data.tasks)) {
          const match = data.tasks.find((t) => t && t.id === id);
          if (match) return match;
        }
      }
      return undefined;
    },
  });
}
