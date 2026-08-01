import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';

export function useDeleteConversation(): UseMutationResult<
  void,
  Error,
  string
> {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id) => atlasClient.conversations.delete(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations.all });
      queryClient.removeQueries({ queryKey: queryKeys.conversations.detail(id) });
    },
  });
}
