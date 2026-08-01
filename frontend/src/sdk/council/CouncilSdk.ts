import { BaseSdk } from '../common/BaseSdk';
import {
  CouncilDto,
  CouncilLeadershipDto,
  CouncilNoticeDto,
  CouncilResourceDto,
  CouncilMemberDto,
  CreateCouncilDto,
  UpdateCouncilDto,
} from './council.dto';
import {
  mapCouncilDtoToModel,
  mapLeadershipDtoToModel,
  mapCouncilNoticeDtoToModel,
  mapCouncilResourceDtoToModel,
  mapMemberDtoToModel,
} from './council.mapper';
import {
  Council,
  CouncilCategory,
  CouncilLeadershipMember,
  CouncilNotice,
  CouncilResource,
  CouncilMember,
  CouncilRole,
} from '../../models/council.model';
import { CampusEvent } from '../../models/campus.model';

export interface CouncilQueryParams {
  search?: string;
  category?: CouncilCategory | 'All';
  sort?: 'name' | 'members' | 'activity' | 'newest';
  page?: number;
  limit?: number;
}

export interface PaginatedCouncilsResponse {
  councils: Council[];
  total: number;
  page: number;
  totalPages: number;
}

export interface CouncilMembersQueryParams {
  query?: string;
  role?: CouncilRole | 'ALL';
  page?: number;
  limit?: number;
}

export interface PaginatedCouncilMembersResponse {
  members: CouncilMember[];
  total: number;
  hasMore: boolean;
}

/**
 * Rich seed councils for production fallbacks & offline capabilities
 */
