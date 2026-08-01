import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import {
  AtlasConversation,
  ConversationQueryParams,
  PaginatedConversationsResponse,
} from '../../models/atlas.model';

export function useAtlasConversations(
  params?: ConversationQueryParams
): UseQueryResult<PaginatedConversationsResponse, Error> {
  return useQuery<PaginatedConversationsResponse, Error>({
    queryKey: queryKeys.conversations.list(params),
    queryFn: async () => {
      const response = await atlasClient.conversations.list(params);
      
      let rawList: AtlasConversation[] = [];
      if (Array.isArray(response)) {
        rawList = response;
      } else if (response && Array.isArray((response as any).data)) {
        return response as PaginatedConversationsResponse;
      } else if (response && Array.isArray((response as any).conversations)) {
        rawList = (response as any).conversations;
      }

      // Filter by search query if provided on client side
      if (params?.search) {
        const query = params.search.toLowerCase();
        rawList = rawList.filter(
          (c) =>
            c.title?.toLowerCase().includes(query) ||
            c.type?.toLowerCase().includes(query)
        );
      }

      // Filter by status if provided
      if (params?.status) {
        rawList = rawList.filter((c) => c.status === params.status);
      }

      // Sort array
      const sortBy = params?.sortBy || 'updatedAt';
      const sortOrder = params?.sortOrder || 'desc';
      rawList.sort((a: any, b: any) => {
        let valA = a[sortBy] ?? '';
        let valB = b[sortBy] ?? '';
        if (sortBy === 'updatedAt' || sortBy === 'createdAt') {
          valA = new Date(valA).getTime() || 0;
          valB = new Date(valB).getTime() || 0;
        }
        if (valA < valB) return sortOrder === 'asc' ? -1 : 1;
        if (valA > valB) return sortOrder === 'asc' ? 1 : -1;
        return 0;
      });

      const page = params?.page || 1;
      const limit = params?.limit || 10;
      const startIndex = (page - 1) * limit;
      const paginatedItems = rawList.slice(startIndex, startIndex + limit);
      const totalPages = Math.ceil(rawList.length / limit) || 1;

      return {
        data: paginatedItems,
        page,
        limit,
        total: rawList.length,
        totalPages,
      };
    },
    staleTime: 1000 * 30, // 30 seconds cache reuse
  });
}
