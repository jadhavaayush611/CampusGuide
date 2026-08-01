import { useQuery } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';

export function usePinnedNotices() {
  return useQuery<Notice[], Error>({
    queryKey: queryKeys.notices.pinned(),
    queryFn: () => noticeSdk.getPinnedNotices(),
    staleTime: 1000 * 60 * 5,
  });
}
