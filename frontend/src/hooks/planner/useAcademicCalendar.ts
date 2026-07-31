import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { AcademicCalendarItem } from '../../models/planner.model';

export function useAcademicCalendar(term?: string): UseQueryResult<AcademicCalendarItem[], Error> {
  return useQuery<AcademicCalendarItem[], Error>({
    queryKey: queryKeys.planner.academicCalendar(term),
    queryFn: () => plannerSdk.getAcademicCalendar(term),
    staleTime: 10 * 60 * 1000,
  });
}
