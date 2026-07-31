import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';

export function useJoinedCommunities(): UseQueryResult<Community[], Error> {
  return useQuery({
    queryKey: queryKeys.communities.joined(),
    queryFn: () => communitySdk.getJoinedCommunities(),
    staleTime: 1000 * 60 * 2,
  });
}
