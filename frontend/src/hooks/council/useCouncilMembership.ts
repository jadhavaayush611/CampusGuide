import { useQueryClient } from '@tanstack/react-query';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { Council } from '../../models/council.model';

export function useCouncilMembership(councilId: string) {
  const queryClient = useQueryClient();

  const joinMutation = useOptimisticMutation<{ success: boolean; isJoined: boolean }, string>({
    mutationFn: (id: string) => councilSdk.joinCouncil(id),
    targetQueryKey: queryKeys.councils.detail(councilId),
    updateCacheOptimistically: (oldData: Council | undefined) => {
      if (!oldData) return oldData;
      return {
        ...oldData,
        isJoined: true,
        pendingJoinRequest: false,
        memberCount: (oldData.memberCount || 0) + 1,
        myRole: 'MEMBER',
      };
    },
    invalidateQueryKeys: [queryKeys.councils.joined(), queryKeys.councils.all],
    successMessage: 'Successfully joined council!',
    errorMessage: 'Failed to join council. Please try again.',
  });

  const leaveMutation = useOptimisticMutation<{ success: boolean; isJoined: boolean }, string>({
    mutationFn: (id: string) => councilSdk.leaveCouncil(id),
    targetQueryKey: queryKeys.councils.detail(councilId),
    updateCacheOptimistically: (oldData: Council | undefined) => {
      if (!oldData) return oldData;
      return {
        ...oldData,
        isJoined: false,
        pendingJoinRequest: false,
        memberCount: Math.max(0, (oldData.memberCount || 0) - 1),
        myRole: 'NONE',
      };
    },
    invalidateQueryKeys: [queryKeys.councils.joined(), queryKeys.councils.all],
    successMessage: 'Successfully left council.',
    errorMessage: 'Failed to leave council. Please try again.',
  });

  return {
    join: () => joinMutation.mutate(councilId),
    leave: () => leaveMutation.mutate(councilId),
    isJoining: joinMutation.isPending,
    isLeaving: leaveMutation.isPending,
  };
}
