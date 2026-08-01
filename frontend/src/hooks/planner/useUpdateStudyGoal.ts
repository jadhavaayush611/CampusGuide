import { useMutation, useQueryClient } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CreateStudyGoalDto } from '../../sdk/planner/planner.dto';
import { StudyGoal } from '../../models/planner.model';

export function useUpdateStudyGoal() {
  const queryClient = useQueryClient();

  return useMutation<
    StudyGoal,
    Error,
    { id: string; payload: Partial<CreateStudyGoalDto> & { completedHours?: number; isCompleted?: boolean } }
  >({
    mutationFn: ({ id, payload }) => plannerSdk.updateStudyGoal(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.planner.studyGoals() });
    },
  });
}