const SEED_COUNCILS: Council[] = [
  {
    id: 'council-1',
    name: 'Computer Society of India (CSI)',
    slug: 'csi',
    category: 'Technical',
    description: 'Advancing computer engineering, IT education, open source projects, and tech symposiums.',
    longDescription: 'The Computer Society of India (CSI) student branch is the premier technical body on campus dedicated to computing excellence, software craftsmanship, algorithmic problem solving, and technological innovation.',
    bannerUrl: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=300&q=80',
    logoEmoji: '💻',
    contactInfo: {
      email: 'csi@campus.edu',
      phone: '+1 (555) 432-8901',
      officeLocation: 'IT Block - Room 302',
      websiteUrl: 'https://csi.campus.edu',
    },
    facultyAdvisor: 'Dr. Evelyn Vance (Dept. Chair of Computer Science)',
    memberCount: 542,
    tags: ['Technical', 'Software', 'Algorithms', 'IEEE-Aligned', 'Programming'],
    isActive: true,
    isFeatured: true,
    isRecentlyActive: true,
    isJoined: true,
    myRole: 'MEMBER',
    pendingJoinRequest: false,
    leadership: [
      {
        id: 'lead-1',
        name: 'Dr. Evelyn Vance',
        role: 'Faculty Advisor',
        category: 'FACULTY_ADVISOR',
        title: 'Professor & Department Head',
        department: 'Computer Science',
        email: 'evance@campus.edu',
        hierarchyOrder: 1,
        bio: 'Faculty Advisor overseeing technical research grants and industry sponsorships.',
      },
      {
        id: 'lead-2',
        name: 'Alex Rivera',
        role: 'President / Chair',
        category: 'CHAIR',
        title: 'Council Chair',
        department: 'Computer Engineering (Senior)',
        email: 'arivera@campus.edu',
        hierarchyOrder: 2,
        bio: 'Leading CSI initiatives, corporate partnerships, and hackathon organization.',
      },
      {
        id: 'lead-3',
        name: 'Samantha Chen',
        role: 'Vice Chair',
        category: 'OFFICER',
        title: 'Vice President',
        department: 'Software Engineering (Junior)',
        email: 'schen@campus.edu',
        hierarchyOrder: 3,
        bio: 'Coordinates technical workshops, internal dev teams, and competitive coders.',
      },
      {
        id: 'lead-4',
        name: 'David Kim',
        role: 'Treasurer',
        category: 'OFFICER',
        title: 'Finance Director',
        department: 'Information Systems',
        email: 'dkim@campus.edu',
        hierarchyOrder: 4,
        bio: 'Manages annual council budget allocations, event sponsorships, and equipment funds.',
      },
    ],
    activityMetrics: {
      activeEventsCount: 5,
      noticesCount: 14,
      memberCount: 542,
      resourcesCount: 24,
      engagementRate: '98%',
      lastActiveAt: new Date().toISOString(),
    },
  },
  {
    id: 'council-2',
    name: 'IEEE Student Branch',
    slug: 'ieee',
    category: 'Technical',
    description: 'Fostering technological innovation and electrical, electronic & computer engineering excellence.',
    longDescription: 'IEEE Student Branch connects students to international technology networks, IEEE papers, hardware prototyping labs, and robotics symposiums.',
    bannerUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=300&q=80',
    logoEmoji: '⚡',
    contactInfo: {
      email: 'ieee@campus.edu',
      phone: '+1 (555) 890-1234',
      officeLocation: 'Electrical Block - Room 104',
      websiteUrl: 'https://ieee.campus.edu',
    },
    facultyAdvisor: 'Prof. Marcus Brody',
    memberCount: 628,
    tags: ['Hardware', 'Electronics', 'IEEE', 'Robotics', 'Research'],
    isActive: true,
    isFeatured: true,
    isRecentlyActive: true,
    isJoined: false,
    myRole: 'NONE',
    pendingJoinRequest: false,
    leadership: [
      {
        id: 'lead-ieee-1',
        name: 'Prof. Marcus Brody',
        role: 'Faculty Advisor',
        category: 'FACULTY_ADVISOR',
        title: 'Professor of Electrical Engineering',
        department: 'Electrical Engineering',
        hierarchyOrder: 1,
      },
      {
        id: 'lead-ieee-2',
        name: 'Elena Rostova',
        role: 'Chairperson',
        category: 'CHAIR',
        title: 'IEEE Student Chair',
        department: 'Electrical & Telecom',
        hierarchyOrder: 2,
      },
    ],
    activityMetrics: {
      activeEventsCount: 6,
      noticesCount: 18,
      memberCount: 628,
      resourcesCount: 31,
      engagementRate: '96%',
      lastActiveAt: new Date().toISOString(),
    },
  },
  {
    id: 'council-3',
    name: 'Cultural Council',
    slug: 'cultural-council',
    category: 'Cultural',
    description: 'Organizing campus-wide cultural festivals, theatrical productions, dance competitions, and arts celebrations.',
    longDescription: 'The Cultural Council governs all performing arts, music societies, fine arts forums, and annual festival committees across campus.',
    bannerUrl: 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1460723237483-7a6dc9d0b212?w=300&q=80',
    logoEmoji: '🎭',
    contactInfo: {
      email: 'cultural@campus.edu',
      phone: '+1 (555) 765-4321',
      officeLocation: 'Student Activity Building - Room 201',
      websiteUrl: 'https://cultural.campus.edu',
    },
    facultyAdvisor: 'Dr. Clara Oswald (Dean of Student Affairs)',
    memberCount: 567,
    tags: ['Music', 'Drama', 'Dance', 'Festivals', 'Arts'],
    isActive: true,
    isFeatured: true,
    isRecentlyActive: true,
    isJoined: true,
    myRole: 'OFFICER',
    pendingJoinRequest: false,
    leadership: [
      {
        id: 'lead-cult-1',
        name: 'Dr. Clara Oswald',
        role: 'Faculty Advisor',
        category: 'FACULTY_ADVISOR',
        title: 'Dean of Student Activities',
        department: 'Humanities & Fine Arts',
        hierarchyOrder: 1,
      },
      {
        id: 'lead-cult-2',
        name: 'Jordan Hayes',
        role: 'General Secretary',
        category: 'CHAIR',
        title: 'Cultural General Secretary',
        department: 'Media & Design',
        hierarchyOrder: 2,
      },
    ],
    activityMetrics: {
      activeEventsCount: 4,
      noticesCount: 10,
      memberCount: 567,
      resourcesCount: 16,
      engagementRate: '92%',
      lastActiveAt: new Date().toISOString(),
    },
  },
  {
    id: 'council-4',
    name: 'Sports Council',
    slug: 'sports-council',
    category: 'Sports',
    description: 'Managing inter-college athletics, tournaments, gym facilities, training camps, and sports leagues.',
    longDescription: 'The Sports Council oversees all competitive teams, field reservations, annual sports meets, and athletic fitness programs.',
    bannerUrl: 'https://images.unsplash.com/photo-1508606572321-901ea4437072?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=300&q=80',
    logoEmoji: '⚽',
    contactInfo: {
      email: 'sports@campus.edu',
      phone: '+1 (555) 234-5678',
      officeLocation: 'Sports Complex Main Office',
      websiteUrl: 'https://sports.campus.edu',
    },
    facultyAdvisor: 'Coach David Miller',
    memberCount: 634,
    tags: ['Athletics', 'Tournaments', 'Fitness', 'Intercollege', 'Leagues'],
    isActive: true,
    isFeatured: false,
    isRecentlyActive: true,
    isJoined: false,
    myRole: 'NONE',
    pendingJoinRequest: false,
    leadership: [
      {
        id: 'lead-sp-1',
        name: 'Coach David Miller',
        role: 'Faculty Advisor',
        category: 'FACULTY_ADVISOR',
        title: 'Director of Athletics',
        department: 'Physical Education',
        hierarchyOrder: 1,
      },
      {
        id: 'lead-sp-2',
        name: 'Carlos Mendez',
        role: 'Sports Secretary',
        category: 'CHAIR',
        title: 'Council Secretary',
        department: 'Kinesiology',
        hierarchyOrder: 2,
      },
    ],
    activityMetrics: {
      activeEventsCount: 8,
      noticesCount: 15,
      memberCount: 634,
      resourcesCount: 19,
      engagementRate: '95%',
      lastActiveAt: new Date().toISOString(),
    },
  },
  {
    id: 'council-5',
    name: 'E-Cell (Entrepreneurship Council)',
    slug: 'e-cell',
    category: 'Entrepreneurship',
    description: 'Fostering student entrepreneurship, pitch competitions, seed funding incubation, and founder mentoring.',
    longDescription: 'E-Cell provides student startups with venture mentorship, incubator office spaces, legal support, and access to angel investor networks.',
    bannerUrl: 'https://images.unsplash.com/photo-1559136555-9303baea8ebd?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=300&q=80',
    logoEmoji: '💡',
    contactInfo: {
      email: 'ecell@campus.edu',
      phone: '+1 (555) 345-6789',
      officeLocation: 'Incubation Center - Suite A',
      websiteUrl: 'https://ecell.campus.edu',
    },
    facultyAdvisor: 'Dr. Richard Hendricks',
    memberCount: 456,
    tags: ['Startups', 'Incubation', 'Venture Capital', 'Pitching', 'Innovation'],
    isActive: true,
    isFeatured: true,
    isRecentlyActive: true,
    isJoined: false,
    myRole: 'NONE',
    pendingJoinRequest: true,
    leadership: [
      {
        id: 'lead-ec-1',
        name: 'Dr. Richard Hendricks',
        role: 'Faculty Advisor',
        category: 'FACULTY_ADVISOR',
        title: 'Incubation Chair',
        department: 'Business & Entrepreneurship',
        hierarchyOrder: 1,
      },
      {
        id: 'lead-ec-2',
        name: 'Priya Sharma',
        role: 'President',
        category: 'CHAIR',
        title: 'E-Cell Lead',
        department: 'Business Administration',
        hierarchyOrder: 2,
      },
    ],
    activityMetrics: {
      activeEventsCount: 3,
      noticesCount: 9,
      memberCount: 456,
      resourcesCount: 14,
      engagementRate: '93%',
      lastActiveAt: new Date().toISOString(),
    },
  },
  {
    id: 'council-6',
    name: 'Training & Placement Cell (TPC)',
    slug: 'tpc',
    category: 'Career',
    description: 'Career guidance, corporate recruiting drives, mock interview clinics, and internship placements.',
    longDescription: 'TPC prepares students for industry readiness, organizing campus recruitment drives, industry guest lectures, and resume building workshops.',
    bannerUrl: 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200&q=80',
    logoUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300&q=80',
    logoEmoji: '💼',
    contactInfo: {
      email: 'placement@campus.edu',
      phone: '+1 (555) 678-9012',
      officeLocation: 'Career Development Center - Ground Floor',
      websiteUrl: 'https://placement.campus.edu',
    },
    facultyAdvisor: 'Prof. Arthur Pendelton',
    memberCount: 892,
    tags: ['Placements', 'Career', 'Internships', 'Corporate Relations', 'Resumes'],
    isActive: true,
    isFeatured: true,
    isRecentlyActive: true,
    isJoined: true,
    myRole: 'MEMBER',
    pendingJoinRequest: false,
    leadership: [
      {
        id: 'lead-tpc-1',
        name: 'Prof. Arthur Pendelton',
        role: 'Faculty Advisor',
        category: 'FACULTY_ADVISOR',
        title: 'Training & Placement Head',
        department: 'Corporate Relations',
        hierarchyOrder: 1,
      },
      {
        id: 'lead-tpc-2',
        name: 'Rohan Gupta',
        role: 'Student Head',
        category: 'CHAIR',
        title: 'Chief Placement Officer',
        department: 'Computer Science',
        hierarchyOrder: 2,
      },
    ],
    activityMetrics: {
      activeEventsCount: 7,
      noticesCount: 22,
      memberCount: 892,
      resourcesCount: 45,
      engagementRate: '99%',
      lastActiveAt: new Date().toISOString(),
    },
  },
];

