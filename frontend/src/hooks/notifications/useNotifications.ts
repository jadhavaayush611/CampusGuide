import { useQuery, keepPreviousData, UseQueryResult } from '@tanstack/react-query';
import { notificationSdk } from '../../sdk/notifications/NotificationSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { NotificationItem, NotificationQueryParams } from '../../models/notification.model';

export function useNotifications(params?: NotificationQueryParams): UseQueryResult<NotificationItem[], Error> {
  return useQuery<NotificationItem[], Error>({
    queryKey: queryKeys.notifications.list(params as Record<string, any>),
    queryFn: () => notificationSdk.getNotifications(params),
    staleTime: 2 * 60 * 1000, // 2 minutes
    placeholderData: keepPreviousData,
  });
}

