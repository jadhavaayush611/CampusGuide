import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { authSdk } from '../../sdk/auth/AuthSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { User } from '../../models/auth.model';

export function useCurrentUser(enabled = true): UseQueryResult<User, Error> {
  return useQuery<User, Error>({
    queryKey: queryKeys.auth.user(),
    queryFn: () => authSdk.getCurrentUser(),
    enabled,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
}
