import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Resource, PaginatedResourcesResponse } from '../../models/resource.model';

interface ToggleBookmarkVariables {
  id: string;
  isBookmarked: boolean;
}

interface BookmarkMutationContext {
  previousDetail?: Resource;
}

export function useBookmarkResource(): UseMutationResult<
  { success: boolean; isBookmarked: boolean },
  Error,
  ToggleBookmarkVariables,
  BookmarkMutationContext
> {
  const queryClient = useQueryClient();

  return useMutation<
    { success: boolean; isBookmarked: boolean },
    Error,
    ToggleBookmarkVariables,
    BookmarkMutationContext
  >({
    mutationFn: ({ id, isBookmarked }: ToggleBookmarkVariables) =>
      isBookmarked ? resourceSdk.removeBookmarkResource(id) : resourceSdk.bookmarkResource(id),

    onMutate: async ({ id, isBookmarked }) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.resources.all });

      // Optimistically update resource detail query
      const detailKey = queryKeys.resources.detail(id);
      const previousDetail = queryClient.getQueryData<Resource>(detailKey);
      if (previousDetail) {
        queryClient.setQueryData<Resource>(detailKey, {
          ...previousDetail,
          isBookmarked: !isBookmarked,
        });
      }

      // Optimistically update active queries matching list pattern
      queryClient.setQueriesData<PaginatedResourcesResponse>(
        { queryKey: queryKeys.resources.all },
        (oldData) => {
          if (!oldData || !oldData.resources) return oldData;
          return {
            ...oldData,
            resources: oldData.resources.map((res) =>
              res.id === id ? { ...res, isBookmarked: !isBookmarked } : res
            ),
          };
        }
      );

      return { previousDetail };
    },

    onError: (_err, { id }, context) => {
      if (context?.previousDetail) {
        queryClient.setQueryData(queryKeys.resources.detail(id), context.previousDetail);
      }
      queryClient.invalidateQueries({ queryKey: queryKeys.resources.all });
    },

    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.resources.all });
    },
  });
}