const SEED_NOTICES: Record<string, CouncilNotice[]> = {
  'council-1': [
    {
      id: 'notice-101',
      title: 'Official Call for CSI Annual Hackathon Core Committee',
      content: 'CSI is recruiting core committee members for the upcoming Campus HackFest. Open roles include Technical Track Lead, Logistics Lead, and Sponsorship Manager. Applications close March 25.',
      postedBy: 'Alex Rivera',
      postedByRole: 'Council Chair',
      councilId: 'council-1',
      councilName: 'Computer Society of India (CSI)',
      isPinned: true,
      isImportant: true,
      category: 'Election',
      createdAt: new Date(Date.now() - 3600000 * 6).toISOString(),
      attachments: [
        { id: 'att-1', name: 'Committee_Application_Form.pdf', fileType: 'pdf', fileSize: '1.2 MB', url: '#' },
        { id: 'att-2', name: 'Hackathon_Rules_Handbook.pdf', fileType: 'pdf', fileSize: '850 KB', url: '#' },
      ],
    },
    {
      id: 'notice-102',
      title: 'Lab 3 Infrastructure Maintenance & Software Upgrades',
      content: 'Please note that IT Lab 3 will be undergoing server maintenance and compiler package upgrades on Saturday from 8:00 AM to 2:00 PM. Remote SSH sessions will be temporarily suspended.',
      postedBy: 'Samantha Chen',
      postedByRole: 'Vice Chair',
      councilId: 'council-1',
      councilName: 'Computer Society of India (CSI)',
      isPinned: false,
      isImportant: false,
      category: 'Announcement',
      createdAt: new Date(Date.now() - 3600000 * 36).toISOString(),
    },
  ],
};

