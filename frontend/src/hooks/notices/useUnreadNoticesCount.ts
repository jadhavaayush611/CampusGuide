import { useQuery } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useUnreadNoticesCount() {
  return useQuery<number, Error>({
    queryKey: queryKeys.notices.unreadCount(),
    queryFn: async () => {
      const notices = await noticeSdk.getAllNotices();
      return noticeSdk.getUnreadCount(notices);
    },
    staleTime: 1000 * 30,
  });
}
