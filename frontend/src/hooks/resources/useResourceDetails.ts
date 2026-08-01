import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource } from '../../models/resource.model';

export function useResourceDetails(id: string): UseQueryResult<Resource, Error> {
  return useQuery<Resource, Error>({
    queryKey: queryKeys.resources.detail(id),
    queryFn: () => resourceSdk.getResourceById(id),
    enabled: Boolean(id),
    staleTime: 1000 * 60 * 5,
  });
}
