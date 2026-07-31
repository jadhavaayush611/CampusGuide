import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CampusEvent } from '../../models/campus.model';

export function useCampusEvents(upcomingOnly = false): UseQueryResult<CampusEvent[], Error> {
  return useQuery<CampusEvent[], Error>({
    queryKey: upcomingOnly ? queryKeys.campus.upcomingEvents() : queryKeys.campus.events(),
    queryFn: () => (upcomingOnly ? campusSdk.getUpcomingEvents() : campusSdk.getEvents()),
    staleTime: 2 * 60 * 1000, // Events refresh faster (2 mins)
  });
}
