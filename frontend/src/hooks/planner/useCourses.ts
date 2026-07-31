import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Course } from '../../models/planner.model';

export function useCourses(department?: string): UseQueryResult<Course[], Error> {
  return useQuery<Course[], Error>({
    queryKey: queryKeys.planner.courses(department),
    queryFn: () => plannerSdk.getCourses(department),
    staleTime: 10 * 60 * 1000,
  });
}
