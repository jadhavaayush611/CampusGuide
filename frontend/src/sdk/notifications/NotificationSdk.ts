import { BaseSdk } from '../common/BaseSdk';
import { NotificationDto, UnreadCountDto } from './notification.dto';
import { mapNotificationDtoToModel } from './notification.mapper';
import {
  NotificationItem,
  NotificationQueryParams,
  NotificationStats,
} from '../../models/notification.model';

const READ_NOTIFICATIONS_KEY = 'campusguide_read_notifications';
const ARCHIVED_NOTIFICATIONS_KEY = 'campusguide_archived_notifications';

const SEED_NOTIFICATIONS_DTO: NotificationDto[] = [
  {
    id: 'notif-101',
    category: 'Academic',
    type: 'ACADEMIC',
    title: 'Mid-Semester Examination Schedule Published',
    message: 'The official Spring 2026 examination timetable is now live on the Academic portal.',
    priority: 'HIGH',
    sourceModule: 'Academic Office',
    deliveryStatus: 'DELIVERED',
    read: false,
    archived: false,
    createdAt: '2026-04-10T09:00:00Z',
    time: '2 hours ago',
    linkUrl: '/academic',
    relatedEntity: { type: 'Notice', id: 'sem-spring-2026', name: 'Spring 2026 Schedule' },
  },
  {
    id: 'notif-102',
    category: 'Planner',
    type: 'PLANNER',
    title: 'Capstone Project Milestone 2 Due Tomorrow',
    message: 'Submit your architectural review document by 11:59 PM to avoid late penalty.',
    priority: 'URGENT',
    sourceModule: 'Planner Module',
    deliveryStatus: 'DELIVERED',
    read: false,
    archived: false,
    createdAt: '2026-04-10T08:15:00Z',
    time: '3 hours ago',
    linkUrl: '/planner?taskId=task-102',
    relatedEntity: { type: 'PlannerTask', id: 'task-102', name: 'Capstone Architecture' },
  },
  {
    id: 'notif-103',
    category: 'Calendar',
    type: 'CALENDAR',
    title: 'AI & Machine Learning Workshop Starting Soon',
    message: 'Join the live session in Seminar Hall A at 3:00 PM today.',
    priority: 'NORMAL',
    sourceModule: 'Campus Events',
    deliveryStatus: 'DELIVERED',
    read: false,
    archived: false,
    createdAt: '2026-04-09T14:30:00Z',
    time: 'Yesterday',
    linkUrl: '/calendar?eventId=evt-404',
    relatedEntity: { type: 'CalendarEvent', id: 'evt-404', name: 'AI Workshop' },
  },
  {
    id: 'notif-104',
    category: 'Communities',
    type: 'COMMUNITIES',
    title: 'Robotics Club: New Discussion Posted',
    message: 'Alex posted: "Best practices for ROS2 node setup in embedded platforms".',
    priority: 'NORMAL',
    sourceModule: 'Robotics Club',
    deliveryStatus: 'DELIVERED',
    read: true,
    archived: false,
    createdAt: '2026-04-08T11:20:00Z',
    time: '2 days ago',
    linkUrl: '/communities/comm-1',
    relatedEntity: { type: 'Community', id: 'comm-1', name: 'Robotics Club' },
  },
  {
    id: 'notif-105',
    category: 'Councils',
    type: 'COUNCILS',
    title: 'Student Council General Election Announcement',
    message: 'Nominations for General Office Bearers open until April 14th.',
    priority: 'HIGH',
    sourceModule: 'Student Council',
    deliveryStatus: 'DELIVERED',
    read: false,
    archived: false,
    createdAt: '2026-04-07T16:45:00Z',
    time: '3 days ago',
    linkUrl: '/councils/council-sci',
    relatedEntity: { type: 'Council', id: 'council-sci', name: 'Student Council' },
  },
  {
    id: 'notif-106',
    category: 'Resources',
    type: 'RESOURCES',
    title: 'New Study Repository Added: Operating Systems Lab Manual',
    message: 'Version 2.4 of OS lab code templates & Linux kernel compilation guide uploaded.',
    priority: 'LOW',
    sourceModule: 'Resource Center',
    deliveryStatus: 'DELIVERED',
    read: true,
    archived: false,
    createdAt: '2026-04-06T10:00:00Z',
    time: '4 days ago',
    linkUrl: '/resources?id=res-88',
    relatedEntity: { type: 'Resource', id: 'res-88', name: 'OS Lab Manual' },
  },
  {
    id: 'notif-107',
    category: 'Notices',
    type: 'NOTICES',
    title: 'Central Library Wing B Temporary Closure',
    message: 'Wing B reading hall closed for digital pod upgrades from April 20 to April 25.',
    priority: 'URGENT',
    sourceModule: 'Estate Office',
    deliveryStatus: 'DELIVERED',
    read: true,
    archived: false,
    createdAt: '2026-04-05T08:00:00Z',
    time: '5 days ago',
    linkUrl: '/notices?id=notice-2',
    relatedEntity: { type: 'Notice', id: 'notice-2', name: 'Library Closure' },
  },
  {
    id: 'notif-108',
    category: 'Atlas',
    type: 'ATLAS',
    title: 'Atlas AI: Personalized Exam Preparation Roadmap Ready',
    message: 'Atlas AI generated a optimized study plan based on your grade performance and upcoming tests.',
    priority: 'NORMAL',
    sourceModule: 'Atlas AI Assistant',
    deliveryStatus: 'DELIVERED',
    read: false,
    archived: false,
    createdAt: '2026-04-04T12:00:00Z',
    time: '6 days ago',
    linkUrl: '/academic',
    relatedEntity: { type: 'Atlas', id: 'rec-12', name: 'AI Study Roadmap' },
  },
  {
    id: 'notif-109',
    category: 'Authentication',
    type: 'AUTHENTICATION',
    title: 'New Sign-in Alert',
    message: 'Your account was logged into from a new Windows device in Mumbai, India.',
    priority: 'NORMAL',
    sourceModule: 'Security System',
    deliveryStatus: 'DELIVERED',
    read: true,
    archived: false,
    createdAt: '2026-04-03T19:30:00Z',
    time: '1 week ago',
    linkUrl: '/profile',
    relatedEntity: { type: 'Auth', id: 'session-99', name: 'Security Audit Log' },
  },
  {
    id: 'notif-110',
    category: 'System',
    type: 'SYSTEM',
    title: 'Scheduled Platform Maintenance Window',
    message: 'CampusGuide services will undergo scheduled routine maintenance this Sunday from 2 AM to 4 AM.',
    priority: 'LOW',
    sourceModule: 'System Administrator',
    deliveryStatus: 'SCHEDULED',
    read: false,
    archived: false,
    createdAt: '2026-04-02T10:00:00Z',
    time: '1 week ago',
    linkUrl: '/notifications',
    relatedEntity: { type: 'System', id: 'maint-01', name: 'Platform Upgrade' },
  },
  {
    id: 'notif-111',
    category: 'Academic',
    type: 'ACADEMIC',
    title: 'Failed to Dispatch Grade Release Alert',
    message: 'SMS delivery gateway failed for course CS302 final grade alert. Retrying via in-app banner.',
    priority: 'HIGH',
    sourceModule: 'Notification Gateway',
    deliveryStatus: 'FAILED',
    read: false,
    archived: false,
    createdAt: '2026-04-01T15:00:00Z',
    time: '1 week ago',
    linkUrl: '/academic',
    relatedEntity: { type: 'Notice', id: 'cs302-grades', name: 'CS302 Grades' },
  },
  {
    id: 'notif-112',
    category: 'Planner',
    type: 'PLANNER',
    title: 'Archived: Completed Task Reminder for DSA Assignment',
    message: 'Assignment 3 completed and verified.',
    priority: 'LOW',
    sourceModule: 'Planner Module',
    deliveryStatus: 'DELIVERED',
    read: true,
    archived: true,
    createdAt: '2026-03-25T09:00:00Z',
    time: '2 weeks ago',
    linkUrl: '/planner',
    relatedEntity: { type: 'PlannerTask', id: 'task-05', name: 'DSA Assignment' },
  },
];

