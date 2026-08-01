import { useQuery } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';

export function useRecentNotices() {
  return useQuery<Notice[], Error>({
    queryKey: queryKeys.notices.recent(),
    queryFn: () => noticeSdk.getRecentNotices(),
    staleTime: 1000 * 60 * 2,
  });
}
