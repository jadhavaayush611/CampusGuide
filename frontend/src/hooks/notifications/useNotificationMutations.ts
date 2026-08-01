import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { notificationSdk } from '../../sdk/notifications/NotificationSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useMarkAsRead() {
  return useOptimisticMutation<void, string>({
    mutationFn: (id: string) => notificationSdk.markAsRead(id),
    invalidateQueryKeys: [queryKeys.notifications.all],
    successMessage: 'Notification marked as read',
  });
}

export function useMarkAsUnread() {
  return useOptimisticMutation<void, string>({
    mutationFn: (id: string) => notificationSdk.markAsUnread(id),
    invalidateQueryKeys: [queryKeys.notifications.all],
    successMessage: 'Notification marked as unread',
  });
}

export function useMarkAllAsRead() {
  return useOptimisticMutation<void, void>({
    mutationFn: () => notificationSdk.markAllAsRead(),
    invalidateQueryKeys: [queryKeys.notifications.all],
    successMessage: 'All notifications marked as read',
  });
}

export function useArchiveNotification() {
  return useOptimisticMutation<void, string>({
    mutationFn: (id: string) => notificationSdk.archiveNotification(id),
    invalidateQueryKeys: [queryKeys.notifications.all],
    successMessage: 'Notification archived',
  });
}

export function useRestoreNotification() {
  return useOptimisticMutation<void, string>({
    mutationFn: (id: string) => notificationSdk.restoreNotification(id),
    invalidateQueryKeys: [queryKeys.notifications.all],
    successMessage: 'Notification restored to inbox',
  });
}

export function useDeleteNotification() {
  return useOptimisticMutation<void, string>({
    mutationFn: (id: string) => notificationSdk.deleteNotification(id),
    invalidateQueryKeys: [queryKeys.notifications.all],
    successMessage: 'Notification deleted',
  });
}
