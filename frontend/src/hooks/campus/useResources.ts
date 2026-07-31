import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource } from '../../models/campus.model';

export function useResources(searchQuery = ''): UseQueryResult<Resource[], Error> {
  return useQuery<Resource[], Error>({
    queryKey: searchQuery ? queryKeys.campus.searchResources(searchQuery) : queryKeys.campus.resources(),
    queryFn: () => (searchQuery ? campusSdk.searchResources(searchQuery) : campusSdk.getResources()),
  });
}
