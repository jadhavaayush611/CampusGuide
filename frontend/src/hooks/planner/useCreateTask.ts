import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CreateTaskDto } from '../../sdk/planner/planner.dto';
import { PlannerTask } from '../../models/planner.model';

export function useCreateTask() {
  const queryClient = useQueryClient();

  return useMutation<PlannerTask, Error, CreateTaskDto>({
    mutationFn: (payload) => plannerSdk.createTask(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.all });
    },
  });
}
