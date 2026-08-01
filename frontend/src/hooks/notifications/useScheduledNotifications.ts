import { useQuery } from '@tanstack/react-query';
import { scheduledNotificationSdk, ScheduledNotificationItem } from '../../sdk/notifications/ScheduledNotificationSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useScheduledNotifications() {
  return useQuery<ScheduledNotificationItem[], Error>({
    queryKey: queryKeys.scheduledNotifications.list(),
    queryFn: () => scheduledNotificationSdk.getScheduledNotifications(),
    staleTime: 5 * 60 * 1000,
  });
}
