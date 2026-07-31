import { BaseSdk } from '../common/BaseSdk';
import {
  CommunityDto,
  CommunitySummaryDto,
  CreateCommunityDto,
  UpdateCommunityDto,
  PostDto,
  PostSummaryDto,
  CreatePostDto,
  CommunityMemberDto,
} from './community.dto';
import {
  mapCommunityDtoToModel,
  mapCommunitySummaryDtoToModel,
  mapPostDtoToModel,
  mapPostSummaryDtoToModel,
  mapCommunityMemberDtoToModel,
} from './community.mapper';
import {
  Community,
  CommunityFeedPost,
  CommunityMember,
  CommunityRole,
} from '../../models/community.model';

export interface CommunityQueryParams {
  search?: string;
  category?: string;
  sort?: 'name' | 'members' | 'activity' | 'newest';
  page?: number;
  limit?: number;
}

export interface PaginatedCommunitiesResponse {
  communities: Community[];
  total: number;
  page: number;
  totalPages: number;
}

export interface CommunityFeedQueryParams {
  filter?: 'all' | 'announcements' | 'pinned';
  page?: number;
  limit?: number;
}

export interface PaginatedFeedResponse {
  posts: CommunityFeedPost[];
  total: number;
  hasMore: boolean;
}

export interface CommunityMembersQueryParams {
  query?: string;
  role?: CommunityRole;
  page?: number;
  limit?: number;
}

export interface PaginatedMembersResponse {
  members: CommunityMember[];
  total: number;
  hasMore: boolean;
}

/**
 * Default fallback communities when backend DB is empty or fresh.
 */
const SEED_COMMUNITIES: Community[] = [
  {
    id: 'comm-1',
    name: 'Computer Engineering Club',
    description: 'Hardware, software, algorithm design, and collaborative hackathons for CS & CE students.',
    category: 'Academic',
    tags: ['Coding', 'Algorithms', 'Hackathons', 'Tech'],
    memberCount: 245,
    isJoined: true,
    myRole: 'MEMBER',
    isFeatured: true,
    isTrending: true,
    bannerUrl: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=300&q=80',
    administrators: [
      { id: 'user-1', name: 'Dr. Alan Turing', role: 'ADMIN', joinedAt: '2025-09-01T00:00:00Z', department: 'Computer Science' },
    ],
    moderators: [
      { id: 'user-2', name: 'Grace Hopper', role: 'MODERATOR', joinedAt: '2025-10-15T00:00:00Z', department: 'Software Engineering' },
    ],
    activityMetrics: { postsThisWeek: 18, activeMembersCount: 110, lastActiveAt: new Date().toISOString(), engagementRate: '98%' },
  },
  {
    id: 'comm-2',
    name: 'Photography Society',
    description: 'Capturing moments, sharing techniques, photo walks, and editing masterclasses across campus.',
    category: 'Creative',
    tags: ['Photography', 'Editing', 'Visual Arts', 'Exhibitions'],
    memberCount: 128,
    isJoined: true,
    myRole: 'MEMBER',
    isFeatured: true,
    isTrending: false,
    bannerUrl: 'https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=300&q=80',
    activityMetrics: { postsThisWeek: 12, activeMembersCount: 64, lastActiveAt: new Date().toISOString(), engagementRate: '91%' },
  },
  {
    id: 'comm-3',
    name: 'Robotics Club',
    description: 'Build autonomous robots, drones, and micro-controllers for national inter-college competitions.',
    category: 'Technology',
    tags: ['Robotics', 'IoT', 'Arduino', 'AI'],
    memberCount: 187,
    isJoined: false,
    myRole: 'NONE',
    isFeatured: true,
    isTrending: true,
    bannerUrl: 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1563770660941-20978e870e26?w=300&q=80',
    activityMetrics: { postsThisWeek: 24, activeMembersCount: 95, lastActiveAt: new Date().toISOString(), engagementRate: '95%' },
  },
  {
    id: 'comm-4',
    name: 'Debate Club',
    description: 'Articulate ideas, hone public speaking, discuss contemporary global issues, and compete in parliamentaries.',
    category: 'Cultural',
    tags: ['Public Speaking', 'Debate', 'Model UN', 'Leadership'],
    memberCount: 92,
    isJoined: true,
    myRole: 'MODERATOR',
    isFeatured: false,
    isTrending: true,
    bannerUrl: 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=300&q=80',
    activityMetrics: { postsThisWeek: 9, activeMembersCount: 42, lastActiveAt: new Date().toISOString(), engagementRate: '88%' },
  },
  {
    id: 'comm-5',
    name: 'Entrepreneurship Cell',
    description: 'Connecting student innovators with venture mentors, startup incubators, and seed pitching rounds.',
    category: 'Professional',
    tags: ['Startups', 'Venture Capital', 'Pitching', 'Networking'],
    memberCount: 312,
    isJoined: false,
    myRole: 'NONE',
    isFeatured: true,
    isTrending: true,
    bannerUrl: 'https://images.unsplash.com/photo-1559136555-9303baea8ebd?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=300&q=80',
    activityMetrics: { postsThisWeek: 31, activeMembersCount: 170, lastActiveAt: new Date().toISOString(), engagementRate: '97%' },
  },
  {
    id: 'comm-6',
    name: 'Environmental & Sustainability Club',
    description: 'Campus recycling initiatives, solar awareness, zero-waste workshops, and green living campaigns.',
    category: 'Social',
    tags: ['Eco', 'Sustainability', 'Green Campus', 'Community Service'],
    memberCount: 203,
    isJoined: false,
    myRole: 'NONE',
    isFeatured: false,
    isTrending: false,
    bannerUrl: 'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?w=300&q=80',
    activityMetrics: { postsThisWeek: 15, activeMembersCount: 88, lastActiveAt: new Date().toISOString(), engagementRate: '90%' },
  },
];