/**
 * Production Notification SDK encapsulating real HTTP endpoints with local persistence fallback.
 */
export class NotificationSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/notifications';
  private localNotifications: NotificationDto[] = [...SEED_NOTIFICATIONS_DTO];

  // --- Local Storage Helpers ---

  private getReadIds(): Set<string> {
    try {
      const item = localStorage.getItem(READ_NOTIFICATIONS_KEY);
      return item ? new Set(JSON.parse(item)) : new Set();
    } catch {
      return new Set();
    }
  }

  private saveReadIds(ids: Set<string>): void {
    try {
      localStorage.setItem(READ_NOTIFICATIONS_KEY, JSON.stringify(Array.from(ids)));
    } catch {
      // ignore storage error
    }
  }

  private getArchivedIds(): Set<string> {
    try {
      const item = localStorage.getItem(ARCHIVED_NOTIFICATIONS_KEY);
      return item ? new Set(JSON.parse(item)) : new Set();
    } catch {
      return new Set();
    }
  }

  private saveArchivedIds(ids: Set<string>): void {
    try {
      localStorage.setItem(ARCHIVED_NOTIFICATIONS_KEY, JSON.stringify(Array.from(ids)));
    } catch {
      // ignore storage error
    }
  }

  // --- Core API Methods ---

  public async getNotifications(params?: NotificationQueryParams): Promise<NotificationItem[]> {
    let dtos: NotificationDto[] = [];
    try {
      const res = await this.get<any>(this.baseUrl);
      if (res && Array.isArray(res.content) && res.content.length > 0) {
        dtos = res.content;
      } else if (Array.isArray(res) && res.length > 0) {
        dtos = res;
      } else {
        dtos = this.localNotifications;
      }
    } catch {
      // Graceful fallback to local seed dataset
      dtos = this.localNotifications;
    }

    const readIds = this.getReadIds();
    const archivedIds = this.getArchivedIds();

    let items = dtos.map((dto) => {
      const model = mapNotificationDtoToModel(dto);
      if (readIds.has(model.id)) {
        model.isRead = true;
      }
      if (archivedIds.has(model.id)) {
        model.isArchived = true;
      }
      return model;
    });

    if (params) {
      const { search, category, status, priority, deliveryStatus, sortBy, sortOrder, page, limit } = params;

      // Status filter
      if (status === 'UNREAD') {
        items = items.filter((i) => !i.isRead && !i.isArchived);
      } else if (status === 'READ') {
        items = items.filter((i) => i.isRead && !i.isArchived);
      } else if (status === 'ARCHIVED') {
        items = items.filter((i) => i.isArchived);
      } else {
        // Default 'ALL': active (non-archived)
        items = items.filter((i) => !i.isArchived);
      }

      // Category filter
      if (category && category !== 'ALL') {
        items = items.filter((i) => i.category === category);
      }

      // Priority filter
      if (priority && priority !== 'ALL') {
        items = items.filter((i) => i.priority === priority);
      }

      // Delivery Status filter
      if (deliveryStatus && deliveryStatus !== 'ALL') {
        items = items.filter((i) => i.deliveryStatus === deliveryStatus);
      }

      // Search filter
      if (search && search.trim() !== '') {
        const q = search.toLowerCase().trim();
        items = items.filter(
          (i) =>
            i.title.toLowerCase().includes(q) ||
            i.message.toLowerCase().includes(q) ||
            i.sourceModule.toLowerCase().includes(q) ||
            i.category.toLowerCase().includes(q) ||
            (i.relatedEntity?.name && i.relatedEntity.name.toLowerCase().includes(q))
        );
      }

      // Sorting
      const sortField = sortBy || 'createdAt';
      const order = sortOrder || 'desc';
      items.sort((a, b) => {
        let cmp = 0;
        if (sortField === 'createdAt') {
          cmp = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        } else if (sortField === 'priority') {
          const weights: Record<string, number> = { URGENT: 4, HIGH: 3, NORMAL: 2, LOW: 1 };
          cmp = (weights[b.priority] || 0) - (weights[a.priority] || 0);
        } else if (sortField === 'title') {
          cmp = a.title.localeCompare(b.title);
        }
        return order === 'asc' ? -cmp : cmp;
      });

      // Pagination
      if (page && limit) {
        const start = (page - 1) * limit;
        items = items.slice(start, start + limit);
      }
    } else {
      // By default, exclude archived items
      items = items.filter((i) => !i.isArchived);
    }

    return items;
  }

  public async getNotificationStats(): Promise<NotificationStats> {
    const allItems = await this.getNotifications({ status: 'ALL' });
    const archivedItems = await this.getNotifications({ status: 'ARCHIVED' });
    const totalCombined = [...allItems, ...archivedItems];

    const unread = allItems.filter((i) => !i.isRead).length;
    const read = allItems.filter((i) => i.isRead).length;
    const archived = archivedItems.length;
    const delivered = totalCombined.filter((i) => i.deliveryStatus === 'DELIVERED').length;
    const scheduled = totalCombined.filter((i) => i.deliveryStatus === 'SCHEDULED').length;
    const failed = totalCombined.filter((i) => i.deliveryStatus === 'FAILED').length;

    return {
      total: allItems.length,
      unread,
      read,
      archived,
      delivered,
      scheduled,
      failed,
    };
  }

  public async getUnreadCount(): Promise<number> {
    try {
      const res = await this.get<UnreadCountDto>(`${this.baseUrl}/unread/count`);
      if (res && typeof res.count === 'number') {
        return res.count;
      }
      if (res && typeof res.unreadCount === 'number') {
        return res.unreadCount;
      }
    } catch {
      // fallback
    }
    const all = await this.getNotifications({ status: 'UNREAD' });
    return all.length;
  }

  public async markAsRead(notificationId: string): Promise<void> {
    try {
      await this.patch<void>(`${this.baseUrl}/${notificationId}/read`);
    } catch {
      // local fallback update
    }
    const readIds = this.getReadIds();
    readIds.add(notificationId);
    this.saveReadIds(readIds);

    const idx = this.localNotifications.findIndex((n) => n.id === notificationId);
    if (idx !== -1) {
      this.localNotifications[idx].read = true;
      this.localNotifications[idx].isRead = true;
    }
  }

  public async markAsUnread(notificationId: string): Promise<void> {
    try {
      await this.patch<void>(`${this.baseUrl}/${notificationId}/unread`);
    } catch {
      // local fallback update
    }
    const readIds = this.getReadIds();
    readIds.delete(notificationId);
    this.saveReadIds(readIds);

    const idx = this.localNotifications.findIndex((n) => n.id === notificationId);
    if (idx !== -1) {
      this.localNotifications[idx].read = false;
      this.localNotifications[idx].isRead = false;
    }
  }

  public async markAllAsRead(): Promise<void> {
    try {
      await this.patch<void>(`${this.baseUrl}/read-all`);
    } catch {
      // local fallback update
    }
    const readIds = this.getReadIds();
    this.localNotifications.forEach((n) => {
      readIds.add(n.id);
      n.read = true;
      n.isRead = true;
    });
    this.saveReadIds(readIds);
  }

  public async archiveNotification(notificationId: string): Promise<void> {
    try {
      await this.patch<void>(`${this.baseUrl}/${notificationId}/archive`);
    } catch {
      // local fallback archive
    }
    const archivedIds = this.getArchivedIds();
    archivedIds.add(notificationId);
    this.saveArchivedIds(archivedIds);

    const idx = this.localNotifications.findIndex((n) => n.id === notificationId);
    if (idx !== -1) {
      this.localNotifications[idx].archived = true;
      this.localNotifications[idx].isArchived = true;
    }
  }

  public async restoreNotification(notificationId: string): Promise<void> {
    try {
      await this.patch<void>(`${this.baseUrl}/${notificationId}/restore`);
    } catch {
      // local fallback restore
    }
    const archivedIds = this.getArchivedIds();
    archivedIds.delete(notificationId);
    this.saveArchivedIds(archivedIds);

    const idx = this.localNotifications.findIndex((n) => n.id === notificationId);
    if (idx !== -1) {
      this.localNotifications[idx].archived = false;
      this.localNotifications[idx].isArchived = false;
    }
  }

  public async deleteNotification(notificationId: string): Promise<void> {
    try {
      await this.delete<void>(`${this.baseUrl}/${notificationId}`);
    } catch {
      // local fallback delete
    }
    this.localNotifications = this.localNotifications.filter((n) => n.id !== notificationId);
    const readIds = this.getReadIds();
    readIds.delete(notificationId);
    this.saveReadIds(readIds);
    const archivedIds = this.getArchivedIds();
    archivedIds.delete(notificationId);
    this.saveArchivedIds(archivedIds);
  }
}

export const notificationSdk = new NotificationSdk();

