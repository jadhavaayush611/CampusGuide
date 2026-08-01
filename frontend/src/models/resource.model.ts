/**
 * Frontend UI Domain Models for Resources Module
 */

export type ResourceCategory =
  | 'Lecture Notes'
  | 'Lab Manuals'
  | 'Past Papers'
  | 'Syllabi'
  | 'Forms'
  | 'Templates'
  | 'Handbooks'
  | 'Policies'
  | 'Miscellaneous';

export interface Resource {
  id: string;
  title: string;
  description?: string;
  category: ResourceCategory | string;
  uploaderId: string;
  uploaderName?: string;
  councilId?: string;
  communityId?: string;
  tags: string[];
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  downloadUrl: string;
  downloadCount: number;
  isBookmarked?: boolean;
  isFeatured?: boolean;
  previewUrl?: string;
  externalUrl?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ResourceQueryParams {
  search?: string;
  category?: ResourceCategory | string;
  tag?: string;
  fileType?: string;
  uploaderId?: string;
  councilId?: string;
  communityId?: string;
  sort?: 'newest' | 'popular' | 'title' | 'size';
  page?: number;
  limit?: number;
}

export interface PaginatedResourcesResponse {
  resources: Resource[];
  total: number;
  page: number;
  totalPages: number;
}

export interface CreateResourcePayload {
  title: string;
  description?: string;
  category: ResourceCategory | string;
  tags?: string[];
  councilId?: string;
  communityId?: string;
  file?: File;
  externalUrl?: string;
}

export interface UpdateResourcePayload {
  title?: string;
  description?: string;
  category?: ResourceCategory | string;
  tags?: string[];
}
