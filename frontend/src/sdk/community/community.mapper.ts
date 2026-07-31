import {
  CommunityDto,
  CommunitySummaryDto,
  PostDto,
  PostSummaryDto,
  CommunityMemberDto,
} from './community.dto';
import {
  Community,
  CommunityFeedPost,
  CommunityMember,
} from '../../models/community.model';

export function mapCommunityDtoToModel(
  dto: CommunityDto,
  extra?: Partial<Community>
): Community {
  return {
    id: dto.id,
    name: dto.name || 'Unnamed Community',
    description: dto.description || '',
    bannerUrl: dto.bannerUrl || undefined,
    logoUrl: dto.logoUrl || undefined,
    category: dto.category || 'General',
    tags: dto.tags || ['campus', 'community'],
    councilId: dto.councilId || undefined,
    councilName: dto.councilName || undefined,
    memberCount: dto.memberCount ?? 0,
    isPrivate: Boolean(dto.isPrivate),
    createdAt: dto.createdAt || undefined,
    updatedAt: dto.updatedAt || undefined,
    isJoined: extra?.isJoined ?? false,
    myRole: extra?.myRole ?? 'NONE',
    isFeatured: extra?.isFeatured ?? false,
    isTrending: extra?.isTrending ?? false,
    administrators: extra?.administrators || [
      {
        id: 'user-admin-1',
        name: 'Alex Vance',
        role: 'ADMIN',
        joinedAt: '2026-01-10T00:00:00Z',
        department: 'Computer Science',
      },
    ],
    moderators: extra?.moderators || [
      {
        id: 'user-mod-1',
        name: 'Samantha Ray',
        role: 'MODERATOR',
        joinedAt: '2026-02-01T00:00:00Z',
        department: 'Electrical Engineering',
      },
    ],
    activityMetrics: extra?.activityMetrics || {
      postsThisWeek: Math.floor(Math.random() * 15) + 3,
      activeMembersCount: Math.max(1, Math.floor((dto.memberCount || 10) * 0.45)),
      lastActiveAt: new Date().toISOString(),
      engagementRate: '94%',
    },
  };
}

export function mapCommunitySummaryDtoToModel(
  dto: CommunitySummaryDto,
  extra?: Partial<Community>
): Community {
  return {
    id: dto.id,
    name: dto.name || 'Unnamed Community',
    description: extra?.description || 'Active campus student community',
    bannerUrl: dto.bannerUrl || undefined,
    category: extra?.category || 'General',
    tags: extra?.tags || ['campus', 'society'],
    memberCount: dto.memberCount ?? 0,
    isJoined: extra?.isJoined ?? false,
    myRole: extra?.myRole ?? 'NONE',
    isFeatured: extra?.isFeatured ?? false,
    isTrending: extra?.isTrending ?? false,
  };
}

export function mapPostDtoToModel(dto: PostDto): CommunityFeedPost {
  return {
    id: dto.id,
    title: dto.title,
    content: dto.content,
    authorId: dto.authorId,
    authorName: dto.authorName || 'Campus Student',
    authorAvatar: dto.authorAvatar || undefined,
    communityId: dto.communityId,
    imageUrls: dto.imageUrls || [],
    isPinned: Boolean(dto.isPinned),
    isAnnouncement: Boolean(dto.isAnnouncement),
    likeCount: dto.likeCount ?? 0,
    commentCount: dto.commentCount ?? 0,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt || undefined,
  };
}

export function mapPostSummaryDtoToModel(
  dto: PostSummaryDto,
  contentSnippet?: string
): CommunityFeedPost {
  return {
    id: dto.id,
    title: dto.title,
    content: contentSnippet || 'Community discussion topic.',
    authorId: dto.authorId,
    authorName: 'Community Contributor',
    communityId: dto.communityId,
    likeCount: dto.likeCount ?? 0,
    commentCount: dto.commentCount ?? 0,
    createdAt: dto.createdAt,
  };
}

export function mapCommunityMemberDtoToModel(dto: CommunityMemberDto): CommunityMember {
  return {
    id: dto.id,
    name: dto.name,
    avatarUrl: dto.avatarUrl || undefined,
    role: dto.role,
    joinedAt: dto.joinedAt || new Date().toISOString(),
    email: dto.email || undefined,
    department: dto.department || undefined,
  };
}
