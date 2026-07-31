/**
 * Frontend UI Domain Models for Notifications
 */

export interface NotificationItem {
  id: string;
  type: 'reminder' | 'mention' | 'event' | 'announcement';
  title: string;
  description: string;
  time: string;
  isRead: boolean;
  linkUrl?: string;
  createdAt?: string;
}

export interface NotificationsSummary {
  unreadCount: number;
  items: NotificationItem[];
}
