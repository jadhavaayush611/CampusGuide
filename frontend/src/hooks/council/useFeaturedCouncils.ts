import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Council } from '../../models/council.model';

export function useFeaturedCouncils() {
  return useQuery<Council[], Error>({
    queryKey: queryKeys.councils.featured(),
    queryFn: () => councilSdk.getFeaturedCouncils(),
    staleTime: 5 * 60 * 1000,
  });
}
