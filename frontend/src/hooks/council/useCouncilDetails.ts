import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Council } from '../../models/council.model';

export function useCouncilDetails(councilId: string | undefined) {
  return useQuery<Council, Error>({
    queryKey: queryKeys.councils.detail(councilId || ''),
    queryFn: () => councilSdk.getCouncilById(councilId!),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
  });
}
