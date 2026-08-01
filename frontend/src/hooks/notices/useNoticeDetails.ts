import { useQuery } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';

export function useNoticeDetails(idOrSlug?: string) {
  const isUuid = Boolean(idOrSlug && /^[0-9a-fA-F-]{36}$/.test(idOrSlug));
  return useQuery<Notice, Error>({
    queryKey: isUuid ? queryKeys.notices.detail(idOrSlug!) : queryKeys.notices.detailBySlug(idOrSlug || ''),
    queryFn: () => {
      if (!idOrSlug) throw new Error('Notice identifier is required');
      return isUuid ? noticeSdk.getNoticeById(idOrSlug) : noticeSdk.getNoticeBySlug(idOrSlug);
    },
    enabled: Boolean(idOrSlug),
  });
}