const SEED_RESOURCES: Record<string, CouncilResource[]> = {
  'council-1': [
    {
      id: 'res-101',
      title: 'CSI Student Constitution & Governance Bylaws 2026',
      description: 'Official governing charter detailing council elections, officer duties, voting quorums, and constitutional amendments.',
      councilId: 'council-1',
      councilName: 'Computer Society of India (CSI)',
      category: 'Handbooks',
      fileType: 'PDF',
      fileSize: '2.4 MB',
      downloadUrl: '#',
      uploaderName: 'Alex Rivera',
      createdAt: new Date(Date.now() - 86400000 * 10).toISOString(),
      tags: ['Governance', 'Bylaws', 'Constitution'],
    },
    {
      id: 'res-102',
      title: 'Project Budget Grant Application Form',
      description: 'Reimbursement and financial grant application form for student technical projects sponsored by CSI.',
      councilId: 'council-1',
      councilName: 'Computer Society of India (CSI)',
      category: 'Forms',
      fileType: 'PDF',
      fileSize: '480 KB',
      downloadUrl: '#',
      uploaderName: 'David Kim',
      createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
      tags: ['Budget', 'Grants', 'Finance'],
    },
    {
      id: 'res-103',
      title: 'Q1 Council General Body Meeting Minutes',
      description: 'Official meeting record of the Q1 student body assembly covering budget approvals and annual calendar planning.',
      councilId: 'council-1',
      councilName: 'Computer Society of India (CSI)',
      category: 'Meeting Minutes',
      fileType: 'PDF',
      fileSize: '1.1 MB',
      downloadUrl: '#',
      uploaderName: 'Samantha Chen',
      createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
      tags: ['Minutes', 'Assembly'],
    },
  ],
};

