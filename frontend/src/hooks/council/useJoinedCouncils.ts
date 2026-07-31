import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Council } from '../../models/council.model';

export function useJoinedCouncils() {
  return useQuery<Council[], Error>({
    queryKey: queryKeys.councils.joined(),
    queryFn: () => councilSdk.getJoinedCouncils(),
    staleTime: 5 * 60 * 1000,
  });
}
