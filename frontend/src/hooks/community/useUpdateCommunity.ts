import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { UpdateCommunityDto } from '../../sdk/community/community.dto';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';
import { toast } from '../../core/toast/useToast';

export interface UpdateCommunityVariables {
  communityId: string;
  payload: UpdateCommunityDto;
}

export function useUpdateCommunity(): UseMutationResult<Community, Error, UpdateCommunityVariables> {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ communityId, payload }) => communitySdk.updateCommunity(communityId, payload),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.communities.detail(updated.id) });
      queryClient.invalidateQueries({ queryKey: queryKeys.communities.all });
      toast.success(`Community "${updated.name}" updated successfully!`);
    },
    onError: (error) => {
      toast.error(error.message || 'Failed to update community');
    },
  });
}
