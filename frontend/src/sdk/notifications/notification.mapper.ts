import { NotificationDto } from './notification.dto';
import { NotificationItem } from '../../models/notification.model';

export function mapNotificationDtoToModel(dto: NotificationDto): NotificationItem {
  return {
    id: dto.id,
    type: dto.type,
    title: dto.title,
    description: dto.description,
    time: dto.time || dto.createdAt || 'Just now',
    isRead: Boolean(dto.isRead),
    linkUrl: dto.linkUrl ?? undefined,
    createdAt: dto.createdAt ?? undefined,
  };
}
