import { useQuery } from '@tanstack/react-query';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';
import { queryClient } from '../../core/query/queryClient';

export function useNoticeDetails(idOrSlug?: string) {
  const isUuid = Boolean(idOrSlug && /^[0-9a-fA-F-]{36}$/.test(idOrSlug));
  return useQuery<Notice, Error>({
    queryKey: isUuid ? queryKeys.notices.detail(idOrSlug!) : queryKeys.notices.detailBySlug(idOrSlug || ''),
    queryFn: () => {
      if (!idOrSlug) throw new Error('Notice identifier is required');
      return isUuid ? noticeSdk.getNoticeById(idOrSlug) : noticeSdk.getNoticeBySlug(idOrSlug);
    },
    enabled: Boolean(idOrSlug),
    staleTime: 5 * 60 * 1000,
    placeholderData: () => {
      if (!idOrSlug) return undefined;
      const queries = queryClient.getQueriesData<Notice[]>({
        queryKey: queryKeys.notices.all,
      });
      for (const [, data] of queries) {
        if (!data || !Array.isArray(data)) continue;
        const match = data.find((n) => n && (n.id === idOrSlug || n.slug === idOrSlug));
        if (match) return match;
      }
      return undefined;
    },
  });
}
