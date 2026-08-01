import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { notificationSdk } from '../../sdk/notifications/NotificationSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { NotificationStats } from '../../models/notification.model';

export function useNotificationStats(): UseQueryResult<NotificationStats, Error> {
  return useQuery<NotificationStats, Error>({
    queryKey: queryKeys.notifications.stats(),
    queryFn: () => notificationSdk.getNotificationStats(),
    staleTime: 30 * 1000,
  });
}
