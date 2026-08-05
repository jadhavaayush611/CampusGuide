import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk, PaginatedCommunitiesResponse } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';
import { queryClient } from '../../core/query/queryClient';

export function useCommunityDetails(communityId: string): UseQueryResult<Community, Error> {
  return useQuery({
    queryKey: queryKeys.communities.detail(communityId),
    queryFn: () => communitySdk.getCommunityById(communityId),
    enabled: Boolean(communityId),
    staleTime: 1000 * 60 * 5,
    placeholderData: () => {
      if (!communityId) return undefined;
      const queries = queryClient.getQueriesData<PaginatedCommunitiesResponse | Community[]>({
        queryKey: queryKeys.communities.all,
      });
      for (const [, data] of queries) {
        if (!data) continue;
        if (Array.isArray(data)) {
          const match = data.find((c) => c && c.id === communityId);
          if (match) return match;
        } else if ('communities' in data && Array.isArray(data.communities)) {
          const match = data.communities.find((c) => c && c.id === communityId);
          if (match) return match;
        }
      }
      return undefined;
    },
  });
}
