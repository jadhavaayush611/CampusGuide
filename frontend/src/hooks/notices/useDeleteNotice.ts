import { useOptimisticMutation } from '../common/useOptimisticMutation';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useDeleteNotice() {
  return useOptimisticMutation<void, string>({
    mutationFn: (id) => noticeSdk.deleteNotice(id),
    invalidateQueryKeys: [queryKeys.notices.all],
    successMessage: 'Notice deleted successfully.',
    errorMessage: 'Failed to delete notice.',
  });
}
