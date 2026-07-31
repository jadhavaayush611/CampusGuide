import { BaseSdk } from '../common/BaseSdk';
import { NotificationDto, UnreadCountDto } from './notification.dto';
import { mapNotificationDtoToModel } from './notification.mapper';
import { NotificationItem } from '../../models/notification.model';

const FALLBACK_NOTIFICATIONS: NotificationItem[] = [
  {
    id: 'notif-1',
    type: 'reminder',
    title: 'HackFest 2026 starting soon',
    description: 'Event starts today at 9:00 AM in CS Block',
    time: '2h ago',
    isRead: false,
    linkUrl: '/resources',
  },
  {
    id: 'notif-2',
    type: 'mention',
    title: 'New announcement from Student Council',
    description: 'Mid-Semester Exam Schedule Released',
    time: '5h ago',
    isRead: false,
    linkUrl: '/notices',
  },
  {
    id: 'notif-3',
    type: 'reminder',
    title: 'Mental Health Workshop tomorrow',
    description: "Don't forget to attend at 3:00 PM in Seminar Hall A",
    time: '1d ago',
    isRead: true,
    linkUrl: '/councils',
  },
  {
    id: 'notif-4',
    type: 'event',
    title: 'Spring Music Night this Friday',
    description: 'Main Auditorium, 6:00 PM',
    time: '2d ago',
    isRead: true,
    linkUrl: '/councils',
  },
];

/**
 * Production Notification SDK encapsulating real HTTP endpoints with placeholder fallback strategy.
 */
export class NotificationSdk extends BaseSdk {
  private readonly baseUrl = '/api/notifications';

  public async getNotifications(): Promise<NotificationItem[]> {
    try {
      const dtos = await this.get<NotificationDto[]>(this.baseUrl);
      if (Array.isArray(dtos) && dtos.length > 0) {
        return dtos.map(mapNotificationDtoToModel);
      }
      return FALLBACK_NOTIFICATIONS;
    } catch {
      // Graceful fallback to placeholders if backend endpoints are pending
      return FALLBACK_NOTIFICATIONS;
    }
  }

  public async getUnreadCount(): Promise<number> {
    try {
      const res = await this.get<UnreadCountDto>(`${this.baseUrl}/unread-count`);
      return res.unreadCount ?? 2;
    } catch {
      const all = await this.getNotifications();
      return all.filter((item) => !item.isRead).length;
    }
  }

  public async markAsRead(notificationId: string): Promise<void> {
    try {
      await this.post<void>(`${this.baseUrl}/${notificationId}/read`);
    } catch {
      // Optimistic silent handling
    }
  }
}

export const notificationSdk = new NotificationSdk();
