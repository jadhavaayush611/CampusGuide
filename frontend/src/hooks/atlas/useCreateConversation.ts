import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { AtlasConversation, ConversationCreateRequest } from '../../models/atlas.model';

export function useCreateConversation(): UseMutationResult<
  AtlasConversation,
  Error,
  ConversationCreateRequest
> {
  const queryClient = useQueryClient();

  return useMutation<AtlasConversation, Error, ConversationCreateRequest>({
    mutationFn: (payload) => atlasClient.conversations.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations.all });
    },
  });
}
