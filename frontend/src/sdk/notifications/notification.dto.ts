/**
 * Notifications Backend DTO Schemas
 */

export interface NotificationDto {
  id: string;
  type?: string | null;
  category?: string | null;
  title: string;
  message?: string | null;
  description?: string | null;
  priority?: string | null;
  read?: boolean | null;
  isRead?: boolean | null;
  archived?: boolean | null;
  isArchived?: boolean | null;
  deliveryStatus?: string | null;
  sourceModule?: string | null;
  linkUrl?: string | null;
  actionLink?: string | null;
  createdAt?: string | null;
  readAt?: string | null;
  time?: string | null;
  metadata?: Record<string, any> | null;
  relatedEntity?: {
    type: string;
    id: string;
    name?: string;
  } | null;
}

export interface UnreadCountDto {
  unreadCount?: number;
  count?: number;
}

