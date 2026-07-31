import { UseMutationResult } from '@tanstack/react-query';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { CreatePostDto } from '../../sdk/community/community.dto';
import { queryKeys } from '../../sdk/queryKeys';
import { CommunityFeedPost } from '../../models/community.model';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export function useCreateCommunityPost(
  communityId: string
): UseMutationResult<CommunityFeedPost, Error, CreatePostDto> {
  return useOptimisticMutation<CommunityFeedPost, CreatePostDto>({
    mutationFn: (payload) => communitySdk.createPost(payload),
    invalidateQueryKeys: [queryKeys.communities.feed(communityId)],
    targetQueryKey: queryKeys.communities.feed(communityId),
    updateCacheOptimistically: (oldData: { posts: CommunityFeedPost[]; total: number; hasMore: boolean } | undefined, variables) => {
      const optimisticPost: CommunityFeedPost = {
        id: `temp-${Date.now()}`,
        title: variables.title,
        content: variables.content,
        authorId: 'current-user',
        authorName: 'You',
        communityId: variables.communityId,
        isPinned: Boolean(variables.isPinned),
        isAnnouncement: Boolean(variables.isAnnouncement),
        likeCount: 0,
        commentCount: 0,
        createdAt: new Date().toISOString(),
      };

      if (!oldData) {
        return { posts: [optimisticPost], total: 1, hasMore: false };
      }
      return {
        ...oldData,
        posts: [optimisticPost, ...oldData.posts],
        total: oldData.total + 1,
      };
    },
    successMessage: 'Post published to community!',
    errorMessage: 'Failed to publish post.',
  });
}