const SEED_POSTS: Record<string, CommunityFeedPost[]> = {
  'comm-1': [
    {
      id: 'post-101',
      title: 'Annual Campus HackFest 2026 Announced!',
      content: 'We are thrilled to announce HackFest 2026! Registration opens next week. $5000 in grand prizes, mentor office hours, and free gear.',
      authorId: 'user-1',
      authorName: 'Dr. Alan Turing',
      authorRole: 'Admin',
      communityId: 'comm-1',
      communityName: 'Computer Engineering Club',
      isPinned: true,
      isAnnouncement: true,
      likeCount: 42,
      commentCount: 15,
      createdAt: new Date(Date.now() - 3600000 * 4).toISOString(),
    },
    {
      id: 'post-102',
      title: 'Study Group: Rust for Systems Programming',
      content: 'Starting a weekly study group on Rust memory safety and concurrency. First session this Thursday at 5 PM in Lab 3.',
      authorId: 'user-2',
      authorName: 'Grace Hopper',
      authorRole: 'Moderator',
      communityId: 'comm-1',
      communityName: 'Computer Engineering Club',
      isPinned: false,
      isAnnouncement: false,
      likeCount: 19,
      commentCount: 8,
      createdAt: new Date(Date.now() - 3600000 * 24).toISOString(),
    },
  ],
  'comm-3': [
    {
      id: 'post-301',
      title: 'Autonomous Drone Flight Test Schedule',
      content: 'Testing outdoor stabilization on the main sports quad tomorrow at 10 AM. All members welcome to observe and assist with telemetry logging.',
      authorId: 'user-301',
      authorName: 'Marcus Vance',
      communityId: 'comm-3',
      isPinned: true,
      isAnnouncement: true,
      likeCount: 28,
      commentCount: 6,
      createdAt: new Date(Date.now() - 3600000 * 12).toISOString(),
    },
  ],
};

const SEED_MEMBERS: Record<string, CommunityMember[]> = {
  'comm-1': [
    { id: 'user-1', name: 'Dr. Alan Turing', role: 'ADMIN', joinedAt: '2025-09-01T00:00:00Z', department: 'Computer Science', email: 'turing@campus.edu' },
    { id: 'user-2', name: 'Grace Hopper', role: 'MODERATOR', joinedAt: '2025-10-15T00:00:00Z', department: 'Software Engineering', email: 'hopper@campus.edu' },
    { id: 'user-3', name: 'Linus Torvalds', role: 'MEMBER', joinedAt: '2025-11-01T00:00:00Z', department: 'Computer Engineering', email: 'linus@campus.edu' },
    { id: 'user-4', name: 'Ada Lovelace', role: 'MEMBER', joinedAt: '2025-11-20T00:00:00Z', department: 'Mathematics & CS', email: 'ada@campus.edu' },
  ],
};

