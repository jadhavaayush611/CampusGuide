import { useMutation, useQueryClient } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { toast } from '../../core/toast/useToast';

export function useToggleNoticeRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, isRead }: { id: string; isRead: boolean }) => {
      return isRead ? noticeSdk.markAsRead(id) : noticeSdk.markAsUnread(id);
    },
    onMutate: async ({ id, isRead }) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.notices.all });
      queryClient.setQueriesData({ queryKey: queryKeys.notices.all }, (oldData: any) => {
        if (!Array.isArray(oldData)) return oldData;
        return oldData.map((notice) => (notice.id === id ? { ...notice, isRead } : notice));
      });
    },
    onSuccess: (_, { isRead }) => {
      toast.success(isRead ? 'Marked notice as read' : 'Marked notice as unread');
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notices.all });
    },
  });
}
