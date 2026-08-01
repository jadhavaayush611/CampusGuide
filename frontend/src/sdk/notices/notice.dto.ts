/**
 * Backend DTO Schemas for Notices Module
 */

export type BackendNoticeCategory =
  | 'ACADEMIC'
  | 'EVENT'
  | 'GENERAL'
  | 'EXAM'
  | 'SCHOLARSHIP'
  | 'ADMINISTRATIVE'
  | 'PLACEMENT'
  | 'OTHER';

export type BackendNoticePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export type BackendNoticeVisibility = 'PUBLIC' | 'STUDENTS' | 'FACULTY' | 'COUNCIL_MEMBERS' | 'INTERNAL';

export interface NoticeAttachmentDto {
  id?: string;
  name: string;
  fileType: string;
  fileSize: number | string;
  url: string;
  externalUrl?: string;
}

export interface NoticeDto {
  id: string;
  title: string;
  slug: string;
  content: string;
  summary?: string | null;
  category?: BackendNoticeCategory | null;
  priority?: BackendNoticePriority | null;
  visibility?: BackendNoticeVisibility | null;
  councilId?: string | null;
  councilName?: string | null;
  postedBy?: string | null;
  postedByRole?: string | null;
  publishedAt?: string | null;
  expiresAt?: string | null;
  isPinned?: boolean | null;
  isPublished?: boolean | null;
  tags?: string[] | null;
  attachments?: NoticeAttachmentDto[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateNoticeRequestDto {
  title: string;
  slug: string;
  content: string;
  summary?: string;
  category?: BackendNoticeCategory;
  priority?: BackendNoticePriority;
  visibility?: BackendNoticeVisibility;
  councilId?: string;
  publishedAt?: string;
  expiresAt?: string;
  isPinned?: boolean;
  isPublished?: boolean;
  tags?: string[];
  attachments?: NoticeAttachmentDto[];
}

export interface UpdateNoticeRequestDto {
  title: string;
  slug: string;
  content: string;
  summary?: string;
  category?: BackendNoticeCategory;
  priority?: BackendNoticePriority;
  visibility?: BackendNoticeVisibility;
  councilId?: string;
  publishedAt?: string;
  expiresAt?: string;
  isPinned?: boolean;
  isPublished?: boolean;
  tags?: string[];
  attachments?: NoticeAttachmentDto[];
}

export interface PublishNoticeRequestDto {
  isPublished: boolean;
}

export interface PinNoticeRequestDto {
  isPinned: boolean;
}