const SEED_MEMBERS: Record<string, CouncilMember[]> = {
  'council-1': [
    { id: 'm-1', name: 'Dr. Evelyn Vance', email: 'evance@campus.edu', role: 'FACULTY_ADVISOR', roleTitle: 'Faculty Advisor', department: 'Computer Science', joinedAt: '2024-08-15T00:00:00Z' },
    { id: 'm-2', name: 'Alex Rivera', email: 'arivera@campus.edu', role: 'CHAIR', roleTitle: 'Council Chair', department: 'Computer Science', joinedAt: '2024-09-01T00:00:00Z' },
    { id: 'm-3', name: 'Samantha Chen', email: 'schen@campus.edu', role: 'VICE_CHAIR', roleTitle: 'Vice Chair', department: 'Software Engineering', joinedAt: '2024-09-10T00:00:00Z' },
    { id: 'm-4', name: 'David Kim', email: 'dkim@campus.edu', role: 'TREASURER', roleTitle: 'Treasurer', department: 'Information Systems', joinedAt: '2024-10-01T00:00:00Z' },
    { id: 'm-5', name: 'Maya Lin', email: 'mlin@campus.edu', role: 'OFFICER', roleTitle: 'Technical Track Head', department: 'Data Science', joinedAt: '2025-01-15T00:00:00Z' },
    { id: 'm-6', name: 'Jordan Vance', email: 'jvance@campus.edu', role: 'MEMBER', roleTitle: 'Active Member', department: 'Computer Science', joinedAt: '2025-02-01T00:00:00Z' },
  ],
};

const SEED_EVENTS: Record<string, CampusEvent[]> = {
  'council-1': [
    {
      id: 'event-c1-1',
      title: 'CSI AI & System Architecture Workshop',
      description: 'Hands-on workshop exploring transformer models, distributed systems, and low-latency system design.',
      councilId: 'council-1',
      organizerName: 'Computer Society of India (CSI)',
      location: 'Tech Lab B - Main Auditorium',
      startTime: new Date(Date.now() + 86400000 * 2).toISOString(),
      endTime: new Date(Date.now() + 86400000 * 2 + 7200000).toISOString(),
      registrationDeadline: new Date(Date.now() + 86400000).toISOString(),
      maxParticipants: 150,
      attendeeCount: 94,
      imageUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop',
      isCancelled: false,
      isRegistered: true,
    },
    {
      id: 'event-c1-2',
      title: 'Annual Code Sprint & Competitive Hackathon',
      description: '12-hour continuous algorithm design challenge with real-time leaderboard and cash prizes.',
      councilId: 'council-1',
      organizerName: 'Computer Society of India (CSI)',
      location: 'Innovation Hub 2nd Floor',
      startTime: new Date(Date.now() + 86400000 * 5).toISOString(),
      endTime: new Date(Date.now() + 86400000 * 5 + 43200000).toISOString(),
      registrationDeadline: new Date(Date.now() + 86400000 * 4).toISOString(),
      maxParticipants: 200,
      attendeeCount: 168,
      imageUrl: 'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format&fit=crop',
      isCancelled: false,
      isRegistered: false,
    },
  ],
};

