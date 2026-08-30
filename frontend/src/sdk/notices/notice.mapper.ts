import { NoticeDto, BackendNoticeCategory, BackendNoticePriority, BackendNoticeVisibility, NoticeAttachmentDto, CreateNoticeRequestDto, UpdateNoticeRequestDto } from './notice.dto';
import { Notice, NoticeCategory, NoticePriority, NoticeVisibility, NoticeAttachment, CreateNoticePayload, UpdateNoticePayload } from '../../models/notice.model';

export function mapBackendCategoryToModel(category?: BackendNoticeCategory | null): NoticeCategory {
  if (!category) return 'General';
  switch (category) {
    case 'ACADEMIC':
      return 'Academic';
    case 'ADMINISTRATIVE':
      return 'Administrative';
    case 'EXAM':
      return 'Examination';
    case 'EVENT':
      return 'Events';
    case 'SCHOLARSHIP':
      return 'Scholarships';
    case 'PLACEMENT':
      return 'Placements';
    case 'OTHER':
      return 'Councils';
    case 'GENERAL':
    default:
      return 'General';
  }
}

export function mapModelCategoryToBackend(category?: NoticeCategory): BackendNoticeCategory {
  if (!category) return 'GENERAL';
  switch (category) {
    case 'Academic':
      return 'ACADEMIC';
    case 'Administrative':
      return 'ADMINISTRATIVE';
    case 'Examination':
      return 'EXAM';
    case 'Events':
      return 'EVENT';
    case 'Scholarships':
      return 'SCHOLARSHIP';
    case 'Placements':
      return 'PLACEMENT';
    case 'Councils':
      return 'OTHER';
    case 'General':
    default:
      return 'GENERAL';
  }
}

export function mapNoticeDtoToModel(dto: NoticeDto, readNoticeIds?: Set<string>): Notice {
  const category = mapBackendCategoryToModel(dto.category);
  const priority: NoticePriority = dto.priority || 'MEDIUM';
  const visibility: NoticeVisibility = dto.visibility || 'PUBLIC';

  const isImportant = priority === 'HIGH' || priority === 'URGENT';
  const isRead = readNoticeIds ? readNoticeIds.has(dto.id) : false;

  const attachments: NoticeAttachment[] = (dto.attachments || []).map((att: any, index) => {
    const name = att.name || att.originalFileName || 'Attachment Document';
    const url = att.url || att.downloadUrl || '#';
    const fileType = att.fileType || att.contentType || 'pdf';
    const fileSize = typeof att.fileSize === 'number'
      ? `${Math.round(att.fileSize / 1024)} KB`
      : (att.fileSize || '1.2 MB');

    return {
      id: att.id || `att-${dto.id}-${index}`,
      name,
      fileType,
      fileSize,
      url,
      externalUrl: att.externalUrl,
      isPreviewable: Boolean(
        fileType?.includes('pdf') ||
        fileType?.includes('image') ||
        fileType?.includes('jpg') ||
        fileType?.includes('png') ||
        url?.endsWith('.pdf') ||
        url?.endsWith('.png') ||
        url?.endsWith('.jpg') ||
        name?.endsWith('.pdf') ||
        name?.endsWith('.png') ||
        name?.endsWith('.jpg')
      ),
    };
  });

  // Infer publisher if missing
  let postedBy = dto.postedBy || dto.councilName || 'Office of Academic Affairs';
  let postedByRole = dto.postedByRole || 'Administration';
  if (category === 'Academic') {
    postedBy = dto.postedBy || 'Dean of Academics';
    postedByRole = 'Academic Office';
  } else if (category === 'Examination') {
    postedBy = dto.postedBy || 'Controller of Examinations';
    postedByRole = 'Exam Cell';
  } else if (category === 'Placements') {
    postedBy = dto.postedBy || 'Training & Placement Officer';
    postedByRole = 'Placement Cell';
  } else if (category === 'Scholarships') {
    postedBy = dto.postedBy || 'Student Financial Aid Office';
    postedByRole = 'Scholarship Committee';
  }

  const tags = dto.tags && dto.tags.length > 0 ? dto.tags : [category.toLowerCase(), priority.toLowerCase()];

  return {
    id: dto.id,
    title: dto.title,
    slug: dto.slug || dto.title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
    content: dto.content || '',
    summary: dto.summary || (dto.content ? dto.content.slice(0, 140) + '...' : undefined),
    category,
    priority,
    visibility,
    postedBy,
    postedByRole,
    councilId: dto.councilId || undefined,
    councilName: dto.councilName || undefined,
    publishedAt: dto.publishedAt || dto.createdAt || new Date().toISOString(),
    expiresAt: dto.expiresAt || undefined,
    isPinned: Boolean(dto.isPinned),
    isPublished: dto.isPublished !== undefined ? Boolean(dto.isPublished) : true,
    isImportant,
    isRead,
    tags,
    attachments,
    createdAt: dto.createdAt || undefined,
    updatedAt: dto.updatedAt || undefined,
  };
}

export function mapCreatePayloadToDto(payload: CreateNoticePayload): CreateNoticeRequestDto {
  const generatedSlug =
    payload.slug || payload.title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');

  return {
    title: payload.title,
    slug: generatedSlug,
    content: payload.content,
    summary: payload.summary,
    category: mapModelCategoryToBackend(payload.category),
    priority: payload.priority as BackendNoticePriority,
    visibility: (payload.visibility as BackendNoticeVisibility) || 'PUBLIC',
    councilId: payload.councilId,
    publishedAt: payload.publishedAt,
    expiresAt: payload.expiresAt,
    isPinned: payload.isPinned,
    isPublished: payload.isPublished !== undefined ? payload.isPublished : true,
    tags: payload.tags,
    attachments: payload.attachments?.map((att) => ({
      id: att.id,
      name: att.name,
      fileType: att.fileType,
      fileSize: att.fileSize,
      url: att.url,
      externalUrl: att.externalUrl,
    })),
  };
}

export function mapUpdatePayloadToDto(payload: UpdateNoticePayload): UpdateNoticeRequestDto {
  const generatedSlug =
    payload.slug || (payload.title ? payload.title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '') : '');

  return {
    title: payload.title || '',
    slug: generatedSlug,
    content: payload.content || '',
    summary: payload.summary,
    category: payload.category ? mapModelCategoryToBackend(payload.category) : undefined,
    priority: payload.priority ? (payload.priority as BackendNoticePriority) : undefined,
    visibility: payload.visibility ? (payload.visibility as BackendNoticeVisibility) : undefined,
    councilId: payload.councilId,
    publishedAt: payload.publishedAt,
    expiresAt: payload.expiresAt,
    isPinned: payload.isPinned,
    isPublished: payload.isPublished,
    tags: payload.tags,
    attachments: payload.attachments?.map((att) => ({
      id: att.id,
      name: att.name,
      fileType: att.fileType,
      fileSize: att.fileSize,
      url: att.url,
      externalUrl: att.externalUrl,
    })),
  };
}
