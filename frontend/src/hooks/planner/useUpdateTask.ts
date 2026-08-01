import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { UpdateTaskDto } from '../../sdk/planner/planner.dto';
import { PlannerTask } from '../../models/planner.model';

export function useUpdateTask() {
  const queryClient = useQueryClient();

  return useMutation<PlannerTask, Error, { id: string; payload: UpdateTaskDto }>({
    mutationFn: ({ id, payload }) => plannerSdk.updateTask(id, payload),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.task(variables.id) });
    },
  });
}
