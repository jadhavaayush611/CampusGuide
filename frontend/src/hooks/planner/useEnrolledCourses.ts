import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Course } from '../../models/planner.model';

export function useEnrolledCourses(): UseQueryResult<Course[], Error> {
  return useQuery<Course[], Error>({
    queryKey: queryKeys.planner.courses(),
    queryFn: () => plannerSdk.getCourses(),
    select: (courses) => courses.filter((c) => c.status === 'ENROLLED' || c.status === 'IN_PROGRESS'),
    staleTime: 5 * 60 * 1000,
  });
}
