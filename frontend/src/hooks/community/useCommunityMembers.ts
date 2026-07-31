import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk, CommunityMembersQueryParams, PaginatedMembersResponse } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useCommunityMembers(
  communityId: string,
  params?: CommunityMembersQueryParams
): UseQueryResult<PaginatedMembersResponse, Error> {
  return useQuery({
    queryKey: queryKeys.communities.members(communityId, params),
    queryFn: () => communitySdk.getCommunityMembers(communityId, params),
    enabled: Boolean(communityId),
    staleTime: 1000 * 60 * 5,
    placeholderData: (prev) => prev,
  });
}
