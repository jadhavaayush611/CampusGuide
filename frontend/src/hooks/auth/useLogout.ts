import { UseMutationResult, useQueryClient } from '@tanstack/react-query';
import { authSdk } from '../../sdk/auth/AuthSdk';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export function useLogout(): UseMutationResult<void, Error, void> {
  const queryClient = useQueryClient();

  return useOptimisticMutation<void, void>({
    mutationFn: () => authSdk.logout(),
    successMessage: 'Signed out successfully',
    options: {
      onSuccess: () => {
        // Clear entire React Query cache on logout
        queryClient.clear();
      },
    },
  });
}
