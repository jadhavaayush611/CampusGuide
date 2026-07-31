import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk, CommunityQueryParams, PaginatedCommunitiesResponse } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useCommunities(
  params?: CommunityQueryParams
): UseQueryResult<PaginatedCommunitiesResponse, Error> {
  return useQuery({
    queryKey: queryKeys.communities.list(params),
    queryFn: () => communitySdk.getCommunities(params),
    staleTime: 1000 * 60 * 5, // 5 minutes cache reuse
    placeholderData: (previousData) => previousData,
  });
}
