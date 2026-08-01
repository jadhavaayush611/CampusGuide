import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { ConversationSummaryResponse } from '../../models/atlas.model';

export function useConversationSummary(
  id: string | null | undefined
): UseQueryResult<ConversationSummaryResponse, Error> {
  return useQuery<ConversationSummaryResponse, Error>({
    queryKey: queryKeys.conversations.summary(id || ''),
    queryFn: () => atlasClient.conversations.getSummary(id!),
    enabled: Boolean(id),
    staleTime: 1000 * 60 * 5,
  });
}
