import {
  CouncilDto,
  CouncilLeadershipDto,
  CouncilNoticeDto,
  CouncilResourceDto,
  CouncilMemberDto,
} from './council.dto';
import {
  Council,
  CouncilCategory,
  CouncilLeadershipMember,
  CouncilNotice,
  CouncilResource,
  CouncilMember,
  CouncilRole,
} from '../../models/council.model';

export function mapCouncilDtoToModel(
  dto: CouncilDto,
  meta?: {
    isJoined?: boolean;
    myRole?: CouncilRole;
    pendingJoinRequest?: boolean;
    leadership?: CouncilLeadershipMember[];
  }
): Council {
  const category = (dto.category || 'Technical') as CouncilCategory;

  return {
    id: String(dto.id),
    name: dto.name,
    slug: dto.slug || dto.name.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
    category,
    description: dto.description,
    longDescription: dto.longDescription || dto.description,
    bannerUrl: dto.bannerUrl || 'https://images.unsplash.com/photo-1541829070764-84a7d30dd3f3?w=1200&q=80',
    logoUrl: dto.logoUrl || 'https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=300&q=80',
    logoEmoji: dto.logoEmoji || '🏛️',
    contactInfo: {
      email: dto.email || 'council@campus.edu',
      phone: dto.contactNumber || '+1 (555) 019-2834',
      officeLocation: dto.officeLocation || 'Student Center, Room 304',
      websiteUrl: dto.websiteUrl || 'https://campusguide.edu/councils',
    },
    facultyAdvisor: dto.facultyAdvisor || 'Dr. Robert Vance (Dept. Head)',
    memberCount: dto.memberCount ?? 120,
    tags: dto.tags || [category, 'Student Senate', 'Official Governance'],
    isActive: dto.isActive ?? true,
    isFeatured: true,
    isRecentlyActive: true,
    isJoined: meta?.isJoined ?? false,
    myRole: meta?.myRole ?? 'NONE',
    pendingJoinRequest: meta?.pendingJoinRequest ?? false,
    leadership: meta?.leadership || [],
    activityMetrics: {
      activeEventsCount: 4,
      noticesCount: 12,
      memberCount: dto.memberCount ?? 120,
      resourcesCount: 18,
      engagementRate: '94%',
      lastActiveAt: new Date().toISOString(),
    },
    createdAt: dto.createdAt || new Date().toISOString(),
    updatedAt: dto.updatedAt || new Date().toISOString(),
  };
}

export function mapLeadershipDtoToModel(dto: CouncilLeadershipDto): CouncilLeadershipMember {
  return {
    id: dto.id,
    name: dto.name,
    role: dto.role,
    category: dto.category,
    title: dto.title,
    department: dto.department,
    email: dto.email || undefined,
    phone: dto.phone || undefined,
    avatarUrl: dto.avatarUrl || undefined,
    hierarchyOrder: dto.hierarchyOrder,
    bio: dto.bio || undefined,
  };
}

export function mapCouncilNoticeDtoToModel(dto: CouncilNoticeDto): CouncilNotice {
  return {
    id: dto.id,
    title: dto.title,
    content: dto.content,
    postedBy: dto.postedBy,
    postedByRole: dto.postedByRole,
    councilId: dto.councilId,
    councilName: dto.councilName || undefined,
    isPinned: Boolean(dto.isPinned),
    isImportant: Boolean(dto.isImportant),
    category: dto.category || 'Announcement',
    createdAt: dto.createdAt,
    attachments: dto.attachments || [],
  };
}

export function mapCouncilResourceDtoToModel(dto: CouncilResourceDto): CouncilResource {
  return {
    id: dto.id,
    title: dto.title,
    description: dto.description || undefined,
    councilId: dto.councilId,
    councilName: dto.councilName || undefined,
    category: dto.category || 'Handbooks',
    fileType: dto.fileType,
    fileSize: dto.fileSize,
    downloadUrl: dto.downloadUrl,
    uploaderName: dto.uploaderName,
    createdAt: dto.createdAt,
    tags: dto.tags || [],
  };
}

export function mapMemberDtoToModel(dto: CouncilMemberDto): CouncilMember {
  return {
    id: dto.id,
    name: dto.name,
    email: dto.email,
    role: (dto.role as CouncilRole) || 'MEMBER',
    roleTitle: dto.roleTitle,
    department: dto.department,
    joinedAt: dto.joinedAt,
    avatarUrl: dto.avatarUrl || undefined,
  };
}
