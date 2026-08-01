import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { ConversationHistoryResponse } from '../../models/atlas.model';

export function useConversationHistory(
  id: string | null | undefined
): UseQueryResult<ConversationHistoryResponse, Error> {
  return useQuery<ConversationHistoryResponse, Error>({
    queryKey: queryKeys.conversations.history(id || ''),
    queryFn: async () => {
      const response = await atlasClient.conversations.getHistory(id!);
      if (Array.isArray(response)) {
        return {
          conversationId: id!,
          userId: 'user',
          messages: response,
          totalMessages: response.length,
        };
      }
      return response;
    },
    enabled: Boolean(id),
    staleTime: 1000 * 15,
  });
}
