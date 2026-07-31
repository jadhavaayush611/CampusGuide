import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Location } from '../../models/campus.model';

export function useLocations(buildingId?: string): UseQueryResult<Location[], Error> {
  return useQuery<Location[], Error>({
    queryKey: queryKeys.campus.locations(buildingId),
    queryFn: () => campusSdk.getLocations(buildingId),
  });
}
