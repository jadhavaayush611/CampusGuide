import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import { AtlasConversation } from '../../models/atlas.model';

interface RenameParams {
  id: string;
  title: string;
}

export function useRenameConversation(): UseMutationResult<
  AtlasConversation,
  Error,
  RenameParams
> {
  const queryClient = useQueryClient();

  return useMutation<AtlasConversation, Error, RenameParams>({
    mutationFn: ({ id, title }) => atlasClient.conversations.rename(id, title),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.conversations.detail(id) });
    },
  });
}
