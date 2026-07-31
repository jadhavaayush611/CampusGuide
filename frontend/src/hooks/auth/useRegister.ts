import { UseMutationResult } from '@tanstack/react-query';
import { authSdk } from '../../sdk/auth/AuthSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { RegisterPayload, AuthSession } from '../../models/auth.model';
import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { useAuth } from '../../core/auth';

export function useRegister(): UseMutationResult<AuthSession, Error, RegisterPayload> {
  const { login } = useAuth();

  return useOptimisticMutation<AuthSession, RegisterPayload>({
    mutationFn: (payload) => authSdk.register(payload),
    invalidateQueryKeys: [queryKeys.auth.all],
    successMessage: 'Account created successfully!',
    errorMessage: (err) => err.message || 'Failed to create account.',
    options: {
      onSuccess: (session) => {
        if (session?.tokens?.accessToken) {
          login(
            {
              accessToken: session.tokens.accessToken,
              refreshToken: session.tokens.refreshToken,
            },
            session.user
          );
        }
      },
    },
  });
}
