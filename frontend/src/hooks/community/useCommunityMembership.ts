import { UseMutationResult, useQueryClient } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Community } from '../../models/community.model';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export interface MembershipMutationVariables {
  communityId: string;
  action: 'join' | 'leave';
}

export interface MembershipMutationResult {
  success: boolean;
  isJoined: boolean;
}

export function useCommunityMembership(): UseMutationResult<
  MembershipMutationResult,
  Error,
  MembershipMutationVariables
> {
  const queryClient = useQueryClient();

  return useOptimisticMutation<MembershipMutationResult, MembershipMutationVariables>({
    mutationFn: ({ communityId, action }) =>
      action === 'join'
        ? communitySdk.joinCommunity(communityId)
        : communitySdk.leaveCommunity(communityId),

    invalidateQueryKeys: [
      queryKeys.communities.joined(),
      queryKeys.communities.all,
    ],

    targetQueryKey: queryKeys.communities.all,

    updateCacheOptimistically: (_oldData, { communityId, action }) => {
      const isJoining = action === 'join';

      // 1. Optimistically update community detail query
      queryClient.setQueryData<Community>(
        queryKeys.communities.detail(communityId),
        (oldDetail) => {
          if (!oldDetail) return oldDetail;
          return {
            ...oldDetail,
            isJoined: isJoining,
            myRole: isJoining ? 'MEMBER' : 'NONE',
            memberCount: isJoining
              ? oldDetail.memberCount + 1
              : Math.max(0, oldDetail.memberCount - 1),
          };
        }
      );

      // 2. Optimistically update joined communities list
      queryClient.setQueryData<Community[]>(queryKeys.communities.joined(), (oldJoined) => {
        if (!Array.isArray(oldJoined)) return oldJoined;
        if (isJoining) {
          const detail = queryClient.getQueryData<Community>(
            queryKeys.communities.detail(communityId)
          );
          if (detail && !oldJoined.some((c) => c.id === communityId)) {
            return [...oldJoined, { ...detail, isJoined: true, myRole: 'MEMBER' }];
          }
          return oldJoined;
        } else {
          return oldJoined.filter((c) => c.id !== communityId);
        }
      });
    },

    successMessage: (_, { action }) =>
      action === 'join' ? 'Joined community successfully!' : 'Left community.',
    errorMessage: (_, { action }) =>
      action === 'join' ? 'Failed to join community.' : 'Failed to leave community.',
  });
}
