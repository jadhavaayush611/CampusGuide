import { UseMutationResult } from '@tanstack/react-query';
import { authSdk } from '../../sdk/auth/AuthSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { LoginCredentials, AuthSession } from '../../models/auth.model';
import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { useAuth } from '../../core/auth';

export function useLogin(): UseMutationResult<AuthSession, Error, LoginCredentials> {
  const { login } = useAuth();

  return useOptimisticMutation<AuthSession, LoginCredentials>({
    mutationFn: (credentials) => authSdk.login(credentials),
    invalidateQueryKeys: [queryKeys.auth.all],
    successMessage: 'Successfully signed in',
    errorMessage: (err) => err.message || 'Failed to sign in. Please check your credentials.',
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
