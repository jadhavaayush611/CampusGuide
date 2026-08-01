import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { AtlasConversation } from '../../models/atlas.model';

export function useArchiveConversation(): UseMutationResult<
  AtlasConversation,
  Error,
  string
> {
  const queryClient = useQueryClient();

  return useMutation<AtlasConversation, Error, string>({
    mutationFn: (id) => atlasClient.conversations.archive(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations.detail(id) });
    },
  });
}
