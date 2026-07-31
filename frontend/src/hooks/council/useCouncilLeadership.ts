import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CouncilLeadershipMember } from '../../models/council.model';

export function useCouncilLeadership(councilId: string | undefined) {
  return useQuery<CouncilLeadershipMember[], Error>({
    queryKey: queryKeys.councils.leadership(councilId || ''),
    queryFn: () => councilSdk.getCouncilLeadership(councilId!),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
  });
}
