/**
 * UI Domain Models for Communities
 */

export type CommunityCategory =
  | 'Academic'
  | 'Technology'
  | 'Creative'
  | 'Cultural'
  | 'Sports'
  | 'Professional'
  | 'Social'
  | string;

export type CommunityRole = 'MEMBER' | 'MODERATOR' | 'ADMIN' | 'OWNER' | 'NONE';

export interface CommunityMember {
  id: string;
  name: string;
  avatarUrl?: string;
  role: CommunityRole;
  joinedAt: string;
  email?: string;
  department?: string;
}

export interface CommunityActivityMetrics {
  postsThisWeek: number;
  activeMembersCount: number;
  lastActiveAt: string;
  engagementRate?: string;
}

export interface Community {
  id: string;
  name: string;
  description: string;
  bannerUrl?: string;
  logoUrl?: string;
  category: CommunityCategory;
  tags?: string[];
  councilId?: string;
  councilName?: string;
  memberCount: number;
  isPrivate?: boolean;
  isFeatured?: boolean;
  isTrending?: boolean;
  isJoined?: boolean;
  myRole?: CommunityRole;
  administrators?: CommunityMember[];
  moderators?: CommunityMember[];
  activityMetrics?: CommunityActivityMetrics;
  createdAt?: string;
  updatedAt?: string;
}

export interface CommunityFeedPost {
  id: string;
  title: string;
  content: string;
  authorId: string;
  authorName: string;
  authorAvatar?: string;
  authorRole?: string;
  communityId: string;
  communityName?: string;
  imageUrls?: string[];
  isPinned?: boolean;
  isAnnouncement?: boolean;
  likeCount: number;
  commentCount: number;
  likedByMe?: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateCommunityInput {
  name: string;
  description: string;
  category?: string;
  bannerUrl?: string;
  logoUrl?: string;
  councilId?: string;
  isPrivate?: boolean;
  tags?: string[];
}

export interface UpdateCommunityInput {
  name?: string;
  description?: string;
  category?: string;
  bannerUrl?: string;
  logoUrl?: string;
  isActive?: boolean;
  tags?: string[];
}
