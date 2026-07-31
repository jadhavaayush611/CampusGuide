import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Schedule } from '../../models/planner.model';

export function useScheduleDetails(id: string): UseQueryResult<Schedule, Error> {
  return useQuery<Schedule, Error>({
    queryKey: queryKeys.planner.schedule(id),
    queryFn: () => plannerSdk.getScheduleById(id),
    enabled: Boolean(id),
  });
}
