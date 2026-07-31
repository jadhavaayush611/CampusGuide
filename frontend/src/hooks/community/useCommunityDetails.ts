import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';

export function useCommunityDetails(communityId: string): UseQueryResult<Community, Error> {
  return useQuery({
    queryKey: queryKeys.communities.detail(communityId),
    queryFn: () => communitySdk.getCommunityById(communityId),
    enabled: Boolean(communityId),
    staleTime: 1000 * 60 * 5,
  });
}
