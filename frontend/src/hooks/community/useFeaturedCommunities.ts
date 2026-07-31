import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';

export function useFeaturedCommunities(): UseQueryResult<Community[], Error> {
  return useQuery({
    queryKey: queryKeys.communities.featured(),
    queryFn: () => communitySdk.getFeaturedCommunities(),
    staleTime: 1000 * 60 * 5,
  });
}

export function useTrendingCommunities(): UseQueryResult<Community[], Error> {
  return useQuery({
    queryKey: queryKeys.communities.trending(),
    queryFn: () => communitySdk.getTrendingCommunities(),
    staleTime: 1000 * 60 * 5,
  });
}

export function useRecentlyActiveCommunities(): UseQueryResult<Community[], Error> {
  return useQuery({
    queryKey: queryKeys.communities.recentlyActive(),
    queryFn: () => communitySdk.getRecentlyActiveCommunities(),
    staleTime: 1000 * 60 * 5,
  });
}
