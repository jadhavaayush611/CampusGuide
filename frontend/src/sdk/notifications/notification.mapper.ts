import { NotificationDto } from './notification.dto';
import {
  NotificationItem,
  NotificationCategory,
  NotificationPriority,
  NotificationDeliveryStatus,
  RelatedEntity,
} from '../../models/notification.model';

export function mapNotificationCategory(rawCategory?: string | null, rawType?: string | null): NotificationCategory {
  const cat = (rawCategory || rawType || '').toUpperCase();
  if (cat.includes('ACADEMIC')) return 'Academic';
  if (cat.includes('PLANNER') || cat.includes('TASK') || cat.includes('REMINDER')) return 'Planner';
  if (cat.includes('CALENDAR') || cat.includes('EVENT')) return 'Calendar';
  if (cat.includes('COMMUNITIES') || cat.includes('COMMUNITY') || cat.includes('MENTION')) return 'Communities';
  if (cat.includes('COUNCIL')) return 'Councils';
  if (cat.includes('RESOURCE')) return 'Resources';
  if (cat.includes('NOTICE') || cat.includes('ANNOUNCEMENT')) return 'Notices';
  if (cat.includes('ATLAS')) return 'Atlas';
  if (cat.includes('AUTH')) return 'Authentication';
  return 'System';
}

export function mapNotificationPriority(rawPriority?: string | null): NotificationPriority {
  const p = (rawPriority || '').toUpperCase();
  if (p === 'URGENT') return 'URGENT';
  if (p === 'HIGH') return 'HIGH';
  if (p === 'LOW') return 'LOW';
  return 'NORMAL';
}

export function mapDeliveryStatus(rawStatus?: string | null): NotificationDeliveryStatus {
  const s = (rawStatus || '').toUpperCase();
  if (s === 'SCHEDULED' || s === 'PENDING') return 'SCHEDULED';
  if (s === 'FAILED') return 'FAILED';
  return 'DELIVERED';
}

export function resolveActionLink(
  category: NotificationCategory,
  providedLink?: string | null,
  relatedEntity?: RelatedEntity
): string {
  if (providedLink && providedLink.trim() !== '') {
    return providedLink;
  }
  switch (category) {
    case 'Planner':
      return relatedEntity?.id ? `/planner?taskId=${relatedEntity.id}` : '/planner';
    case 'Calendar':
      return relatedEntity?.id ? `/calendar?eventId=${relatedEntity.id}` : '/calendar';
    case 'Communities':
      return relatedEntity?.id ? `/communities/${relatedEntity.id}` : '/communities';
    case 'Councils':
      return relatedEntity?.id ? `/councils/${relatedEntity.id}` : '/councils';
    case 'Resources':
      return relatedEntity?.id ? `/resources?id=${relatedEntity.id}` : '/resources';
    case 'Notices':
      return relatedEntity?.id ? `/notices?id=${relatedEntity.id}` : '/notices';
    case 'Atlas':
      return '/academic';
    case 'Authentication':
      return '/profile';
    case 'Academic':
      return '/academic';
    case 'System':
    default:
      return '/notifications';
  }
}

export function mapNotificationDtoToModel(dto: NotificationDto): NotificationItem {
  const category = mapNotificationCategory(dto.category, dto.type);
  const priority = mapNotificationPriority(dto.priority);
  const deliveryStatus = mapDeliveryStatus(dto.deliveryStatus);
  const isRead = Boolean(dto.read ?? dto.isRead);
  const isArchived = Boolean(dto.archived ?? dto.isArchived);
  const message = dto.message || dto.description || dto.title || '';
  const createdAt = dto.createdAt || new Date().toISOString();
  const timestamp = dto.time || dto.createdAt || 'Just now';

  const relatedEntity: RelatedEntity | undefined = dto.relatedEntity
    ? {
        type: dto.relatedEntity.type as any,
        id: dto.relatedEntity.id,
        name: dto.relatedEntity.name,
      }
    : undefined;

  const actionLink = resolveActionLink(category, dto.actionLink || dto.linkUrl, relatedEntity);

  return {
    id: dto.id,
    type: dto.type || category.toLowerCase(),
    title: dto.title,
    message,
    description: message,
    category,
    priority,
    sourceModule: dto.sourceModule || `${category} Module`,
    time: timestamp,
    timestamp,
    createdAt,
    readAt: dto.readAt ?? undefined,
    isRead,
    isArchived,
    deliveryStatus,
    actionLink,
    linkUrl: actionLink,
    relatedEntity,
  };
}

