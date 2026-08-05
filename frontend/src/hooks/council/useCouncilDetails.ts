import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { councilSdk, PaginatedCouncilsResponse } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Council } from '../../models/council.model';
import { queryClient } from '../../core/query/queryClient';

export function useCouncilDetails(councilId: string | undefined): UseQueryResult<Council, Error> {
  return useQuery<Council, Error>({
    queryKey: queryKeys.councils.detail(councilId || ''),
    queryFn: () => councilSdk.getCouncilById(councilId!),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
    placeholderData: () => {
      if (!councilId) return undefined;
      const queries = queryClient.getQueriesData<PaginatedCouncilsResponse | Council[]>({
        queryKey: queryKeys.councils.all,
      });
      for (const [, data] of queries) {
        if (!data) continue;
        if (Array.isArray(data)) {
          const match = data.find((c) => c && c.id === councilId);
          if (match) return match;
        } else if ('councils' in data && Array.isArray(data.councils)) {
          const match = data.councils.find((c) => c && c.id === councilId);
          if (match) return match;
        }
      }
      return undefined;
    },
  });
}
