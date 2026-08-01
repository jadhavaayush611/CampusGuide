import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CreateResourcePayload, Resource } from '../../models/resource.model';

export function useCreateResource(): UseMutationResult<Resource, Error, CreateResourcePayload> {
  const queryClient = useQueryClient();

  return useMutation<Resource, Error, CreateResourcePayload>({
    mutationFn: (payload: CreateResourcePayload) => resourceSdk.createResource(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.resources.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.campus.resources() });
    },
  });
}
