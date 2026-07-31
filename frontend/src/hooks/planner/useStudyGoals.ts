import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { StudyGoal } from '../../models/planner.model';

export function useStudyGoals(): UseQueryResult<StudyGoal[], Error> {
  return useQuery<StudyGoal[], Error>({
    queryKey: queryKeys.planner.studyGoals(),
    queryFn: () => plannerSdk.getStudyGoals(),
  });
}
