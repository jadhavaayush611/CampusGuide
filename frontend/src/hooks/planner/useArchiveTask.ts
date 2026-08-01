import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { PlannerTask } from '../../models/planner.model';

export function useArchiveTask() {
  const queryClient = useQueryClient();

  return useMutation<PlannerTask, Error, string>({
    mutationFn: (id) => plannerSdk.archiveTask(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.all });
    },
  });
}
