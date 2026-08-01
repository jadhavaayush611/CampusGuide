import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { AtlasCapabilityResponse } from '../../models/atlas.model';

export function useAtlasCapabilities(): UseQueryResult<AtlasCapabilityResponse, Error> {
  return useQuery<AtlasCapabilityResponse, Error>({
    queryKey: queryKeys.atlas.capabilities(),
    queryFn: () => atlasClient.getCapabilities(),
    staleTime: 1000 * 60 * 10, // 10 minutes cache
  });
}
