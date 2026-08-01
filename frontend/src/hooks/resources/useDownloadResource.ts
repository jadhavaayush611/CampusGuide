import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';

interface DownloadVariables {
  id: string;
  fileName?: string;
}

export function useDownloadResource(): UseMutationResult<void, Error, DownloadVariables> {
  const queryClient = useQueryClient();

  return useMutation<void, Error, DownloadVariables>({
    mutationFn: ({ id, fileName }: DownloadVariables) => resourceSdk.downloadResource(id, fileName),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.resources.all });
    },
  });
}
