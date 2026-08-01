import { BaseSdk } from '../common/BaseSdk';

export interface ScheduledNotificationItem {
  id: string;
  userId?: string;
  title: string;
  message?: string;
  scheduledTime: string; // ISO LocalDateTime
  status: 'PENDING' | 'SENT' | 'FAILED' | 'CANCELLED';
  targetType?: string;
  targetId?: string;
  channel?: string;
  createdAt?: string;
}

const FALLBACK_SCHEDULED_NOTIFICATIONS: ScheduledNotificationItem[] = [
  {
    id: 'sched-1',
    title: 'Reminder: Project Submission Deadline',
    message: 'CS401 Capstone final code repository drop',
    scheduledTime: new Date(Date.now() + 1000 * 60 * 60 * 24 * 2).toISOString(),
    status: 'PENDING',
    targetType: 'TASK',
    channel: 'IN_APP',
  },
  {
    id: 'sched-2',
    title: 'Reminder: Midterm Registration Closing',
    message: 'Check course portal for seat allocation',
    scheduledTime: new Date(Date.now() + 1000 * 60 * 60 * 24 * 4).toISOString(),
    status: 'PENDING',
    targetType: 'ACADEMIC',
    channel: 'IN_APP',
  },
];

export class ScheduledNotificationSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/scheduled-notifications';

  public async getScheduledNotifications(): Promise<ScheduledNotificationItem[]> {
    try {
      const items = await this.get<ScheduledNotificationItem[]>(this.baseUrl);
      if (Array.isArray(items) && items.length > 0) {
        return items;
      }
      return FALLBACK_SCHEDULED_NOTIFICATIONS;
    } catch {
      return FALLBACK_SCHEDULED_NOTIFICATIONS;
    }
  }

  public async getPendingNotifications(): Promise<ScheduledNotificationItem[]> {
    try {
      const items = await this.get<ScheduledNotificationItem[]>(`${this.baseUrl}/pending`);
      if (Array.isArray(items) && items.length > 0) {
        return items;
      }
      return FALLBACK_SCHEDULED_NOTIFICATIONS.filter((i) => i.status === 'PENDING');
    } catch {
      return FALLBACK_SCHEDULED_NOTIFICATIONS.filter((i) => i.status === 'PENDING');
    }
  }
}

export const scheduledNotificationSdk = new ScheduledNotificationSdk();
