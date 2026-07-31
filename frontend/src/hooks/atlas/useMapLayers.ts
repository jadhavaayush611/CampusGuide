import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasSdk } from '../../sdk/atlas/AtlasSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { MapLayer } from '../../models/atlas.model';

export function useMapLayers(): UseQueryResult<MapLayer[], Error> {
  return useQuery<MapLayer[], Error>({
    queryKey: queryKeys.atlas.mapLayers(),
    queryFn: () => atlasSdk.getMapLayers(),
    staleTime: 30 * 60 * 1000,
  });
}
