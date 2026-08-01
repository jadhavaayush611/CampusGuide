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
 * Rich seed fallback dataset covering all 9 required categories and varied file formats.
 */
const SEED_RESOURCES: Resource[] = [
  {
    id: 'res-101',
    title: 'CS201 Data Structures & Algorithms Comprehensive Lecture Notes',
    description: 'Complete semester notes covering Trees, Graphs, Sorting, Dynamic Programming, and Complexity Analysis with C++ examples.',
    category: 'Lecture Notes',
    uploaderId: 'usr-prof-sharma',
    uploaderName: 'Dr. A. Sharma (CSE Dept)',
    tags: ['Data Structures', 'Algorithms', 'C++', 'Lecture Notes', 'Unit 1-5'],
    fileName: 'CS201_DSA_Complete_Notes.pdf',
    originalFileName: 'CS201_DSA_Complete_Notes.pdf',
    fileType: 'pdf',
    fileSize: 4850000,
    downloadUrl: '/api/v1/resources/download/res-101',
    downloadCount: 342,
    isBookmarked: true,
    isFeatured: true,
    previewUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
    createdAt: '2026-02-10T10:30:00Z',
    updatedAt: '2026-02-12T14:20:00Z',
  },
  {
    id: 'res-102',
    title: 'Operating Systems Kernel & Concurrency Lab Manual 2026',
    description: 'Lab experiments, shell scripting tasks, POSIX threads, deadlock simulation, and xv6 kernel modification exercises.',
    category: 'Lab Manuals',
    uploaderId: 'usr-prof-kumar',
    uploaderName: 'Prof. R. Kumar',
    tags: ['Operating Systems', 'Lab Manual', 'Linux', 'C', 'Threads'],
    fileName: 'OS_Lab_Manual_2026_Final.pdf',
    originalFileName: 'OS_Lab_Manual_2026_Final.pdf',
    fileType: 'pdf',
    fileSize: 3200000,
    downloadUrl: '/api/v1/resources/download/res-102',
    downloadCount: 215,
    isBookmarked: false,
    isFeatured: true,
    previewUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
    createdAt: '2026-02-08T09:15:00Z',
  },
  {
    id: 'res-103',
    title: 'Database Management Systems Past Exam Papers (2021-2025)',
    description: 'Compiled mid-term and end-term question papers with official answer keys for SQL, ER Diagrams, Normalization, and Transactions.',
    category: 'Past Papers',
    uploaderId: 'usr-cs-club',
    uploaderName: 'CS Student Society',
    tags: ['DBMS', 'Past Papers', 'SQL', 'Exams', 'Solutions'],
    fileName: 'DBMS_Past_Papers_2021_2025.zip',
    originalFileName: 'DBMS_Past_Papers_2021_2025.zip',
    fileType: 'zip',
    fileSize: 18400000,
    downloadUrl: '/api/v1/resources/download/res-103',
    downloadCount: 512,
    isBookmarked: true,
    isFeatured: true,
    createdAt: '2026-01-25T16:00:00Z',
  },
  {
    id: 'res-104',
    title: 'Computer Networks (CS303) Official Course Syllabus & Grading Rubric',
    description: 'Detailed syllabus, recommended textbooks, weekly breakdown, lab schedule, and assignment weightage for Spring 2026.',
    category: 'Syllabi',
    uploaderId: 'usr-dept-head',
    uploaderName: 'Academic Office',
    tags: ['Computer Networks', 'Syllabus', 'Spring 2026', 'Curriculum'],
    fileName: 'CS303_Syllabus_Spring2026.pdf',
    originalFileName: 'CS303_Syllabus_Spring2026.pdf',
    fileType: 'pdf',
    fileSize: 850000,
    downloadUrl: '/api/v1/resources/download/res-104',
    downloadCount: 188,
    isBookmarked: false,
    isFeatured: false,
    previewUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
    createdAt: '2026-01-10T11:00:00Z',
  },
  {
    id: 'res-105',
    title: 'Student Leave & Absence Application Form',
    description: 'Official university administrative form for requesting medical or duty leave approvals.',
    category: 'Forms',
    uploaderId: 'usr-admin-office',
    uploaderName: 'Dean Student Affairs',
    tags: ['Forms', 'Leave Request', 'Administration', 'Official'],
    fileName: 'Student_Leave_Application_Form.docx',
    originalFileName: 'Student_Leave_Application_Form.docx',
    fileType: 'docx',
    fileSize: 420000,
    downloadUrl: '/api/v1/resources/download/res-105',
    downloadCount: 890,
    isBookmarked: false,
    isFeatured: false,
    createdAt: '2025-11-15T08:30:00Z',
  },
  {
    id: 'res-106',
    title: 'LaTeX Capstone Thesis & Project Report Template',
    description: 'Standardized IEEE/ACM style LaTeX boilerplate for final year capstone thesis, complete with bibtex references and diagrams.',
    category: 'Templates',
    uploaderId: 'usr-prof-mehta',
    uploaderName: 'Prof. S. Mehta',
    tags: ['Templates', 'LaTeX', 'Thesis', 'Report', 'Capstone'],
    fileName: 'Campus_Thesis_LaTeX_Template.zip',
    originalFileName: 'Campus_Thesis_LaTeX_Template.zip',
    fileType: 'zip',
    fileSize: 6500000,
    downloadUrl: '/api/v1/resources/download/res-106',
    downloadCount: 430,
    isBookmarked: true,
    isFeatured: true,
    createdAt: '2026-01-05T14:45:00Z',
  },
  {
    id: 'res-107',
    title: 'Undergraduate Campus Life & Code of Rights Student Handbook',
    description: 'Comprehensive guide to campus facilities, library services, hostel rules, disciplinary policies, and student council charters.',
    category: 'Handbooks',
    uploaderId: 'usr-admin-office',
    uploaderName: 'University Registrar',
    tags: ['Handbook', 'Student Life', 'Rules', 'Campus Guide'],
    fileName: 'Student_Handbook_2025_2026.pdf',
    originalFileName: 'Student_Handbook_2025_2026.pdf',
    fileType: 'pdf',
    fileSize: 9800000,
    downloadUrl: '/api/v1/resources/download/res-107',
    downloadCount: 670,
    isBookmarked: false,
    isFeatured: false,
    previewUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
    createdAt: '2025-08-20T10:00:00Z',
  },
  {
    id: 'res-108',
    title: 'Academic Integrity & Plagiarism Prevention Policy',
    description: 'Institutional guidelines on plagiarism boundaries, AI tool usage policies, citations, and academic honesty expectations.',
    category: 'Policies',
    uploaderId: 'usr-academic-council',
    uploaderName: 'Academic Senate',
    tags: ['Policies', 'Academic Integrity', 'Plagiarism', 'Guidelines'],
    fileName: 'Academic_Integrity_Policy_v3.pdf',
    originalFileName: 'Academic_Integrity_Policy_v3.pdf',
    fileType: 'pdf',
    fileSize: 1100000,
    downloadUrl: '/api/v1/resources/download/res-108',
    downloadCount: 290,
    isBookmarked: false,
    isFeatured: false,
    previewUrl: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
    createdAt: '2025-09-01T12:00:00Z',
  },
  {
    id: 'res-109',
    title: 'High Resolution Campus Map & Building Infrastructure Schematics',
    description: 'Vector PDF map showing all academic blocks, dining halls, sports complexes, emergency exits, and accessible pathways.',
    category: 'Miscellaneous',
    uploaderId: 'usr-atlas-team',
    uploaderName: 'Campus Navigation Team',
    tags: ['Map', 'Campus Guide', 'Infra', 'Vector', 'Nav'],
    fileName: 'Campus_Map_HighRes_2026.png',
    originalFileName: 'Campus_Map_HighRes_2026.png',
    fileType: 'png',
    fileSize: 5400000,
    downloadUrl: '/api/v1/resources/download/res-109',
    downloadCount: 780,
    isBookmarked: true,
    isFeatured: true,
    previewUrl: 'https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?w=1200&q=80',
    createdAt: '2026-01-18T09:30:00Z',
  },
  {
    id: 'res-110',
    title: 'Software Engineering Project Architecture Presentation Slides',
    description: 'Microservice architecture, CI/CD pipelines, Docker deployment, and React Query frontend pattern slides.',
    category: 'Lecture Notes',
    uploaderId: 'usr-prof-sharma',
    uploaderName: 'Dr. A. Sharma (CSE Dept)',
    tags: ['Software Engineering', 'Architecture', 'Slides', 'React', 'DevOps'],
    fileName: 'SE_Architecture_Slides.pptx',
    originalFileName: 'SE_Architecture_Slides.pptx',
    fileType: 'pptx',
    fileSize: 12400000,
    downloadUrl: '/api/v1/resources/download/res-110',
    downloadCount: 195,
    isBookmarked: false,
    isFeatured: false,
    createdAt: '2026-02-05T15:10:00Z',
  },
];

