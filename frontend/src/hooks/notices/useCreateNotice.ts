import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CreateNoticePayload, Notice } from '../../models/notice.model';

export function useCreateNotice() {
  return useOptimisticMutation<Notice, CreateNoticePayload>({
    mutationFn: (payload) => noticeSdk.createNotice(payload),
    invalidateQueryKeys: [queryKeys.notices.all],
    successMessage: 'Notice created successfully!',
    errorMessage: 'Failed to create notice.',
  });
}
