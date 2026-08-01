/**
 * Frontend UI Domain Models for Notices Module
 */

export type NoticeCategory =
  | 'Academic'
  | 'Administrative'
  | 'Examination'
  | 'Events'
  | 'Councils'
  | 'Placements'
  | 'Scholarships'
  | 'General';

export type NoticePriority = 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW';

export type NoticeVisibility = 'PUBLIC' | 'STUDENTS' | 'FACULTY' | 'COUNCIL_MEMBERS' | 'INTERNAL';

export interface NoticeAttachment {
  id: string;
  name: string;
  fileType: string;
  fileSize: string | number;
  url: string;
  externalUrl?: string;
  isPreviewable?: boolean;
}

export interface Notice {
  id: string;
  title: string;
  slug: string;
  content: string;
  summary?: string;
  category: NoticeCategory;
  priority: NoticePriority;
  visibility: NoticeVisibility;
  postedBy: string;
  postedByRole?: string;
  councilId?: string;
  councilName?: string;
  publishedAt: string;
  expiresAt?: string;
  isPinned: boolean;
  isPublished: boolean;
  isImportant: boolean;
  isRead?: boolean;
  readAt?: string;
  tags: string[];
  attachments: NoticeAttachment[];
  createdAt?: string;
  updatedAt?: string;
}

export interface NoticeQueryParams {
  search?: string;
  category?: NoticeCategory | 'ALL';
  priority?: NoticePriority | 'ALL';
  publisher?: string;
  visibility?: NoticeVisibility;
  status?: 'ALL' | 'ACTIVE' | 'EXPIRED' | 'UNREAD' | 'ARCHIVED' | 'DRAFT';
  tags?: string[];
  isPinned?: boolean;
  includeUnpublished?: boolean;
  page?: number;
  limit?: number;
  sortBy?: 'publishedAt' | 'priority' | 'title' | 'createdAt';
  sortOrder?: 'asc' | 'desc';
}

export interface CreateNoticePayload {
  title: string;
  slug?: string;
  content: string;
  summary?: string;
  category: NoticeCategory;
  priority: NoticePriority;
  visibility?: NoticeVisibility;
  councilId?: string;
  publishedAt?: string;
  expiresAt?: string;
  isPinned?: boolean;
  isPublished?: boolean;
  tags?: string[];
  attachments?: NoticeAttachment[];
}

export interface UpdateNoticePayload {
  title?: string;
  slug?: string;
  content?: string;
  summary?: string;
  category?: NoticeCategory;
  priority?: NoticePriority;
  visibility?: NoticeVisibility;
  councilId?: string;
  publishedAt?: string;
  expiresAt?: string;
  isPinned?: boolean;
  isPublished?: boolean;
  tags?: string[];
  attachments?: NoticeAttachment[];
}

export interface NoticeReadStatus {
  noticeId: string;
  isRead: boolean;
  readAt?: string;
}
