import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { attachmentSdk, AttachmentItem } from '../../sdk/attachments/AttachmentSdk';

export const attachmentKeys = {
  all: ['attachments'] as const,
  owner: (ownerType: string, ownerId: string) => ['attachments', ownerType, ownerId] as const,
};

export function useAttachments(ownerType: 'PLANNER_TASK' | 'NOTICE', ownerId?: string) {
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: attachmentKeys.owner(ownerType, ownerId || ''),
    queryFn: () => (ownerId ? attachmentSdk.getAttachmentsForOwner(ownerType, ownerId) : Promise.resolve([])),
    enabled: Boolean(ownerId),
  });

  const uploadMutation = useMutation({
    mutationFn: ({ file, targetOwnerId }: { file: File; targetOwnerId?: string }) => {
      const id = targetOwnerId || ownerId;
      if (!id) throw new Error('Missing owner ID for attachment upload');
      return attachmentSdk.uploadAttachment(file, ownerType, id);
    },
    onSuccess: (_, vars) => {
      const id = vars.targetOwnerId || ownerId;
      if (id) {
        queryClient.invalidateQueries({ queryKey: attachmentKeys.owner(ownerType, id) });
      }
      queryClient.invalidateQueries({ queryKey: ['planner'] });
      queryClient.invalidateQueries({ queryKey: ['notices'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (attachmentId: string) => attachmentSdk.deleteAttachment(attachmentId),
    onSuccess: () => {
      if (ownerId) {
        queryClient.invalidateQueries({ queryKey: attachmentKeys.owner(ownerType, ownerId) });
      }
      queryClient.invalidateQueries({ queryKey: ['planner'] });
      queryClient.invalidateQueries({ queryKey: ['notices'] });
    },
  });

  return {
    attachments: query.data || [],
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
    uploadAttachment: uploadMutation.mutateAsync,
    isUploading: uploadMutation.isPending,
    uploadError: uploadMutation.error,
    deleteAttachment: deleteMutation.mutateAsync,
    isDeleting: deleteMutation.isPending,
  };
}
