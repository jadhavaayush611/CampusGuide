import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';

export function usePublishNotice() {
  return useOptimisticMutation<Notice, { id: string; isPublished: boolean }>({
    mutationFn: ({ id, isPublished }) => noticeSdk.publishNotice(id, isPublished),
    invalidateQueryKeys: [queryKeys.notices.all],
    successMessage: (_, { isPublished }) => (isPublished ? 'Notice published!' : 'Notice unpublished!'),
    errorMessage: 'Failed to update publication status.',
  });
}
