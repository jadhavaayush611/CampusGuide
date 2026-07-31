import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { notificationSdk } from '../../sdk/notifications/NotificationSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useUnreadNotificationCount(): UseQueryResult<number, Error> {
  return useQuery<number, Error>({
    queryKey: queryKeys.notifications.unreadCount(),
    queryFn: () => notificationSdk.getUnreadCount(),
    staleTime: 30 * 1000, // 30 seconds
  });
}
