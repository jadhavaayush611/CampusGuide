/**
 * Backend DTO Schemas for Communities
 */

export interface CommunityDto {
  id: string;
  name: string;
  description: string;
  bannerUrl?: string | null;
  logoUrl?: string | null;
  category?: string | null;
  tags?: string[] | null;
  councilId?: string | null;
  councilName?: string | null;
  memberCount: number;
  isPrivate?: boolean | null;
  isActive?: boolean | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CommunitySummaryDto {
  id: string;
  name: string;
  bannerUrl?: string | null;
  memberCount: number;
}

export interface CreateCommunityDto {
  name: string;
  description: string;
  bannerUrl?: string;
  logoUrl?: string;
  category?: string;
  councilId: string;
  tags?: string[];
  isPrivate?: boolean;
}

export interface UpdateCommunityDto {
  description?: string;
  bannerUrl?: string;
  logoUrl?: string;
  isActive?: boolean;
  name?: string;
  category?: string;
  tags?: string[];
}


export interface CommunityMemberDto {
  id: string;
  name: string;
  avatarUrl?: string | null;
  role: 'MEMBER' | 'MODERATOR' | 'ADMIN' | 'OWNER' | 'NONE';
  joinedAt?: string | null;
  email?: string | null;
  department?: string | null;
}

export interface PostDto {
  id: string;
  title: string;
  content: string;
  authorId: string;
  authorName?: string | null;
  authorAvatar?: string | null;
  communityId: string;
  imageUrls?: string[] | null;
  likeCount?: number | null;
  commentCount?: number | null;
  isPinned?: boolean | null;
  isAnnouncement?: boolean | null;
  isEdited?: boolean | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface PostSummaryDto {
  id: string;
  title: string;
  authorId: string;
  communityId: string;
  likeCount?: number | null;
  commentCount?: number | null;
  createdAt: string;
}

export interface CreatePostDto {
  title: string;
  content: string;
  communityId: string;
  imageUrls?: string[];
  isPinned?: boolean;
  isAnnouncement?: boolean;
}
