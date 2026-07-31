/**
 * Notifications Backend DTO Schemas
 */

export interface NotificationDto {
  id: string;
  type: 'reminder' | 'mention' | 'event' | 'announcement';
  title: string;
  description: string;
  time?: string | null;
  isRead: boolean;
  linkUrl?: string | null;
  createdAt?: string | null;
}

export interface UnreadCountDto {
  unreadCount: number;
}
