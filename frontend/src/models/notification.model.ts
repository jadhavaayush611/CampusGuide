/**
 * Frontend UI Domain Models for Notifications
 */

export type NotificationCategory =
  | 'Academic'
  | 'Planner'
  | 'Calendar'
  | 'Communities'
  | 'Councils'
  | 'Resources'
  | 'Notices'
  | 'Atlas'
  | 'Authentication'
  | 'System';

export type NotificationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export type NotificationDeliveryStatus = 'DELIVERED' | 'SCHEDULED' | 'FAILED';

export interface RelatedEntity {
  type: 'PlannerTask' | 'CalendarEvent' | 'Community' | 'Council' | 'Resource' | 'Notice' | 'Atlas' | 'Auth' | 'System';
  id: string;
  name?: string;
}

export interface NotificationItem {
  id: string;
  type?: 'reminder' | 'mention' | 'event' | 'announcement' | string;
  title: string;
  message: string;
  description?: string; // alias for backward compatibility
  category: NotificationCategory;
  priority: NotificationPriority;
  sourceModule: string;
  time?: string; // display timestamp (e.g., '10m ago')
  timestamp: string;
  createdAt: string; // ISO date
  readAt?: string;
  isRead: boolean;
  isArchived: boolean;
  deliveryStatus: NotificationDeliveryStatus;
  actionLink?: string;
  linkUrl?: string; // alias for backward compatibility
  relatedEntity?: RelatedEntity;
}

export interface NotificationsSummary {
  unreadCount: number;
  items: NotificationItem[];
}

export interface NotificationQueryParams {
  search?: string;
  category?: NotificationCategory | 'ALL';
  status?: 'ALL' | 'UNREAD' | 'READ' | 'ARCHIVED';
  priority?: NotificationPriority | 'ALL';
  deliveryStatus?: NotificationDeliveryStatus | 'ALL';
  sortBy?: 'createdAt' | 'priority' | 'title';
  sortOrder?: 'asc' | 'desc';
  page?: number;
  limit?: number;
}

export interface NotificationStats {
  total: number;
  unread: number;
  read: number;
  archived: number;
  delivered: number;
  scheduled: number;
  failed: number;
}