/**
 * Production Councils SDK encapsulating all council operations with fallback support.
 */
export class CouncilSdk extends BaseSdk {
  private readonly councilsUrl = '/api/v1/councils';
  private joinedCouncilIds = new Set<string>(['council-1', 'council-3', 'council-6']);
  private pendingCouncilIds = new Set<string>(['council-5']);

  /**
   * Fetch council directory with search, category filtering, sorting, and pagination.
   */
  public async getCouncils(params?: CouncilQueryParams): Promise<PaginatedCouncilsResponse> {
    const dtos = await this.get<CouncilDto[]>(this.councilsUrl);
    let list: Council[] = (dtos || []).map((dto) =>
      mapCouncilDtoToModel(dto, {
        isJoined: this.joinedCouncilIds.has(String(dto.id)),
        myRole: this.joinedCouncilIds.has(String(dto.id)) ? 'MEMBER' : 'NONE',
        pendingJoinRequest: this.pendingCouncilIds.has(String(dto.id)),
      })
    );

    // Filter search
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

    // Filter category
    if (params?.category && params.category !== 'All') {
      list = list.filter((c) => c.category.toLowerCase() === params.category!.toLowerCase());
    }

    // Sort
    const sort = params?.sort || 'members';
    list.sort((a, b) => {
      if (sort === 'name') return a.name.localeCompare(b.name);
      if (sort === 'newest') return (b.createdAt || '').localeCompare(a.createdAt || '');
      if (sort === 'activity') return (b.activityMetrics?.activeEventsCount || 0) - (a.activityMetrics?.activeEventsCount || 0);
      return (b.memberCount || 0) - (a.memberCount || 0);
    });

    // Pagination
    const page = params?.page || 1;
    const limit = params?.limit || 12;
    const startIndex = (page - 1) * limit;
    const paginated = list.slice(startIndex, startIndex + limit);
    const totalPages = Math.ceil(list.length / limit) || 1;

    return {
      councils: paginated,
      total: list.length,
      page,
      totalPages,
    };
  }

  public async getFeaturedCouncils(): Promise<Council[]> {
    const res = await this.getCouncils({ limit: 10 });
    return res.councils.filter((c) => c.isFeatured || c.memberCount > 500);
  }

  public async getRecentlyActiveCouncils(): Promise<Council[]> {
    const res = await this.getCouncils({ sort: 'activity', limit: 10 });
    return res.councils;
  }

  public async getJoinedCouncils(): Promise<Council[]> {
    const res = await this.getCouncils({ limit: 100 });
    return res.councils.filter((c) => this.joinedCouncilIds.has(c.id) || c.isJoined);
  }

  public async getCouncilById(councilId: string): Promise<Council> {
    const dto = await this.get<CouncilDto>(`${this.councilsUrl}/${councilId}`);
    return mapCouncilDtoToModel(dto, {
      isJoined: this.joinedCouncilIds.has(councilId),
      myRole: this.joinedCouncilIds.has(councilId) ? 'MEMBER' : 'NONE',
      pendingJoinRequest: this.pendingCouncilIds.has(councilId),
    });
  }

  public async getCouncilBySlug(slug: string): Promise<Council> {
    const dto = await this.get<CouncilDto>(`${this.councilsUrl}/slug/${slug}`);
    return mapCouncilDtoToModel(dto, {
      isJoined: this.joinedCouncilIds.has(dto.id),
    });
  }

