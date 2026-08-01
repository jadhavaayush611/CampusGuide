import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { UpdateResourcePayload, Resource } from '../../models/resource.model';

interface UpdateResourceVariables {
  id: string;
  payload: UpdateResourcePayload;
}

export function useUpdateResource(): UseMutationResult<Resource, Error, UpdateResourceVariables> {
  const queryClient = useQueryClient();

  return useMutation<Resource, Error, UpdateResourceVariables>({
    mutationFn: ({ id, payload }: UpdateResourceVariables) => resourceSdk.updateResource(id, payload),
    onSuccess: (data, variables) => {
      queryClient.setQueryData(queryKeys.resources.detail(variables.id), data);
      queryClient.invalidateQueries({ queryKey: queryKeys.resources.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.campus.resources() });
    },
  });
}
