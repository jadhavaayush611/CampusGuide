import { useQuery } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CouncilNotice } from '../../models/council.model';

export function useCouncilNotices(councilId: string | undefined, filter?: string) {
  return useQuery<CouncilNotice[], Error>({
    queryKey: queryKeys.councils.notices(councilId || '', filter),
    queryFn: () => councilSdk.getCouncilNotices(councilId!, filter),
    enabled: Boolean(councilId),
    staleTime: 5 * 60 * 1000,
  });
}
