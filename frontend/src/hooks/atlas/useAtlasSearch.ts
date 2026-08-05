import { useQuery, keepPreviousData, UseQueryResult } from '@tanstack/react-query';
import { atlasSdk } from '../../sdk/atlas/AtlasSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { SpatialSearchResult } from '../../models/atlas.model';

export interface UseAtlasSearchOptions {
  query: string;
  category?: string;
  userLat?: number;
  userLng?: number;
  enabled?: boolean;
}

export function useAtlasSearch({
  query,
  category,
  userLat,
  userLng,
  enabled = true,
}: UseAtlasSearchOptions): UseQueryResult<SpatialSearchResult[], Error> {
  return useQuery<SpatialSearchResult[], Error>({
    queryKey: queryKeys.atlas.search(query, category),
    queryFn: () => atlasSdk.searchSpatial(query, category, userLat, userLng),
    enabled: enabled && query.trim().length > 0,
    staleTime: 1 * 60 * 1000,
    placeholderData: keepPreviousData,
  });
}
