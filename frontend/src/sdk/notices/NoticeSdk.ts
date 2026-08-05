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

export class NoticeSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/notices';

  private cachedReadNoticeIds: Set<string> | null = null;

  // --- Local Storage Read Persistence Helper ---

  public getReadNoticeIds(): Set<string> {
    if (this.cachedReadNoticeIds) {
      return this.cachedReadNoticeIds;
    }
    try {
      const stored = localStorage.getItem(READ_NOTICES_STORAGE_KEY);
      if (!stored) {
        this.cachedReadNoticeIds = new Set();
        return this.cachedReadNoticeIds;
      }
      const parsed = JSON.parse(stored);
      this.cachedReadNoticeIds = new Set(Array.isArray(parsed) ? parsed : []);
    } catch {
      this.cachedReadNoticeIds = new Set();
    }
    return this.cachedReadNoticeIds;
  }

  public saveReadNoticeIds(ids: Set<string>): void {
    this.cachedReadNoticeIds = ids;
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
    return 0;
  }

  // --- Core Notice Discovery & Operations ---

  public async getAllNotices(params?: NoticeQueryParams): Promise<Notice[]> {
    const dtos = await this.get<NoticeDto[]>(this.baseUrl, {
      includeUnpublished: params?.includeUnpublished || false,
    });

    const readIds = this.getReadNoticeIds();
    let notices = (dtos || []).map((dto) => mapNoticeDtoToModel(dto, readIds));

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
            notices = notices.filter(
              (n) => n.isPublished && n.expiresAt && new Date(n.expiresAt) <= now
            );
            break;
          case 'DRAFT':
            notices = notices.filter((n) => !n.isPublished);
            break;
          case 'ARCHIVED':
            notices = notices.filter((n) => !n.isPublished);
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
    const dto = await this.get<NoticeDto>(`${this.baseUrl}/${id}`);
    return mapNoticeDtoToModel(dto, this.getReadNoticeIds());
  }

  public async getNoticeBySlug(slug: string): Promise<Notice> {
    const dto = await this.get<NoticeDto>(`${this.baseUrl}/slug/${slug}`);
    return mapNoticeDtoToModel(dto, this.getReadNoticeIds());
  }

  // --- Notice Management ---

  public async createNotice(payload: CreateNoticePayload): Promise<Notice> {
    const dtoReq: CreateNoticeRequestDto = mapCreatePayloadToDto(payload);
    const createdDto = await this.post<NoticeDto>(this.baseUrl, dtoReq);
    return mapNoticeDtoToModel(createdDto, this.getReadNoticeIds());
  }

  public async updateNotice(id: string, payload: UpdateNoticePayload): Promise<Notice> {
    const dtoReq: UpdateNoticeRequestDto = mapUpdatePayloadToDto(payload);
    const updatedDto = await this.put<NoticeDto>(`${this.baseUrl}/${id}`, dtoReq);
    return mapNoticeDtoToModel(updatedDto, this.getReadNoticeIds());
  }

  public async publishNotice(id: string, isPublished: boolean): Promise<Notice> {
    const reqBody: PublishNoticeRequestDto = { isPublished };
    const updatedDto = await this.patch<NoticeDto>(`${this.baseUrl}/${id}/publish`, reqBody);
    return mapNoticeDtoToModel(updatedDto, this.getReadNoticeIds());
  }

  public async pinNotice(id: string, isPinned: boolean): Promise<Notice> {
    const reqBody: PinNoticeRequestDto = { isPinned };
    const updatedDto = await this.patch<NoticeDto>(`${this.baseUrl}/${id}/pin`, reqBody);
    return mapNoticeDtoToModel(updatedDto, this.getReadNoticeIds());
  }

  public async deleteNotice(id: string): Promise<void> {
    await this.delete<void>(`${this.baseUrl}/${id}`);
  }

  public async downloadAttachment(attachmentId: string, url: string): Promise<void> {
    if (url && url !== '#') {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  }
}

export const noticeSdk = new NoticeSdk();
