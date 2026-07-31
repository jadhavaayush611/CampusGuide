import { UseMutationResult, useQueryClient } from '@tanstack/react-query';
import { authSdk } from '../../sdk/auth/AuthSdk';
import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { useAuth } from '../../core/auth';

export function useLogout(): UseMutationResult<void, Error, void> {
  const queryClient = useQueryClient();
  const { logout } = useAuth();

  return useOptimisticMutation<void, void>({
    mutationFn: () => authSdk.logout(),
    successMessage: 'Signed out successfully',
    options: {
      onSuccess: () => {
        logout();
        queryClient.clear();
      },
      onError: () => {
        logout();
        queryClient.clear();
      },
    },
  });
}
