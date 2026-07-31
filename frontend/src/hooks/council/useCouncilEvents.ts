import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CampusEvent } from '../../models/campus.model';

export function useCouncilEvents(councilId: string | undefined) {
  return useQuery<CampusEvent[], Error>({
    queryKey: queryKeys.councils.events(councilId || ''),
    queryFn: () => councilSdk.getCouncilEvents(councilId!),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
  });
}
