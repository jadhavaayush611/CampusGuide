import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource } from '../../models/resource.model';

export function useFeaturedResources(): UseQueryResult<Resource[], Error> {
  return useQuery<Resource[], Error>({
    queryKey: queryKeys.resources.featured(),
    queryFn: () => resourceSdk.getFeaturedResources(),
    staleTime: 1000 * 60 * 5,
  });
}
