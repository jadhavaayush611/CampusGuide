import { useQuery } from '@tanstack/react-query';
import { councilSdk, CouncilMembersQueryParams, PaginatedCouncilMembersResponse } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useCouncilMembers(councilId: string | undefined, params?: CouncilMembersQueryParams) {
  return useQuery<PaginatedCouncilMembersResponse, Error>({
    queryKey: queryKeys.councils.members(councilId || '', params),
    queryFn: () => councilSdk.getCouncilMembers(councilId!, params),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
  });
}
