import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Schedule } from '../../models/planner.model';

export function useSchedules(): UseQueryResult<Schedule[], Error> {
  return useQuery<Schedule[], Error>({
    queryKey: queryKeys.planner.schedules(),
    queryFn: () => plannerSdk.getSchedules(),
  });
}