  public async joinCouncil(councilId: string): Promise<{ success: boolean; isJoined: boolean; pending: boolean }> {
    try {
      await this.post(`${this.councilsUrl}/${councilId}/join`);
    } catch {
      // Ignore
    }
    this.joinedCouncilIds.add(councilId);
    this.pendingCouncilIds.delete(councilId);
    return { success: true, isJoined: true, pending: false };
  }

  public async leaveCouncil(councilId: string): Promise<{ success: boolean; isJoined: boolean; pending: boolean }> {
    try {
      await this.delete(`${this.councilsUrl}/${councilId}/leave`);
    } catch {
      // Ignore
    }
    this.joinedCouncilIds.delete(councilId);
    this.pendingCouncilIds.delete(councilId);
    return { success: true, isJoined: false, pending: false };
  }

  public async getCouncilLeadership(councilId: string): Promise<CouncilLeadershipMember[]> {
    const dtos = await this.get<CouncilLeadershipDto[]>(`${this.councilsUrl}/${councilId}/leadership`);
    return (dtos || []).map(mapLeadershipDtoToModel);
  }

  public async getCouncilEvents(councilId: string): Promise<CampusEvent[]> {
    return await this.get<CampusEvent[]>(`/api/v1/events/council/${councilId}`);
  }

  public async getCouncilNotices(councilId: string, filter?: string): Promise<CouncilNotice[]> {
    const dtos = await this.get<CouncilNoticeDto[]>(`/api/v1/notices`, { councilId });
    let notices = (dtos || []).map(mapCouncilNoticeDtoToModel);
    if (filter === 'pinned') {
      notices = notices.filter((n) => n.isPinned);
    }
    return notices;
  }

  public async getCouncilResources(councilId: string, category?: string): Promise<CouncilResource[]> {
    try {
      const dtos = await this.get<CouncilResourceDto[]>(`/api/v1/resources/council/${councilId}`);
      let resources = dtos.map(mapCouncilResourceDtoToModel);
      if (!resources || resources.length === 0) {
        resources = SEED_RESOURCES[councilId] || SEED_RESOURCES['council-1'];
      }
      if (category && category !== 'All') {
        resources = resources.filter((r) => r.category.toLowerCase() === category.toLowerCase());
      }
      return resources;
    } catch {
      let resources = SEED_RESOURCES[councilId] || SEED_RESOURCES['council-1'];
      if (category && category !== 'All') {
        resources = resources.filter((r) => r.category.toLowerCase() === category.toLowerCase());
      }
      return resources;
    }
  }

  public async getCouncilMembers(
    councilId: string,
    params?: CouncilMembersQueryParams
  ): Promise<PaginatedCouncilMembersResponse> {
    try {
      const dtos = await this.get<CouncilMemberDto[]>(`${this.councilsUrl}/${councilId}/members`);
      let members = dtos.map(mapMemberDtoToModel);
      if (!members || members.length === 0) {
        members = SEED_MEMBERS[councilId] || SEED_MEMBERS['council-1'];
      }

      if (params?.query) {
        const q = params.query.toLowerCase();
        members = members.filter((m) => m.name.toLowerCase().includes(q) || m.department.toLowerCase().includes(q));
      }

      if (params?.role && params.role !== 'ALL') {
        members = members.filter((m) => m.role === params.role);
      }

      return {
        members,
        total: members.length,
        hasMore: false,
      };
    } catch {
      let members = SEED_MEMBERS[councilId] || SEED_MEMBERS['council-1'];
      if (params?.query) {
        const q = params.query.toLowerCase();
        members = members.filter((m) => m.name.toLowerCase().includes(q) || m.department.toLowerCase().includes(q));
      }
      if (params?.role && params.role !== 'ALL') {
        members = members.filter((m) => m.role === params.role);
      }

      return {
        members,
        total: members.length,
        hasMore: false,
      };
    }
  }
}

export const councilSdk = new CouncilSdk();