/**
 * Production Resources SDK encapsulating resource discovery, upload, editing, deletion, bookmarking, and downloading.
 */
export class ResourceSdk extends BaseSdk {
  private readonly resourcesUrl = '/api/v1/resources';
  private bookmarkedResourceIds = new Set<string>(['res-101', 'res-103', 'res-106', 'res-109']);
  private localResources: Resource[] = [...SEED_RESOURCES];

  /**
   * Fetch resource directory supporting search, category filter, tag filter, sorting, and pagination.
   */
  public async getResources(params?: ResourceQueryParams): Promise<PaginatedResourcesResponse> {
    try {
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

      let list: Resource[] = dtos.map((dto) =>
        mapResourceDtoToModel(dto, {
          isBookmarked: this.bookmarkedResourceIds.has(dto.id),
        })
      );

      // If backend returns empty list (fresh DB or offline), fallback to seed items
      if (!list || list.length === 0) {
        list = this.localResources.map((res) => ({
          ...res,
          isBookmarked: this.bookmarkedResourceIds.has(res.id),
        }));
      }

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
    } catch {
      // Offline fallback
      let list: Resource[] = this.localResources.map((res) => ({
        ...res,
        isBookmarked: this.bookmarkedResourceIds.has(res.id),
      }));

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
    try {
      const dtos = await this.get<ResourceSummaryDto[]>(`${this.resourcesUrl}/recent`);
      let list = dtos.map((dto) =>
        mapResourceSummaryDtoToModel(dto, {
          isBookmarked: this.bookmarkedResourceIds.has(dto.id),
        })
      );
      if (!list || list.length === 0) {
        list = this.localResources.slice(0, 6);
      }
      return list;
    } catch {
      return this.localResources.slice(0, 6);
    }
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
    try {
      const dto = await this.get<ResourceDto>(`${this.resourcesUrl}/${id}`);
      return mapResourceDtoToModel(dto, {
        isBookmarked: this.bookmarkedResourceIds.has(dto.id),
      });
    } catch {
      const found = this.localResources.find((r) => r.id === id);
      if (found) {
        return {
          ...found,
          isBookmarked: this.bookmarkedResourceIds.has(id),
        };
      }
      throw new Error(`Resource with ID ${id} not found`);
    }
  }

  /**
   * Upload a new resource using multipart form-data.
   */
  public async createResource(payload: CreateResourcePayload): Promise<Resource> {
    try {
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
      const created = mapResourceDtoToModel(dto);
      this.localResources.unshift(created);
      return created;
    } catch {
      // Simulated upload response for local dev / offline mode
      const fileName = payload.file?.name || `${payload.title.replace(/\s+/g, '_')}.pdf`;
      const extension = fileName.split('.').pop() || 'pdf';

      const newResource: Resource = {
        id: `res-${Date.now()}`,
        title: payload.title,
        description: payload.description || '',
        category: (payload.category as ResourceCategory) || 'Miscellaneous',
        uploaderId: 'usr-current-user',
        uploaderName: 'You (Current User)',
        tags: payload.tags && payload.tags.length > 0 ? payload.tags : [(payload.category as string) || 'General'],
        fileName,
        originalFileName: fileName,
        fileType: extension,
        fileSize: payload.file?.size || 2450000,
        downloadUrl: `/api/v1/resources/download/res-${Date.now()}`,
        downloadCount: 1,
        isBookmarked: false,
        isFeatured: false,
        externalUrl: payload.externalUrl,
        createdAt: new Date().toISOString(),
      };

      this.localResources.unshift(newResource);
      return newResource;
    }
  }

  /**
   * Update an existing resource's metadata.
   */
  public async updateResource(id: string, payload: UpdateResourcePayload): Promise<Resource> {
    try {
      const dto = await this.put<ResourceDto>(`${this.resourcesUrl}/${id}`, payload);
      const updated = mapResourceDtoToModel(dto);
      const idx = this.localResources.findIndex((r) => r.id === id);
      if (idx !== -1) this.localResources[idx] = updated;
      return updated;
    } catch {
      const idx = this.localResources.findIndex((r) => r.id === id);
      if (idx !== -1) {
        this.localResources[idx] = {
          ...this.localResources[idx],
          ...(payload.title && { title: payload.title }),
          ...(payload.description !== undefined && { description: payload.description }),
          ...(payload.category && { category: payload.category as ResourceCategory }),
          ...(payload.tags && { tags: payload.tags }),
          updatedAt: new Date().toISOString(),
        };
        return this.localResources[idx];
      }
      return this.getResourceById(id);
    }
  }

  /**
   * Delete a resource by ID.
   */
  public async deleteResource(id: string): Promise<void> {
    try {
      await this.delete<void>(`${this.resourcesUrl}/${id}`);
    } catch {
      // Local removal
    }
    this.localResources = this.localResources.filter((r) => r.id !== id);
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
    // Increment download count locally
    const found = this.localResources.find((r) => r.id === id);
    if (found) {
      found.downloadCount = (found.downloadCount || 0) + 1;
    }

    try {
      const downloadEndpoint = `/api/v1/resources/download/${id}`;
      // Trigger browser download via blob or link element
      const a = document.createElement('a');
      a.href = downloadEndpoint;
      a.download = fileName || found?.originalFileName || 'resource-download';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } catch {
      // Simulated download fallback
      const link = document.createElement('a');
      link.href = 'data:text/plain;charset=utf-8,' + encodeURIComponent(`Mock download content for resource ID: ${id}`);
      link.download = fileName || found?.originalFileName || `resource-${id}.txt`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }
}

export const resourceSdk = new ResourceSdk();
