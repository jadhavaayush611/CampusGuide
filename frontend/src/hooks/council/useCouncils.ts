import { useQuery } from '@tanstack/react-query';
import { councilSdk, CouncilQueryParams, PaginatedCouncilsResponse } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useCouncils(params?: CouncilQueryParams) {
  return useQuery<PaginatedCouncilsResponse, Error>({
    queryKey: queryKeys.councils.list(params),
    queryFn: () => councilSdk.getCouncils(params),
    staleTime: 5 * 60 * 1000,
  });
}
