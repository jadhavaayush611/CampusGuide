import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useDeleteStudyGoal() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id) => plannerSdk.deleteStudyGoal(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.studyGoals() });
    },
  });
}
