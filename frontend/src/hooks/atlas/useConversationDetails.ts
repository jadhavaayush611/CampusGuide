import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { AtlasConversation } from '../../models/atlas.model';

export function useConversationDetails(
  id: string | null | undefined
): UseQueryResult<AtlasConversation, Error> {
  return useQuery<AtlasConversation, Error>({
    queryKey: queryKeys.conversations.detail(id || ''),
    queryFn: () => atlasClient.conversations.get(id!),
    enabled: Boolean(id),
    staleTime: 1000 * 60,
  });
}
