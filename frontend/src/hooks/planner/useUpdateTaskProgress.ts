import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { PlannerTask } from '../../models/planner.model';

export function useUpdateTaskProgress() {
  const queryClient = useQueryClient();

  return useMutation<PlannerTask, Error, { id: string; progress: number }>({
    mutationFn: ({ id, progress }) => plannerSdk.updateTaskProgress(id, progress),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.all });
    },
  });
}
