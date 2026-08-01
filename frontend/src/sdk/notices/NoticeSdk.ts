import { BaseSdk } from '../common/BaseSdk';
import { NoticeDto, CreateNoticeRequestDto, UpdateNoticeRequestDto, PublishNoticeRequestDto, PinNoticeRequestDto } from './notice.dto';
import { mapNoticeDtoToModel, mapCreatePayloadToDto, mapUpdatePayloadToDto } from './notice.mapper';
import {
  Notice,
  NoticeQueryParams,
  CreateNoticePayload,
  UpdateNoticePayload,
  NoticeReadStatus,
} from '../../models/notice.model';

const READ_NOTICES_STORAGE_KEY = 'campusguide_read_notices';

const SEED_NOTICES_DTO: NoticeDto[] = [
  {
    id: 'notice-1',
    title: 'Mid-Semester Examination Timetable & Regulations Spring 2026',
    slug: 'mid-semester-examination-timetable-spring-2026',
    content:
      'The mid-semester examinations for all undergraduate and postgraduate programs will commence on March 15, 2026. Hall tickets will be issued via student portals effective March 5th. Strict adherence to seating plans and examination hall protocols is required. Mobile phones, smartwatches, and programmable calculators are strictly prohibited.',
    summary: 'Mid-semester examinations commence March 15, 2026. Hall tickets available starting March 5th.',
    category: 'EXAM',
    priority: 'URGENT',
    visibility: 'STUDENTS',
    postedBy: 'Controller of Examinations',
    postedByRole: 'Exam Board',
    publishedAt: '2026-04-10T09:00:00Z',
    expiresAt: '2026-04-30T23:59:59Z',
    isPinned: true,
    isPublished: true,
    tags: ['examination', 'timetable', 'hall-ticket', 'spring-2026'],
    attachments: [
      {
        id: 'att-1-1',
        name: 'Mid_Sem_Exam_Timetable_2026.pdf',
        fileType: 'application/pdf',
        fileSize: '2.4 MB',
        url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
      },
      {
        id: 'att-1-2',
        name: 'Exam_Hall_Rules_and_Guidelines.pdf',
        fileType: 'application/pdf',
        fileSize: '850 KB',
        url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
      },
    ],
    createdAt: '2026-04-10T08:30:00Z',
    updatedAt: '2026-04-10T08:30:00Z',
  },
  {
    id: 'notice-2',
    title: 'Campus Central Library Wing B Renovation & Temporary Closure',
    slug: 'campus-central-library-wing-b-renovation',
    content:
      'The 3rd floor reading hall and Wing B of the Central Library will undergo scheduled electrical upgrades and digital workspace installation from April 20 to April 25. Silent study pods in the Innovation Hub will remain open 24/7 during this period to accommodate students.',
    summary: 'Library Wing B temporarily closed Apr 20-25 for digital upgrades. 24/7 study pods available at Innovation Hub.',
    category: 'ADMINISTRATIVE',
    priority: 'MEDIUM',
    visibility: 'PUBLIC',
    postedBy: 'Facilities Management',
    postedByRole: 'Estate & Infrastructure',
    publishedAt: '2026-04-09T14:30:00Z',
    expiresAt: '2026-04-26T23:59:59Z',
    isPinned: true,
    isPublished: true,
    tags: ['library', 'facilities', 'maintenance', 'study-spaces'],
    attachments: [
      {
        id: 'att-2-1',
        name: 'Alternative_Study_Spaces_Map.png',
        fileType: 'image/png',
        fileSize: '1.1 MB',
        url: 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=800&q=80',
      },
    ],
    createdAt: '2026-04-09T14:00:00Z',
    updatedAt: '2026-04-09T14:00:00Z',
  },
  {
    id: 'notice-3',
    title: 'Merit-Cum-Means Scholarship Scheme Application Deadline 2026-27',
    slug: 'merit-cum-means-scholarship-scheme-2026-27',
    content:
      'Applications are invited for the annual Merit-Cum-Means Scholarship for eligible B.Tech and M.Tech students. Applicants must submit verified income certificates along with academic grade sheets to the Financial Aid Section before April 30, 2026. Late submissions will not be considered under any circumstances.',
    summary: 'Submit MCM scholarship applications with income proof by April 30, 2026.',
    category: 'SCHOLARSHIP',
    priority: 'HIGH',
    visibility: 'STUDENTS',
    postedBy: 'Student Financial Aid Office',
    postedByRole: 'Scholarship Committee',
    publishedAt: '2026-04-08T11:15:00Z',
    expiresAt: '2026-05-01T23:59:59Z',
    isPinned: false,
    isPublished: true,
    tags: ['scholarship', 'financial-aid', 'merit', 'deadline'],
    attachments: [
      {
        id: 'att-3-1',
        name: 'MCM_Scholarship_Application_Form.pdf',
        fileType: 'application/pdf',
        fileSize: '1.8 MB',
        url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
      },
    ],
    createdAt: '2026-04-08T11:00:00Z',
    updatedAt: '2026-04-08T11:00:00Z',
  },
  {
    id: 'notice-4',
    title: 'Annual Campus Placement Drive 2026 — Phase 2 Company Registrations',
    slug: 'annual-campus-placement-drive-2026-phase-2',
    content:
      'Phase 2 of Campus Recruitment 2026 will host over 45 lead tech organizations including Microsoft, Amazon, Tata Consultancy Services, and Nvidia. All final-year students are requested to update their resumes and verify their academic CGPA records on the Placement Portal prior to April 15.',
    summary: 'Phase 2 Placement Drive featuring 45+ hiring partners. Update resume by April 15.',
    category: 'PLACEMENT',
    priority: 'HIGH',
    visibility: 'STUDENTS',
    postedBy: 'Training & Placement Officer',
    postedByRole: 'Career Development Cell',
    publishedAt: '2026-04-07T16:00:00Z',
    expiresAt: '2026-05-15T23:59:59Z',
    isPinned: false,
    isPublished: true,
    tags: ['placements', 'careers', 'jobs', 'internships', 'interviews'],
    attachments: [
      {
        id: 'att-4-1',
        name: 'Placement_Drive_Phase2_Schedule.pdf',
        fileType: 'application/pdf',
        fileSize: '3.1 MB',
        url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
      },
    ],
    createdAt: '2026-04-07T15:45:00Z',
    updatedAt: '2026-04-07T15:45:00Z',
  },
  {
    id: 'notice-5',
    title: 'TechFest 2026 Hackathon & Robotics Expo Registrations Open',
    slug: 'techfest-2026-hackathon-robotics-expo',
    content:
      'Technical Council presents TechFest 2026! Featuring a 36-hour AI Hackathon, Autonomous Drone Racing, and Competitive Coding Showdown. Cash prizes worth total ₹5,00,000. Register your teams before April 18th.',
    summary: '36-hour AI Hackathon & Robotics Expo with ₹5L total cash pool. Register by April 18.',
    category: 'EVENT',
    priority: 'MEDIUM',
    visibility: 'PUBLIC',
    postedBy: 'Technical Council Chair',
    postedByRole: 'Student Council',
    publishedAt: '2026-04-06T10:00:00Z',
    expiresAt: '2026-04-20T23:59:59Z',
    isPinned: false,
    isPublished: true,
    tags: ['events', 'techfest', 'hackathon', 'robotics', 'prizes'],
    attachments: [
      {
        id: 'att-5-1',
        name: 'TechFest_2026_Rulebook.pdf',
        fileType: 'application/pdf',
        fileSize: '4.2 MB',
        url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
      },
    ],
    createdAt: '2026-04-06T09:30:00Z',
    updatedAt: '2026-04-06T09:30:00Z',
  },
  {
    id: 'notice-6',
    title: 'Updated Policy on Elective Subject Selection & Credit Transfer',
    slug: 'updated-policy-on-elective-subject-selection',
    content:
      'The Academic Senate has approved revisions to open elective selection guidelines for 5th and 7th semester students. Online courses from NPTEL/Coursera equivalent to 3 credits can now be transferred subject to prior HOD approval.',
    summary: 'NPTEL & MOOC credit transfer policy updated. Max 6 credits allowed via online electives.',
    category: 'ACADEMIC',
    priority: 'MEDIUM',
    visibility: 'STUDENTS',
    postedBy: 'Dean of Academics',
    postedByRole: 'Academic Senate',
    publishedAt: '2026-04-05T12:00:00Z',
    expiresAt: '2026-06-30T23:59:59Z',
    isPinned: false,
    isPublished: true,
    tags: ['academics', 'electives', 'credits', 'policy'],
    attachments: [],
    createdAt: '2026-04-05T11:30:00Z',
    updatedAt: '2026-04-05T11:30:00Z',
  },
  {
    id: 'notice-7',
    title: 'Student Council General Election Nominations 2026',
    slug: 'student-council-general-election-nominations-2026',
    content:
      'Official notification for the election of Office Bearers for the General Student Council. Nominees must submit Form A to the Dean of Student Affairs office by April 14, 2026.',
    summary: 'Nominations open for Student Council General Elections 2026. Submit forms by April 14.',
    category: 'OTHER',
    priority: 'HIGH',
    visibility: 'STUDENTS',
    postedBy: 'Election Officer',
    postedByRole: 'Student Affairs',
    publishedAt: '2026-04-04T08:00:00Z',
    expiresAt: '2026-04-15T23:59:59Z',
    isPinned: false,
    isPublished: true,
    tags: ['councils', 'elections', 'student-body', 'nominations'],
    attachments: [
      {
        id: 'att-7-1',
        name: 'Nomination_Form_A.pdf',
        fileType: 'application/pdf',
        fileSize: '512 KB',
        url: 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf',
      },
    ],
    createdAt: '2026-04-04T07:45:00Z',
    updatedAt: '2026-04-04T07:45:00Z',
  },
  {
    id: 'notice-8',
    title: 'Winter Semester Grade Re-evaluation Results & Score Verification',
    slug: 'winter-semester-grade-reevaluation-results-2025',
    content:
      'Grade re-evaluation requests submitted for the Winter 2025 semester have been processed. Updated transcripts are available for download on the Student ERP Portal.',
    summary: 'Winter 2025 re-evaluation results published on Student ERP.',
    category: 'EXAM',
    priority: 'LOW',
    visibility: 'STUDENTS',
    postedBy: 'Controller of Examinations',
    postedByRole: 'Exam Board',
    publishedAt: '2026-03-28T10:00:00Z',
    expiresAt: '2026-04-01T00:00:00Z', // Expired notice example
    isPinned: false,
    isPublished: true,
    tags: ['grades', 'results', 'reevaluation', 'archived'],
    attachments: [],
    createdAt: '2026-03-28T09:00:00Z',
    updatedAt: '2026-03-28T09:00:00Z',
  },
];

