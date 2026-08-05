import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource, PaginatedResourcesResponse } from '../../models/resource.model';
import { queryClient } from '../../core/query/queryClient';

export function useResourceDetails(id: string): UseQueryResult<Resource, Error> {
  return useQuery<Resource, Error>({
    queryKey: queryKeys.resources.detail(id),
    queryFn: () => resourceSdk.getResourceById(id),
    enabled: Boolean(id),
    staleTime: 1000 * 60 * 5,
    placeholderData: () => {
      if (!id) return undefined;
      const queries = queryClient.getQueriesData<PaginatedResourcesResponse | Resource[]>({
        queryKey: queryKeys.resources.all,
      });
      for (const [, data] of queries) {
        if (!data) continue;
        if (Array.isArray(data)) {
          const match = data.find((r) => r && r.id === id);
          if (match) return match;
        } else if ('resources' in data && Array.isArray(data.resources)) {
          const match = data.resources.find((r) => r && r.id === id);
          if (match) return match;
        }
      }
      return undefined;
    },
  });
}
