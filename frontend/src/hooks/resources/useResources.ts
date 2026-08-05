import { useQuery, keepPreviousData, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { ResourceQueryParams, PaginatedResourcesResponse } from '../../models/resource.model';

export function useResources(params?: ResourceQueryParams): UseQueryResult<PaginatedResourcesResponse, Error> {
  return useQuery<PaginatedResourcesResponse, Error>({
    queryKey: queryKeys.resources.list(params),
    queryFn: () => resourceSdk.getResources(params),
    staleTime: 1000 * 60 * 5, // 5 minutes cache reuse
    placeholderData: keepPreviousData,
  });
}