export class NoticeSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/notices';
  private localNotices: NoticeDto[] = [...SEED_NOTICES_DTO];

  // --- Local Storage Read Persistence Helper ---

  public getReadNoticeIds(): Set<string> {
    try {
      const stored = localStorage.getItem(READ_NOTICES_STORAGE_KEY);
      if (!stored) return new Set();
      const parsed = JSON.parse(stored);
      return new Set(Array.isArray(parsed) ? parsed : []);
    } catch {
      return new Set();
    }
  }

  public saveReadNoticeIds(ids: Set<string>): void {
    try {
      localStorage.setItem(READ_NOTICES_STORAGE_KEY, JSON.stringify(Array.from(ids)));
    } catch {
      // ignore quota / security error
    }
  }

  public markAsRead(noticeId: string): NoticeReadStatus {
    const ids = this.getReadNoticeIds();
    ids.add(noticeId);
    this.saveReadNoticeIds(ids);
    return { noticeId, isRead: true, readAt: new Date().toISOString() };
  }

  public markAsUnread(noticeId: string): NoticeReadStatus {
    const ids = this.getReadNoticeIds();
    ids.delete(noticeId);
    this.saveReadNoticeIds(ids);
    return { noticeId, isRead: false };
  }

  public getUnreadCount(notices?: Notice[]): number {
    const readIds = this.getReadNoticeIds();
    if (notices) {
      return notices.filter((n) => !readIds.has(n.id)).length;
    }
    return this.localNotices.filter((n) => !readIds.has(n.id)).length;
  }

  // --- Core Notice Discovery & Operations ---

  public async getAllNotices(params?: NoticeQueryParams): Promise<Notice[]> {
    let dtos: NoticeDto[] = [];
    try {
      dtos = await this.get<NoticeDto[]>(this.baseUrl, {
        includeUnpublished: params?.includeUnpublished || false,
      });
      if (!dtos || dtos.length === 0) {
        dtos = this.localNotices;
      }
    } catch {
      // Backend fallback to seed data
      dtos = this.localNotices;
    }

    const readIds = this.getReadNoticeIds();
    let notices = dtos.map((dto) => mapNoticeDtoToModel(dto, readIds));

    // Client-side filtering & search
    if (params) {
      const { search, category, priority, status, publisher, tags, isPinned } = params;

      if (search && search.trim() !== '') {
        const query = search.toLowerCase().trim();
        notices = notices.filter(
          (n) =>
            n.title.toLowerCase().includes(query) ||
            n.content.toLowerCase().includes(query) ||
            (n.summary && n.summary.toLowerCase().includes(query)) ||
            n.postedBy.toLowerCase().includes(query) ||
            n.tags.some((t) => t.toLowerCase().includes(query))
        );
      }

      if (category && category !== 'ALL') {
        notices = notices.filter((n) => n.category === category);
      }

      if (priority && priority !== 'ALL') {
        notices = notices.filter((n) => n.priority === priority);
      }

      if (publisher && publisher.trim() !== '') {
        const pub = publisher.toLowerCase().trim();
        notices = notices.filter((n) => n.postedBy.toLowerCase().includes(pub));
      }

      if (tags && tags.length > 0) {
        notices = notices.filter((n) => tags.some((t) => n.tags.includes(t)));
      }

      if (isPinned !== undefined) {
        notices = notices.filter((n) => n.isPinned === isPinned);
      }

      if (status) {
        const now = new Date();
        switch (status) {
          case 'ACTIVE':
            notices = notices.filter(
              (n) => n.isPublished && (!n.expiresAt || new Date(n.expiresAt) > now)
            );
            break;
          case 'EXPIRED':
          case 'ARCHIVED':
            notices = notices.filter(
              (n) => !n.isPublished || (n.expiresAt && new Date(n.expiresAt) <= now)
            );
            break;
          case 'UNREAD':
            notices = notices.filter((n) => !n.isRead);
            break;
        }
      }

      // Sorting
      const sortBy = params.sortBy || 'publishedAt';
      const sortOrder = params.sortOrder || 'desc';
      notices.sort((a, b) => {
        // Pinned notices stay on top by default unless specific sorting requested
        if (a.isPinned !== b.isPinned && sortBy === 'publishedAt') {
          return a.isPinned ? -1 : 1;
        }

        let comparison = 0;
        if (sortBy === 'publishedAt') {
          comparison = new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime();
        } else if (sortBy === 'priority') {
          const weightMap = { URGENT: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
          comparison = weightMap[b.priority] - weightMap[a.priority];
        } else if (sortBy === 'title') {
          comparison = a.title.localeCompare(b.title);
        } else if (sortBy === 'createdAt') {
          comparison = new Date(b.createdAt || b.publishedAt).getTime() - new Date(a.createdAt || a.publishedAt).getTime();
        }

        return sortOrder === 'asc' ? -comparison : comparison;
      });

      // Pagination
      if (params.page && params.limit) {
        const start = (params.page - 1) * params.limit;
        notices = notices.slice(start, start + params.limit);
      }
    }

    return notices;
  }

  public async getPinnedNotices(): Promise<Notice[]> {
    return this.getAllNotices({ isPinned: true, status: 'ACTIVE' });
  }

  public async getRecentNotices(): Promise<Notice[]> {
    return this.getAllNotices({ sortBy: 'publishedAt', sortOrder: 'desc', limit: 5 });
  }

  public async getImportantNotices(): Promise<Notice[]> {
    const notices = await this.getAllNotices({ status: 'ACTIVE' });
    return notices.filter((n) => n.isImportant);
  }

  public async getArchivedNotices(): Promise<Notice[]> {
    return this.getAllNotices({ status: 'ARCHIVED' });
  }

  public async getNoticeById(id: string): Promise<Notice> {
    try {
      const dto = await this.get<NoticeDto>(`${this.baseUrl}/${id}`);
      return mapNoticeDtoToModel(dto, this.getReadNoticeIds());
    } catch {
      const local = this.localNotices.find((n) => n.id === id);
      if (!local) {
        throw new Error(`Notice not found with ID ${id}`);
      }
      return mapNoticeDtoToModel(local, this.getReadNoticeIds());
    }
  }

  public async getNoticeBySlug(slug: string): Promise<Notice> {
    try {
      const dto = await this.get<NoticeDto>(`${this.baseUrl}/slug/${slug}`);
      return mapNoticeDtoToModel(dto, this.getReadNoticeIds());
    } catch {
      const local = this.localNotices.find((n) => n.slug === slug);
      if (!local) {
        throw new Error(`Notice not found with slug ${slug}`);
      }
      return mapNoticeDtoToModel(local, this.getReadNoticeIds());
    }
  }

  // --- Notice Management ---

  public async createNotice(payload: CreateNoticePayload): Promise<Notice> {
    const dtoReq: CreateNoticeRequestDto = mapCreatePayloadToDto(payload);
    let createdDto: NoticeDto;
    try {
      createdDto = await this.post<NoticeDto>(this.baseUrl, dtoReq);
    } catch {
      // Local fallback creation
      createdDto = {
        ...dtoReq,
        id: `notice-${Date.now()}`,
        postedBy: 'Office of Academic Affairs',
        postedByRole: 'Admin',
        publishedAt: dtoReq.publishedAt || new Date().toISOString(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
    }
    this.localNotices.unshift(createdDto);
    return mapNoticeDtoToModel(createdDto, this.getReadNoticeIds());
  }

  public async updateNotice(id: string, payload: UpdateNoticePayload): Promise<Notice> {
    const dtoReq: UpdateNoticeRequestDto = mapUpdatePayloadToDto(payload);
    let updatedDto: NoticeDto;
    try {
      updatedDto = await this.put<NoticeDto>(`${this.baseUrl}/${id}`, dtoReq);
    } catch {
      const index = this.localNotices.findIndex((n) => n.id === id);
      if (index === -1) throw new Error(`Notice with ID ${id} not found.`);
      updatedDto = {
        ...this.localNotices[index],
        ...dtoReq,
        updatedAt: new Date().toISOString(),
      };
    }
    const idx = this.localNotices.findIndex((n) => n.id === id);
    if (idx !== -1) {
      this.localNotices[idx] = updatedDto;
    }
    return mapNoticeDtoToModel(updatedDto, this.getReadNoticeIds());
  }

  public async publishNotice(id: string, isPublished: boolean): Promise<Notice> {
    const reqBody: PublishNoticeRequestDto = { isPublished };
    let updatedDto: NoticeDto;
    try {
      updatedDto = await this.patch<NoticeDto>(`${this.baseUrl}/${id}/publish`, reqBody);
    } catch {
      const index = this.localNotices.findIndex((n) => n.id === id);
      if (index === -1) throw new Error(`Notice with ID ${id} not found.`);
      updatedDto = {
        ...this.localNotices[index],
        isPublished,
        updatedAt: new Date().toISOString(),
      };
    }
    const idx = this.localNotices.findIndex((n) => n.id === id);
    if (idx !== -1) {
      this.localNotices[idx] = updatedDto;
    }
    return mapNoticeDtoToModel(updatedDto, this.getReadNoticeIds());
  }

  public async pinNotice(id: string, isPinned: boolean): Promise<Notice> {
    const reqBody: PinNoticeRequestDto = { isPinned };
    let updatedDto: NoticeDto;
    try {
      updatedDto = await this.patch<NoticeDto>(`${this.baseUrl}/${id}/pin`, reqBody);
    } catch {
      const index = this.localNotices.findIndex((n) => n.id === id);
      if (index === -1) throw new Error(`Notice with ID ${id} not found.`);
      updatedDto = {
        ...this.localNotices[index],
        isPinned,
        updatedAt: new Date().toISOString(),
      };
    }
    const idx = this.localNotices.findIndex((n) => n.id === id);
    if (idx !== -1) {
      this.localNotices[idx] = updatedDto;
    }
    return mapNoticeDtoToModel(updatedDto, this.getReadNoticeIds());
  }

  public async deleteNotice(id: string): Promise<void> {
    try {
      await this.delete<void>(`${this.baseUrl}/${id}`);
    } catch {
      // Local fallback removal
    }
    this.localNotices = this.localNotices.filter((n) => n.id !== id);
  }

  public async downloadAttachment(attachmentId: string, url: string): Promise<void> {
    if (url && url !== '#') {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  }
}

export const noticeSdk = new NoticeSdk();
