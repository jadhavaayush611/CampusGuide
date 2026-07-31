import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { DegreePlan } from '../../models/planner.model';

export function useDegreePlan(): UseQueryResult<DegreePlan, Error> {
  return useQuery<DegreePlan, Error>({
    queryKey: queryKeys.planner.degreePlan(),
    queryFn: () => plannerSdk.getDegreePlan(),
    staleTime: 10 * 60 * 1000,
  });
}
