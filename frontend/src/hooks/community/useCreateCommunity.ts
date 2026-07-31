import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { CreateCommunityDto } from '../../sdk/community/community.dto';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';
import { toast } from '../../core/toast/useToast';

export function useCreateCommunity(): UseMutationResult<Community, Error, CreateCommunityDto> {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateCommunityDto) => communitySdk.createCommunity(payload),
    onSuccess: (newCommunity) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.communities.all });
      toast.success(`Community "${newCommunity.name}" created successfully!`);
    },
    onError: (error) => {
      toast.error(error.message || 'Failed to create community');
    },
  });
}
