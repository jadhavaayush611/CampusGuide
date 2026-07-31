import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk, PaginatedFeedResponse } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useCommunityFeed(
  communityId: string,
  filter?: 'all' | 'announcements' | 'pinned'
): UseQueryResult<PaginatedFeedResponse, Error> {
  return useQuery({
    queryKey: queryKeys.communities.feed(communityId, filter),
    queryFn: () => communitySdk.getCommunityPosts(communityId, { filter }),
    enabled: Boolean(communityId),
    staleTime: 1000 * 60 * 3,
  });
}
