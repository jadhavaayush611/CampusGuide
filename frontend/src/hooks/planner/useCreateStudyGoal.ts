import { UseMutationResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { StudyGoal } from '../../models/planner.model';
import { CreateStudyGoalDto } from '../../sdk/planner/planner.dto';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export function useCreateStudyGoal(): UseMutationResult<StudyGoal, Error, CreateStudyGoalDto> {
  return useOptimisticMutation<StudyGoal, CreateStudyGoalDto>({
    mutationFn: (payload) => plannerSdk.createStudyGoal(payload),
    invalidateQueryKeys: [queryKeys.planner.studyGoals()],
    successMessage: 'Study goal added!',
    errorMessage: 'Failed to create study goal.',
  });
}
