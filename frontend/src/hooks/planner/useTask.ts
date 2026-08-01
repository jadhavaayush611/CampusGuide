import { useQuery } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { PlannerTask } from '../../models/planner.model';

export function useTask(id: string) {
  return useQuery<PlannerTask>({
    queryKey: queryKeys.planner.task(id),
    queryFn: () => plannerSdk.getTaskById(id),
    enabled: !!id,
  });
}
