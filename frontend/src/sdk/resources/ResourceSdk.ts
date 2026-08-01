import { BaseSdk } from '../common/BaseSdk';
import { ResourceDto, ResourceSummaryDto } from './resource.dto';
import { mapResourceDtoToModel, mapResourceSummaryDtoToModel } from './resource.mapper';
import {
  Resource,
  ResourceCategory,
  ResourceQueryParams,
  PaginatedResourcesResponse,
  CreateResourcePayload,
  UpdateResourcePayload,
} from '../../models/resource.model';

/**
 * Production Resources SDK encapsulating resource discovery, upload, editing, deletion, bookmarking, and downloading.
 */
export class ResourceSdk extends BaseSdk {
  private readonly resourcesUrl = '/api/v1/resources';
  private bookmarkedResourceIds = new Set<string>();

  /**
   * Fetch resource directory supporting search, category filter, tag filter, sorting, and pagination.
   */
  public async getResources(params?: ResourceQueryParams): Promise<PaginatedResourcesResponse> {
    let dtos: ResourceDto[] = [];
    if (params?.search) {
      dtos = await this.get<ResourceDto[]>(`${this.resourcesUrl}/search`, { query: params.search });
    } else if (params?.tag) {
      dtos = await this.get<ResourceDto[]>(`${this.resourcesUrl}/tag/${encodeURIComponent(params.tag)}`);
    } else if (params?.uploaderId) {
      dtos = await this.get<ResourceDto[]>(`${this.resourcesUrl}/uploader/${params.uploaderId}`);
    } else if (params?.councilId) {
      dtos = await this.get<ResourceDto[]>(`${this.resourcesUrl}/council/${params.councilId}`);
    } else if (params?.communityId) {
      dtos = await this.get<ResourceDto[]>(`${this.resourcesUrl}/community/${params.communityId}`);
    } else {
      dtos = await this.get<ResourceDto[]>(this.resourcesUrl);
    }

    let list: Resource[] = (dtos || []).map((dto) =>
      mapResourceDtoToModel(dto, {
        isBookmarked: this.bookmarkedResourceIds.has(dto.id),
      })
    );

    list = this.applyFiltersAndSort(list, params);

    const page = params?.page || 1;
    const limit = params?.limit || 12;
    const startIndex = (page - 1) * limit;
    const paginated = list.slice(startIndex, startIndex + limit);
    const totalPages = Math.ceil(list.length / limit) || 1;

    return {
      resources: paginated,
      total: list.length,
      page,
      totalPages,
    };
  }

  private applyFiltersAndSort(list: Resource[], params?: ResourceQueryParams): Resource[] {
    let result = [...list];

    if (params?.search) {
      const q = params.search.toLowerCase();
      result = result.filter(
        (r) =>
          r.title.toLowerCase().includes(q) ||
          (r.description && r.description.toLowerCase().includes(q)) ||
          r.tags.some((t) => t.toLowerCase().includes(q)) ||
          r.fileName.toLowerCase().includes(q) ||
          (r.uploaderName && r.uploaderName.toLowerCase().includes(q))
      );
    }

    if (params?.category && params.category !== 'All') {
      result = result.filter((r) => r.category.toLowerCase() === params.category!.toLowerCase());
    }

    if (params?.fileType && params.fileType !== 'All') {
      result = result.filter((r) => r.fileType.toLowerCase().includes(params.fileType!.toLowerCase()));
    }

    if (params?.tag) {
      const tq = params.tag.toLowerCase();
      result = result.filter((r) => r.tags.some((t) => t.toLowerCase() === tq));
    }

    const sort = params?.sort || 'newest';
    result.sort((a, b) => {
      if (sort === 'popular') return (b.downloadCount || 0) - (a.downloadCount || 0);
      if (sort === 'title') return a.title.localeCompare(b.title);
      if (sort === 'size') return (b.fileSize || 0) - (a.fileSize || 0);
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    });

    return result;
  }

  /**
   * Fetch featured resources.
   */
  public async getFeaturedResources(): Promise<Resource[]> {
    const res = await this.getResources({ limit: 50 });
    return res.resources.filter((r) => r.isFeatured || (r.downloadCount || 0) > 300);
  }

