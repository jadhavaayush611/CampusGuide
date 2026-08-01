import { BaseSdk } from '../common/BaseSdk';
import { NotificationDto, UnreadCountDto } from './notification.dto';
import { mapNotificationDtoToModel } from './notification.mapper';
import {
  NotificationItem,
  NotificationQueryParams,
  NotificationStats,
} from '../../models/notification.model';

export class NotificationSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/notifications';

  // --- Core API Methods ---

  public async getNotifications(params?: NotificationQueryParams): Promise<NotificationItem[]> {
    let dtos: NotificationDto[] = [];
    const res = await this.get<any>(this.baseUrl);
    if (res && Array.isArray(res.content)) {
      dtos = res.content;
    } else if (Array.isArray(res)) {
      dtos = res;
    }

    let items = dtos.map((dto) => mapNotificationDtoToModel(dto));

    if (params) {
      const { search, category, status, priority, deliveryStatus, sortBy, sortOrder, page, limit } = params;

      if (status === 'UNREAD') {
        items = items.filter((i) => !i.isRead && !i.isArchived);
      } else if (status === 'READ') {
        items = items.filter((i) => i.isRead && !i.isArchived);
      } else if (status === 'ARCHIVED') {
        items = items.filter((i) => i.isArchived);
      } else {
        items = items.filter((i) => !i.isArchived);
      }

      if (category && category !== 'ALL') {
        items = items.filter((i) => i.category === category);
      }

      if (priority && priority !== 'ALL') {
        items = items.filter((i) => i.priority === priority);
      }

      if (deliveryStatus && deliveryStatus !== 'ALL') {
        items = items.filter((i) => i.deliveryStatus === deliveryStatus);
      }

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

      if (page && limit) {
        const start = (page - 1) * limit;
        items = items.slice(start, start + limit);
      }
    } else {
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
    const res = await this.get<UnreadCountDto>(`${this.baseUrl}/unread/count`);
    if (res && typeof res.count === 'number') {
      return res.count;
    }
    if (res && typeof (res as any).unreadCount === 'number') {
      return (res as any).unreadCount;
    }
    const all = await this.getNotifications({ status: 'UNREAD' });
    return all.length;
  }

  public async markAsRead(notificationId: string): Promise<void> {
    await this.patch<void>(`${this.baseUrl}/${notificationId}/read`);
  }

  public async markAsUnread(notificationId: string): Promise<void> {
    await this.patch<void>(`${this.baseUrl}/${notificationId}/unread`);
  }

  public async markAllAsRead(): Promise<void> {
    await this.patch<void>(`${this.baseUrl}/read-all`);
  }

  public async archiveNotification(notificationId: string): Promise<void> {
    await this.patch<void>(`${this.baseUrl}/${notificationId}/archive`);
  }

  public async restoreNotification(notificationId: string): Promise<void> {
    await this.patch<void>(`${this.baseUrl}/${notificationId}/restore`);
  }

  public async deleteNotification(notificationId: string): Promise<void> {
    await this.delete<void>(`${this.baseUrl}/${notificationId}`);
  }
}

export const notificationSdk = new NotificationSdk();

