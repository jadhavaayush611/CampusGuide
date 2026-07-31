import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Building } from '../../models/campus.model';

export function useBuildings(): UseQueryResult<Building[], Error> {
  return useQuery<Building[], Error>({
    queryKey: queryKeys.campus.buildings(),
    queryFn: () => campusSdk.getBuildings(),
    staleTime: 15 * 60 * 1000, // Buildings rarely change (15 mins)
  });
}
