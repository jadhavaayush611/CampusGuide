import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { notificationSdk } from '../../sdk/notifications/NotificationSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { NotificationItem } from '../../models/notification.model';

export function useNotifications(): UseQueryResult<NotificationItem[], Error> {
  return useQuery<NotificationItem[], Error>({
    queryKey: queryKeys.notifications.list(),
    queryFn: () => notificationSdk.getNotifications(),
    staleTime: 1 * 60 * 1000, // 1 minute
  });
}
