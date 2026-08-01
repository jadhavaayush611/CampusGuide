import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { PlannerTask } from '../../models/planner.model';

export function useMarkTaskComplete() {
  const queryClient = useQueryClient();

  return useMutation<PlannerTask, Error, { id: string; completed: boolean }>({
    mutationFn: ({ id, completed }) => plannerSdk.markTaskComplete(id, completed),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.all });
    },
  });
}