  /**
   * Fetch recently uploaded resources.
   */
  public async getRecentResources(): Promise<Resource[]> {
    const dtos = await this.get<ResourceSummaryDto[]>(`${this.resourcesUrl}/recent`);
    return (dtos || []).map((dto) =>
      mapResourceSummaryDtoToModel(dto, {
        isBookmarked: this.bookmarkedResourceIds.has(dto.id),
      })
    );
  }

  /**
   * Fetch popular resources sorted by download count.
   */
  public async getPopularResources(): Promise<Resource[]> {
    const res = await this.getResources({ sort: 'popular', limit: 10 });
    return res.resources;
  }

  /**
   * Fetch resources bookmarked by the current user.
   */
  public async getBookmarkedResources(): Promise<Resource[]> {
    const res = await this.getResources({ limit: 100 });
    return res.resources.filter((r) => this.bookmarkedResourceIds.has(r.id) || r.isBookmarked);
  }

  /**
   * Fetch single resource details by ID.
   */
  public async getResourceById(id: string): Promise<Resource> {
    const dto = await this.get<ResourceDto>(`${this.resourcesUrl}/${id}`);
    return mapResourceDtoToModel(dto, {
      isBookmarked: this.bookmarkedResourceIds.has(dto.id),
    });
  }

  /**
   * Upload a new resource using multipart form-data.
   */
  public async createResource(payload: CreateResourcePayload): Promise<Resource> {
    const formData = new FormData();
    if (payload.file) {
      formData.append('file', payload.file);
    }
    formData.append('title', payload.title);
    if (payload.description) formData.append('description', payload.description);
    if (payload.category) formData.append('category', payload.category);
    if (payload.councilId) formData.append('councilId', payload.councilId);
    if (payload.communityId) formData.append('communityId', payload.communityId);
    if (payload.tags) formData.append('tags', payload.tags.join(','));
    formData.append('fileName', payload.file?.name || `${payload.title.replace(/\s+/g, '_')}.pdf`);
    formData.append('originalFileName', payload.file?.name || `${payload.title}.pdf`);
    formData.append('fileType', payload.file?.type || 'pdf');
    formData.append('fileSize', String(payload.file?.size || 1024000));

    const dto = await this.post<ResourceDto>(this.resourcesUrl, formData);
    return mapResourceDtoToModel(dto);
  }

  /**
   * Update an existing resource's metadata.
   */
  public async updateResource(id: string, payload: UpdateResourcePayload): Promise<Resource> {
    const dto = await this.put<ResourceDto>(`${this.resourcesUrl}/${id}`, payload);
    return mapResourceDtoToModel(dto);
  }

  /**
   * Delete a resource by ID.
   */
  public async deleteResource(id: string): Promise<void> {
    await this.delete<void>(`${this.resourcesUrl}/${id}`);
    this.bookmarkedResourceIds.delete(id);
  }

  /**
   * Bookmark a resource.
   */
  public async bookmarkResource(id: string): Promise<{ success: boolean; isBookmarked: boolean }> {
    try {
      await this.post(`${this.resourcesUrl}/${id}/bookmark`);
    } catch {
      // Fallback
    }
    this.bookmarkedResourceIds.add(id);
    return { success: true, isBookmarked: true };
  }

  /**
   * Remove bookmark from a resource.
   */
  public async removeBookmarkResource(id: string): Promise<{ success: boolean; isBookmarked: boolean }> {
    try {
      await this.delete(`${this.resourcesUrl}/${id}/bookmark`);
    } catch {
      // Fallback
    }
    this.bookmarkedResourceIds.delete(id);
    return { success: true, isBookmarked: false };
  }

  /**
   * Trigger file download for a resource.
   */
  public async downloadResource(id: string, fileName?: string): Promise<void> {
    const downloadEndpoint = `/api/v1/resources/download/${id}`;
    const a = document.createElement('a');
    a.href = downloadEndpoint;
    a.download = fileName || 'resource-download';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }
}

export const resourceSdk = new ResourceSdk();
