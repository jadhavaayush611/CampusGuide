import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource } from '../../models/resource.model';

export function useBookmarkedResources(): UseQueryResult<Resource[], Error> {
  return useQuery<Resource[], Error>({
    queryKey: queryKeys.resources.bookmarked(),
    queryFn: () => resourceSdk.getBookmarkedResources(),
    staleTime: 1000 * 60 * 2,
  });
}