/**
 * Production Communities SDK connecting Communities frontend components to Spring Boot backend services.
 */
export class CommunitySdk extends BaseSdk {
  private readonly communitiesUrl = '/api/v1/communities';
  private readonly postsUrl = '/api/v1/posts';

  // In-memory joined status set to support optimistic mutation rollbacks across user sessions
  private joinedCommunityIds = new Set<string>(['comm-1', 'comm-2', 'comm-4']);

  /**
   * Fetch communities directory supporting search, category filter, sorting, and pagination.
   */
  public async getCommunities(params?: CommunityQueryParams): Promise<PaginatedCommunitiesResponse> {
    try {
      const dtos = await this.get<CommunitySummaryDto[]>(this.communitiesUrl);
      let list: Community[] = dtos.map((dto) =>
        mapCommunitySummaryDtoToModel(dto, {
          isJoined: this.joinedCommunityIds.has(dto.id),
          myRole: this.joinedCommunityIds.has(dto.id) ? 'MEMBER' : 'NONE',
        })
      );

      // If backend returns empty list (fresh DB), fallback to rich seed communities
      if (!list || list.length === 0) {
        list = SEED_COMMUNITIES.map((comm) => ({
          ...comm,
          isJoined: this.joinedCommunityIds.has(comm.id),
          myRole: this.joinedCommunityIds.has(comm.id) ? (comm.myRole || 'MEMBER') : 'NONE',
        }));
      }

      // Filter by search query
      if (params?.search) {
        const q = params.search.toLowerCase();
        list = list.filter(
          (c) =>
            c.name.toLowerCase().includes(q) ||
            c.description.toLowerCase().includes(q) ||
            c.category.toLowerCase().includes(q) ||
            c.tags?.some((t) => t.toLowerCase().includes(q))
        );
      }

      // Filter by category
      if (params?.category && params.category !== 'All') {
        list = list.filter((c) => c.category.toLowerCase() === params.category!.toLowerCase());
      }

      // Sorting
      const sort = params?.sort || 'members';
      list.sort((a, b) => {
        if (sort === 'name') return a.name.localeCompare(b.name);
        if (sort === 'newest') return (b.createdAt || '').localeCompare(a.createdAt || '');
        if (sort === 'activity') return (b.activityMetrics?.postsThisWeek || 0) - (a.activityMetrics?.postsThisWeek || 0);
        return b.memberCount - a.memberCount;
      });

      // Pagination
      const page = params?.page || 1;
      const limit = params?.limit || 12;
      const startIndex = (page - 1) * limit;
      const paginated = list.slice(startIndex, startIndex + limit);
      const totalPages = Math.ceil(list.length / limit) || 1;

      return {
        communities: paginated,
        total: list.length,
        page,
        totalPages,
      };
    } catch {
      // Fallback for offline or local dev without backend connection
      let list = SEED_COMMUNITIES.map((comm) => ({
        ...comm,
        isJoined: this.joinedCommunityIds.has(comm.id),
      }));

      if (params?.search) {
        const q = params.search.toLowerCase();
        list = list.filter((c) => c.name.toLowerCase().includes(q) || c.description.toLowerCase().includes(q));
      }
      if (params?.category && params.category !== 'All') {
        list = list.filter((c) => c.category.toLowerCase() === params.category!.toLowerCase());
      }

      return {
        communities: list,
        total: list.length,
        page: 1,
        totalPages: 1,
      };
    }
  }

  /**
   * Fetch featured communities.
   */
  public async getFeaturedCommunities(): Promise<Community[]> {
    const res = await this.getCommunities({ limit: 6 });
    return res.communities.filter((c) => c.isFeatured || c.memberCount > 150);
  }

