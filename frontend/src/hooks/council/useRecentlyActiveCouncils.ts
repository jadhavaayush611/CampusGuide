import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Council } from '../../models/council.model';

export function useRecentlyActiveCouncils() {
  return useQuery<Council[], Error>({
    queryKey: queryKeys.councils.recentlyActive(),
    queryFn: () => councilSdk.getRecentlyActiveCouncils(),
    staleTime: 5 * 60 * 1000,
  });
}
