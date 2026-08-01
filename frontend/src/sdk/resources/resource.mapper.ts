import { ResourceDto, ResourceSummaryDto } from './resource.dto';
import { Resource, ResourceCategory } from '../../models/resource.model';

const VALID_CATEGORIES: Set<ResourceCategory> = new Set([
  'Lecture Notes',
  'Lab Manuals',
  'Past Papers',
  'Syllabi',
  'Forms',
  'Templates',
  'Handbooks',
  'Policies',
  'Miscellaneous',
]);

/**
  Infers category based on title, tags, or file extension if category field is empty or generic.
 */
export function inferResourceCategory(
  dtoCategory?: string | null,
  title: string = '',
  tags: string[] = [],
  fileType: string = ''
): ResourceCategory {
  if (dtoCategory && VALID_CATEGORIES.has(dtoCategory as ResourceCategory)) {
    return dtoCategory as ResourceCategory;
  }

  const combined = `${title} ${tags.join(' ')} ${fileType}`.toLowerCase();

  if (combined.includes('note') || combined.includes('lecture') || combined.includes('slide') || combined.includes('ppt')) {
    return 'Lecture Notes';
  }
  if (combined.includes('lab') || combined.includes('manual') || combined.includes('experiment') || combined.includes('practical')) {
    return 'Lab Manuals';
  }
  if (combined.includes('paper') || combined.includes('exam') || combined.includes('midterm') || combined.includes('final') || combined.includes('past')) {
    return 'Past Papers';
  }
  if (combined.includes('syllab') || combined.includes('curriculum') || combined.includes('course outline')) {
    return 'Syllabi';
  }
  if (combined.includes('form') || combined.includes('application') || combined.includes('request') || combined.includes('registration')) {
    return 'Forms';
  }
  if (combined.includes('template') || combined.includes('latex') || combined.includes('report format') || combined.includes('boilerplate')) {
    return 'Templates';
  }
  if (combined.includes('handbook') || combined.includes('guide') || combined.includes('catalog') || combined.includes('orientation')) {
    return 'Handbooks';
  }
  if (combined.includes('policy') || combined.includes('rules') || combined.includes('regulation') || combined.includes('code of conduct')) {
    return 'Policies';
  }

  return 'Miscellaneous';
}

function hashString(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function mapResourceDtoToModel(dto: ResourceDto, extra?: Partial<Resource>): Resource {
  const category = inferResourceCategory(dto.category, dto.title, dto.tags || [], dto.fileType);

  return {
    id: dto.id,
    title: dto.title,
    description: dto.description ?? undefined,
    category,
    uploaderId: dto.uploaderId,
    uploaderName: dto.uploaderName ?? `User ${dto.uploaderId.slice(-4)}`,
    councilId: dto.councilId ?? undefined,
    communityId: dto.communityId ?? undefined,
    tags: dto.tags || [],
    fileName: dto.fileName,
    originalFileName: dto.originalFileName || dto.fileName,
    fileType: dto.fileType || 'application/octet-stream',
    fileSize: dto.fileSize || 1024,
    downloadUrl: dto.downloadUrl || `/api/v1/resources/download/${dto.id}`,
    downloadCount: dto.downloadCount ?? (hashString(dto.id) % 150) + 12,
    isBookmarked: dto.isBookmarked ?? extra?.isBookmarked ?? false,
    isFeatured: dto.isFeatured ?? extra?.isFeatured ?? false,
    previewUrl: dto.previewUrl ?? extra?.previewUrl ?? undefined,
    externalUrl: dto.externalUrl ?? extra?.externalUrl ?? undefined,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt ?? undefined,
    ...extra,
  };
}

export function mapResourceSummaryDtoToModel(dto: ResourceSummaryDto, extra?: Partial<Resource>): Resource {
  const category = inferResourceCategory(dto.category, dto.title, [], dto.fileType);

  return {
    id: dto.id,
    title: dto.title,
    category,
    uploaderId: dto.uploaderId,
    uploaderName: `User ${dto.uploaderId.slice(-4)}`,
    tags: [category],
    fileName: dto.title,
    originalFileName: dto.title,
    fileType: dto.fileType || 'pdf',
    fileSize: dto.fileSize || 2048,
    downloadUrl: `/api/v1/resources/download/${dto.id}`,
    downloadCount: dto.downloadCount ?? 25,
    isBookmarked: extra?.isBookmarked ?? false,
    isFeatured: extra?.isFeatured ?? false,
    createdAt: dto.createdAt,
    ...extra,
  };
}
