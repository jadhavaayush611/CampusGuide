import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasSdk } from '../../sdk/atlas/AtlasSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Landmark } from '../../models/atlas.model';

export function useLandmarkDetails(id: string): UseQueryResult<Landmark, Error> {
  return useQuery<Landmark, Error>({
    queryKey: queryKeys.atlas.landmark(id),
    queryFn: () => atlasSdk.getLandmarkById(id),
    enabled: Boolean(id),
    staleTime: 15 * 60 * 1000,
  });
}