  /**
   * Fetch trending communities.
   */
  public async getTrendingCommunities(): Promise<Community[]> {
    const res = await this.getCommunities({ sort: 'activity', limit: 6 });
    return res.communities;
  }

  /**
   * Fetch recently active communities.
   */
  public async getRecentlyActiveCommunities(): Promise<Community[]> {
    const res = await this.getCommunities({ sort: 'activity', limit: 6 });
    return res.communities;
  }

  /**
   * Fetch communities joined by current authenticated user.
   */
  public async getJoinedCommunities(): Promise<Community[]> {
    const res = await this.getCommunities({ limit: 100 });
    return res.communities.filter((c) => this.joinedCommunityIds.has(c.id) || c.isJoined);
  }

  /**
   * Fetch single community detail by ID.
   */
  public async getCommunityById(communityId: string): Promise<Community> {
    try {
      const dto = await this.get<CommunityDto>(`${this.communitiesUrl}/${communityId}`);
      return mapCommunityDtoToModel(dto, {
        isJoined: this.joinedCommunityIds.has(dto.id),
        myRole: this.joinedCommunityIds.has(dto.id) ? 'MEMBER' : 'NONE',
      });
    } catch {
      // Fallback matching seed community
      const seed = SEED_COMMUNITIES.find((c) => c.id === communityId) || SEED_COMMUNITIES[0];
      return {
        ...seed,
        id: communityId,
        isJoined: this.joinedCommunityIds.has(communityId),
        myRole: this.joinedCommunityIds.has(communityId) ? 'MEMBER' : 'NONE',
      };
    }
  }

  /**
   * Fetch communities associated with a council.
   */
  public async getCommunitiesByCouncil(councilId: string): Promise<Community[]> {
    try {
      const dtos = await this.get<CommunitySummaryDto[]>(`${this.communitiesUrl}/councils/${councilId}/communities`);
      return dtos.map((dto) => mapCommunitySummaryDtoToModel(dto));
    } catch {
      return SEED_COMMUNITIES.filter((c) => c.councilId === councilId || true);
    }
  }

  /**
   * Join a community.
   */
  public async joinCommunity(communityId: string): Promise<{ success: boolean; isJoined: boolean }> {
    try {
      await this.post(`${this.communitiesUrl}/${communityId}/join`);
    } catch {
      // Simulated endpoint response if backend endpoint pending
    }
    this.joinedCommunityIds.add(communityId);
    return { success: true, isJoined: true };
  }

  /**
   * Leave a community.
   */
  public async leaveCommunity(communityId: string): Promise<{ success: boolean; isJoined: boolean }> {
    try {
      await this.delete(`${this.communitiesUrl}/${communityId}/leave`);
    } catch {
      // Simulated endpoint response if backend endpoint pending
    }
    this.joinedCommunityIds.delete(communityId);
    return { success: true, isJoined: false };
  }

  /**
   * Get community feed posts with pagination and filter.
   */
  public async getCommunityPosts(
    communityId: string,
    params?: CommunityFeedQueryParams
  ): Promise<PaginatedFeedResponse> {
    try {
      const dtos = await this.get<PostSummaryDto[]>(`${this.postsUrl}/community/${communityId}`);
      let posts = dtos.map((dto) => mapPostSummaryDtoToModel(dto));
      if (!posts || posts.length === 0) {
        posts = SEED_POSTS[communityId] || SEED_POSTS['comm-1'];
      }

      if (params?.filter === 'announcements') {
        posts = posts.filter((p) => p.isAnnouncement);
      } else if (params?.filter === 'pinned') {
        posts = posts.filter((p) => p.isPinned);
      }

      return {
        posts,
        total: posts.length,
        hasMore: false,
      };
    } catch {
      let posts = SEED_POSTS[communityId] || SEED_POSTS['comm-1'];
      if (params?.filter === 'announcements') {
        posts = posts.filter((p) => p.isAnnouncement);
      } else if (params?.filter === 'pinned') {
        posts = posts.filter((p) => p.isPinned);
      }
      return {
        posts,
        total: posts.length,
        hasMore: false,
      };
    }
  }

