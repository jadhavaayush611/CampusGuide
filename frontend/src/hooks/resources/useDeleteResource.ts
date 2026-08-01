import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useDeleteResource(): UseMutationResult<void, Error, string> {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id: string) => resourceSdk.deleteResource(id),
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: queryKeys.resources.detail(id) });
      queryClient.invalidateQueries({ queryKey: queryKeys.resources.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.campus.resources() });
    },
  });
}
