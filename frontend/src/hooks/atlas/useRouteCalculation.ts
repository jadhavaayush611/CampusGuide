import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasSdk } from '../../sdk/atlas/AtlasSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CalculatedRoute } from '../../models/atlas.model';

export interface UseRouteCalculationOptions {
  originLat: number;
  originLng: number;
  destLat: number;
  destLng: number;
  isAccessible?: boolean;
  travelMode?: 'WALKING' | 'WHEELCHAIR' | 'SHUTTLE';
  enabled?: boolean;
}

export function useRouteCalculation({
  originLat,
  originLng,
  destLat,
  destLng,
  isAccessible,
  travelMode = 'WALKING',
  enabled = true,
}: UseRouteCalculationOptions): UseQueryResult<CalculatedRoute, Error> {
  const isCoordinatesValid =
    Boolean(originLat) && Boolean(originLng) && Boolean(destLat) && Boolean(destLng);

  return useQuery<CalculatedRoute, Error>({
    queryKey: queryKeys.atlas.route(originLat, originLng, destLat, destLng, isAccessible),
    queryFn: () =>
      atlasSdk.calculateRoute({
        originLatitude: originLat,
        originLongitude: originLng,
        destinationLatitude: destLat,
        destinationLongitude: destLng,
        isAccessible,
        travelMode,
      }),
    enabled: enabled && isCoordinatesValid,
    staleTime: 5 * 60 * 1000,
  });
}
