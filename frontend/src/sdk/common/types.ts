/**
 * Shared Common SDK Types
 */

export interface PageParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiResponseEnvelope<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
}
