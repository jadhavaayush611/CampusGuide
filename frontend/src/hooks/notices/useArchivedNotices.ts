import { useQuery } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';

export function useArchivedNotices() {
  return useQuery<Notice[], Error>({
    queryKey: queryKeys.notices.archived(),
    queryFn: () => noticeSdk.getArchivedNotices(),
    staleTime: 1000 * 60 * 5,
  });
}
