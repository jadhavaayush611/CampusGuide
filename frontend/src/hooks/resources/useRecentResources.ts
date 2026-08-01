import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource } from '../../models/resource.model';

export function useRecentResources(): UseQueryResult<Resource[], Error> {
  return useQuery<Resource[], Error>({
    queryKey: queryKeys.resources.recent(),
    queryFn: () => resourceSdk.getRecentResources(),
    staleTime: 1000 * 60 * 2,
  });
}
