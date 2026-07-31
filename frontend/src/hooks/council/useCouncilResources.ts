import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CouncilResource } from '../../models/council.model';

export function useCouncilResources(councilId: string | undefined, category?: string) {
  return useQuery<CouncilResource[], Error>({
    queryKey: queryKeys.councils.resources(councilId || '', category),
    queryFn: () => councilSdk.getCouncilResources(councilId!, category),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
  });
}
