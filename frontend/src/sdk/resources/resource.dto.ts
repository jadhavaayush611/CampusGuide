/**
 * Backend DTO Schemas for Resources Module
 */

export interface ResourceDto {
  id: string;
  title: string;
  description?: string | null;
  category?: string | null;
  uploaderId: string;
  uploaderName?: string | null;
  councilId?: string | null;
  communityId?: string | null;
  tags?: string[] | null;
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  downloadUrl: string;
  downloadCount?: number | null;
  isBookmarked?: boolean | null;
  isFeatured?: boolean | null;
  previewUrl?: string | null;
  externalUrl?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface ResourceSummaryDto {
  id: string;
  title: string;
  fileType: string;
  fileSize: number;
  uploaderId: string;
  createdAt: string;
  category?: string | null;
  downloadCount?: number | null;
}

export interface CreateResourceRequestDto {
  title: string;
  description?: string;
  category?: string;
  councilId?: string;
  communityId?: string;
  tags?: string[];
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
}

export interface UpdateResourceRequestDto {
  title?: string;
  description?: string;
  category?: string;
  tags?: string[];
}
