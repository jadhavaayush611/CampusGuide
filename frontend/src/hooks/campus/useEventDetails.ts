import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CampusEvent } from '../../models/campus.model';

export function useEventDetails(eventId: string): UseQueryResult<CampusEvent, Error> {
  return useQuery<CampusEvent, Error>({
    queryKey: queryKeys.campus.event(eventId),
    queryFn: () => campusSdk.getEventById(eventId),
    enabled: Boolean(eventId),
  });
}
