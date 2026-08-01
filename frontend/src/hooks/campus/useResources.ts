import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource } from '../../models/resource.model';

export function useCampusResources(searchQuery = ''): UseQueryResult<Resource[], Error> {
  return useQuery<Resource[], Error>({
    queryKey: searchQuery ? queryKeys.campus.searchResources(searchQuery) : queryKeys.campus.resources(),
    queryFn: async () => {
      const res = await resourceSdk.getResources({ search: searchQuery });
      return res.resources;
    },
  });
}

export const useResources = useCampusResources;
