import { UseMutationResult } from '@tanstack/react-query';
import { authSdk } from '../../sdk/auth/AuthSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { User } from '../../models/auth.model';
import { UpdateProfileDto } from '../../sdk/auth/auth.dto';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export function useUpdateProfile(userId: string): UseMutationResult<User, Error, UpdateProfileDto> {
  return useOptimisticMutation<User, UpdateProfileDto>({
    mutationFn: (payload) => authSdk.updateProfile(userId, payload),
    invalidateQueryKeys: [queryKeys.auth.user()],
    targetQueryKey: queryKeys.auth.user(),
    updateCacheOptimistically: (oldUser: User, variables: UpdateProfileDto) => {
      if (!oldUser) return oldUser;
      return {
        ...oldUser,
        ...variables,
      };
    },
    successMessage: 'Profile updated successfully',
    errorMessage: 'Failed to update profile',
  });
}
