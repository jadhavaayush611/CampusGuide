import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CampusEvent } from '../../models/campus.model';
import { queryClient } from '../../core/query/queryClient';

export function useEventDetails(eventId: string): UseQueryResult<CampusEvent, Error> {
  return useQuery<CampusEvent, Error>({
    queryKey: queryKeys.campus.event(eventId),
    queryFn: () => campusSdk.getEventById(eventId),
    enabled: Boolean(eventId),
    staleTime: 5 * 60 * 1000,
    placeholderData: () => {
      if (!eventId) return undefined;
      const queries = queryClient.getQueriesData<CampusEvent[]>({
        queryKey: queryKeys.campus.events(),
      });
      for (const [, data] of queries) {
        if (!data || !Array.isArray(data)) continue;
        const match = data.find((e) => e && e.id === eventId);
        if (match) return match;
      }
      return undefined;
    },
  });
}
