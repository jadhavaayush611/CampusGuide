import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Notice } from '../../models/notice.model';

export function usePinNotice() {
  return useOptimisticMutation<Notice, { id: string; isPinned: boolean }>({
    mutationFn: ({ id, isPinned }) => noticeSdk.pinNotice(id, isPinned),
    invalidateQueryKeys: [queryKeys.notices.all],
    successMessage: (_, { isPinned }) => (isPinned ? 'Notice pinned to top!' : 'Notice unpinned.'),
    errorMessage: 'Failed to update pinned status.',
  });
}
