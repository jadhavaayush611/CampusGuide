import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice, NoticeQueryParams } from '../../models/notice.model';

export function useNotices(params?: NoticeQueryParams) {
  return useQuery<Notice[], Error>({
    queryKey: queryKeys.notices.list(params),
    queryFn: () => noticeSdk.getAllNotices(params),
    staleTime: 1000 * 60 * 5,
    placeholderData: keepPreviousData,
  });
}
