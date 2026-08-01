import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { UpdateNoticePayload, Notice } from '../../models/notice.model';

export function useUpdateNotice() {
  return useOptimisticMutation<Notice, { id: string; payload: UpdateNoticePayload }>({
    mutationFn: ({ id, payload }) => noticeSdk.updateNotice(id, payload),
    invalidateQueryKeys: [queryKeys.notices.all],
    successMessage: 'Notice updated successfully!',
    errorMessage: 'Failed to update notice.',
  });
}