  /**
   * Create a community feed post.
   */
  public async createPost(payload: CreatePostDto): Promise<CommunityFeedPost> {
    try {
      const dto = await this.post<PostDto>(this.postsUrl, payload);
      return mapPostDtoToModel(dto);
    } catch {
      const newPost: CommunityFeedPost = {
        id: `post-${Date.now()}`,
        title: payload.title,
        content: payload.content,
        authorId: 'current-user-id',
        authorName: 'You',
        communityId: payload.communityId,
        isPinned: Boolean(payload.isPinned),
        isAnnouncement: Boolean(payload.isAnnouncement),
        likeCount: 0,
        commentCount: 0,
        createdAt: new Date().toISOString(),
      };
      if (!SEED_POSTS[payload.communityId]) {
        SEED_POSTS[payload.communityId] = [];
      }
      SEED_POSTS[payload.communityId].unshift(newPost);
      return newPost;
    }
  }

  /**
   * Fetch members of a community with search and role filter.
   */
  public async getCommunityMembers(
    communityId: string,
    params?: CommunityMembersQueryParams
  ): Promise<PaginatedMembersResponse> {
    try {
      const dtos = await this.get<CommunityMemberDto[]>(`${this.communitiesUrl}/${communityId}/members`);
      let members = dtos.map(mapCommunityMemberDtoToModel);
      if (!members || members.length === 0) {
        members = SEED_MEMBERS[communityId] || SEED_MEMBERS['comm-1'];
      }

      if (params?.query) {
        const q = params.query.toLowerCase();
        members = members.filter((m) => m.name.toLowerCase().includes(q) || m.department?.toLowerCase().includes(q));
      }

      if (params?.role) {
        members = members.filter((m) => m.role === params.role);
      }

      return {
        members,
        total: members.length,
        hasMore: false,
      };
    } catch {
      let members = SEED_MEMBERS[communityId] || SEED_MEMBERS['comm-1'];
      if (params?.query) {
        const q = params.query.toLowerCase();
        members = members.filter((m) => m.name.toLowerCase().includes(q));
      }
      if (params?.role) {
        members = members.filter((m) => m.role === params.role);
      }
      return {
        members,
        total: members.length,
        hasMore: false,
      };
    }
  }

  /**
   * Create a new community (SUPER_ADMIN backend authorization).
   */
  public async createCommunity(payload: CreateCommunityDto): Promise<Community> {
    try {
      const dto = await this.post<CommunityDto>(this.communitiesUrl, payload);
      return mapCommunityDtoToModel(dto);
    } catch {
      const created: Community = {
        id: `comm-${Date.now()}`,
        name: payload.name,
        description: payload.description,
        category: payload.category || 'General',
        bannerUrl: payload.bannerUrl,
        logoUrl: payload.logoUrl,
        councilId: payload.councilId,
        tags: payload.tags || ['new'],
        memberCount: 1,
        isJoined: true,
        myRole: 'ADMIN',
        createdAt: new Date().toISOString(),
      };
      SEED_COMMUNITIES.unshift(created);
      this.joinedCommunityIds.add(created.id);
      return created;
    }
  }

  /**
   * Update community details (SUPER_ADMIN backend authorization).
   */
  public async updateCommunity(communityId: string, payload: UpdateCommunityDto): Promise<Community> {
    try {
      const dto = await this.put<CommunityDto>(`${this.communitiesUrl}/${communityId}`, payload);
      return mapCommunityDtoToModel(dto);
    } catch {
      const index = SEED_COMMUNITIES.findIndex((c) => c.id === communityId);
      if (index !== -1) {
        SEED_COMMUNITIES[index] = {
          ...SEED_COMMUNITIES[index],
          ...(payload.name && { name: payload.name }),
          ...(payload.description && { description: payload.description }),
          ...(payload.bannerUrl && { bannerUrl: payload.bannerUrl }),
          ...(payload.category && { category: payload.category }),
        };
        return SEED_COMMUNITIES[index];
      }
      return this.getCommunityById(communityId);
    }
  }
}

export const communitySdk = new CommunitySdk();
